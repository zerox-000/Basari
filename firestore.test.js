const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(uid, now) {
  return {
    userId: uid,
    firstName: "Ayşe",
    lastName: "Yılmaz",
    gradeLevel: "12. Sınıf",
    branch: "Sayısal",
    targetExam: "YKS",
    targetScore: 480,
    dailyQuestionGoal: 120,
    learningStyle: "Görsel",
    preferredStudyHours: "Akşam (18:00-22:00)",
    dailyStudyMinutes: 240,
    themePalette: "OCEAN",
    darkModePref: "SYSTEM",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated users are denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now)));
  await assertFails(unauthDb.collection("leaderboard").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("writtenGrades").get()
  );
});

test("User profile create, partial update, shadow field rejection, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      themePalette: "EMERALD",
      dailyQuestionGoal: 150,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID, new Date(Date.now() + 60000)))
  );

  // Reject cross-user read & write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ firstName: "Hacked", updatedAt: new Date(Date.now() - 1000) })
  );
});

test("WrittenGrades, QuestionLogs, MockExams, CustomCountdowns, AiReports enforce ownership and schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // WrittenGrade
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("writtenGrades").doc("wg_1").set({
      userId: ALICE_UID,
      subjectId: "mat",
      subjectName: "Matematik",
      term: "1. Dönem",
      examType: "1. Yazılı",
      score: 92.5,
      note: "Türev çalıştım",
      recordedAtMillis: Date.now() - 2000,
      createdAt: now,
      updatedAt: now,
    })
  );
  // Reject invalid score > 100
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("writtenGrades").doc("wg_bad").set({
      userId: ALICE_UID,
      subjectId: "mat",
      subjectName: "Matematik",
      term: "1. Dönem",
      examType: "1. Yazılı",
      score: 120,
      recordedAtMillis: Date.now() - 2000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // QuestionLog
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("questionLogs").doc("ql_1").set({
      userId: ALICE_UID,
      subjectId: "fiz",
      subjectName: "Fizik",
      topicName: "Newton Hareket Yasaları",
      difficulty: "Orta",
      source: "Soru Bankası",
      totalCount: 20,
      correctCount: 16,
      wrongCount: 3,
      blankCount: 1,
      recordedAtMillis: Date.now() - 1000,
      createdAt: now,
      updatedAt: now,
    })
  );
  // Reject mismatched counts
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("questionLogs").doc("ql_bad").set({
      userId: ALICE_UID,
      subjectId: "fiz",
      subjectName: "Fizik",
      topicName: "Newton Hareket Yasaları",
      difficulty: "Orta",
      source: "Soru Bankası",
      totalCount: 20,
      correctCount: 19,
      wrongCount: 3,
      blankCount: 1,
      recordedAtMillis: Date.now() - 1000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // MockExam
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("mockExams").doc("me_1").set({
      userId: ALICE_UID,
      examTitle: "TYT Deneme 1",
      examType: "TYT",
      totalCorrect: 85,
      totalWrong: 12,
      totalBlank: 23,
      totalNet: 82.0,
      estimatedScore: 410.5,
      subjectResults: [
        { subjectId: "mat", subjectName: "Matematik", correct: 30, wrong: 4, blank: 6, net: 29.0, weakTopics: ["Problemler"] }
      ],
      recordedAtMillis: Date.now() - 1000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // CustomCountdown
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("customCountdowns").doc("cd_1").set({
      userId: ALICE_UID,
      title: "Fizik Yazılısı",
      category: "Yazılı",
      targetDateMillis: Date.now() + 864000000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // AiReport
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("aiReports").doc("ai_1").set({
      userId: ALICE_UID,
      summary: "Genel ilerlemen çok iyi, Fizik mekanik konularına odaklanmalısın.",
      weakAreas: [{ subject: "Fizik", topic: "Newton Yasaları", severity: "Yüksek", reason: "Hata oranı %20" }],
      priorityTasks: [{ title: "MEB Kazanım Testi Çöz", priority: "Yüksek", durationMinutes: 45 }],
      studyTips: ["Görsel şemalarla kuvvet diyagramı çiz"],
      resources: ["MEB OGM Materyal Fizik 11"],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot list or read Alice's subcollections
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("writtenGrades").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("questionLogs").doc("ql_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("mockExams").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("studyPlans").get());

  // StudyPlan create & toggle session completion update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("studyPlans").doc("plan_1").set({
      userId: ALICE_UID,
      planTitle: "12. Sınıf Sayısal Haftalık Program",
      coachNote: "Matematik ve Fizik ağırlıklı planlandı.",
      sessions: [
        {
          sessionId: "s1",
          dayOfWeek: "Pazartesi",
          timeSlot: "18:30 - 19:30",
          subjectName: "Matematik",
          topicName: "Türev",
          activityType: "Soru Çözümü",
          targetQuestions: 40,
          durationMinutes: 60,
          completed: false
        }
      ],
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("studyPlans").doc("plan_1").update({
      sessions: [
        {
          sessionId: "s1",
          dayOfWeek: "Pazartesi",
          timeSlot: "18:30 - 19:30",
          subjectName: "Matematik",
          topicName: "Türev",
          activityType: "Soru Çözümü",
          targetQuestions: 40,
          durationMinutes: 60,
          completed: true
        }
      ],
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("studyPlans").doc("plan_1").get());
});

test("Leaderboard is readable by any signed-in user but writable only by owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayşe Y.",
      gradeLevel: "12. Sınıf",
      branch: "Sayısal",
      targetExam: "YKS",
      totalSolvedQuestions: 450,
      totalCorrectQuestions: 390,
      averageWrittenGrade: 91.5,
      latestMockNet: 88.25,
      rankingScore: 1520.5,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob can query and read Alice's public leaderboard card
  await assertSucceeds(bobDb.collection("leaderboard").get());
  await assertSucceeds(bobDb.collection("leaderboard").doc(ALICE_UID).get());

  // Bob cannot overwrite Alice's leaderboard entry
  await assertFails(
    bobDb.collection("leaderboard").doc(ALICE_UID).update({
      rankingScore: 0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
