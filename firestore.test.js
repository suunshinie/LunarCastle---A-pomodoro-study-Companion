const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_scholar_1";
const BOB_UID = "bob_scholar_2";

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

test("Unauthenticated access is denied across all user subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection(`users/${ALICE_UID}/focusSessions`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/notes`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/milestones`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/books`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/quotes`).get());

  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/notes`).doc("n1").set({
      userId: ALICE_UID,
      title: "Midnight Note",
      body: "Drafting",
      tag: "Study",
      pinned: false,
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("Cross-user reads and writes are strictly denied", async () => {
  const past = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection(`users/${BOB_UID}/notes`).doc("bob_note").set({
      userId: BOB_UID,
      title: "Bob Private Note",
      body: "Secret draft",
      tag: "Draft",
      pinned: true,
      createdAt: past,
      updatedAt: past,
    });
    await adminDb.collection(`users/${BOB_UID}/books`).doc("bob_book").set({
      userId: BOB_UID,
      title: "Piranesi",
      author: "Susanna Clarke",
      status: "Reading",
      currentPage: 42,
      totalPages: 245,
      bookmarkLabel: "Hall of Statues",
      createdAt: past,
      updatedAt: past,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection(`users/${BOB_UID}/notes`).get());
  await assertFails(aliceDb.collection(`users/${BOB_UID}/notes`).doc("bob_note").get());
  await assertFails(
    aliceDb.collection(`users/${BOB_UID}/notes`).doc("bob_note").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(aliceDb.collection(`users/${BOB_UID}/books`).doc("bob_book").delete());
});

test("Owner can create and delete FocusSessions, but cannot update them", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 1000);
  const sessionRef = aliceDb.collection(`users/${ALICE_UID}/focusSessions`).doc("sess_1");

  await assertSucceeds(
    sessionRef.set({
      userId: ALICE_UID,
      label: "Chapter IV Outline",
      durationMinutes: 25,
      teaName: "Earl Grey Moonlight",
      teaSteepMinutes: 4,
      soundscapes: ["Night Rain", "Library Hearth"],
      completedAt: past,
    })
  );

  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/focusSessions`).get());

  // Immutable session logs cannot be updated
  await assertFails(sessionRef.update({ durationMinutes: 99 }));

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/focusSessions`).doc("sess_future").set({
      userId: ALICE_UID,
      label: "Future",
      durationMinutes: 25,
      teaName: "Chamomile",
      teaSteepMinutes: 5,
      soundscapes: [],
      completedAt: new Date(Date.now() + 60000),
    })
  );

  await assertSucceeds(sessionRef.delete());
});

test("Owner StudyNote & DiamondMilestone lifecycle enforces strict schemas and partial updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 1000);

  const noteRef = aliceDb.collection(`users/${ALICE_UID}/notes`).doc("note_1");
  await assertSucceeds(
    noteRef.set({
      userId: ALICE_UID,
      title: "Nocturne Reflections",
      body: "Rain against the turret glass.",
      tag: "Reflection",
      pinned: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update succeeds
  await assertSucceeds(
    noteRef.update({
      pinned: true,
      updatedAt: updated,
    })
  );

  // Reject ghost field ("shadow update") and immutable createdAt overwrite
  await assertFails(
    noteRef.update({
      pinned: false,
      isAdmin: true,
      updatedAt: updated,
    })
  );
  await assertFails(
    noteRef.update({
      createdAt: updated,
      updatedAt: updated,
    })
  );

  // Reject `@DocumentId` 'id' field in create payload
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/notes`).doc("note_with_id").set({
      id: "note_with_id",
      userId: ALICE_UID,
      title: "Bad ID",
      body: "",
      tag: "Study",
      pinned: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Milestone create & diamond check toggle
  const milestoneRef = aliceDb.collection(`users/${ALICE_UID}/milestones`).doc("ms_1");
  await assertSucceeds(
    milestoneRef.set({
      userId: ALICE_UID,
      title: "Finish opening stanza",
      completed: false,
      linkedTea: "Oolong Mist",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    milestoneRef.update({
      completed: true,
      updatedAt: updated,
    })
  );
});

test("Owner BookEntry & QuoteCard enforce status enums, page bounds, and updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 1000);

  const bookRef = aliceDb.collection(`users/${ALICE_UID}/books`).doc("book_1");
  await assertSucceeds(
    bookRef.set({
      userId: ALICE_UID,
      title: "The Shadow of the Wind",
      author: "Carlos Ruiz Zafon",
      status: "Reading",
      currentPage: 120,
      totalPages: 487,
      bookmarkLabel: "Cemetery of Forgotten Books",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Invalid status enum & currentPage > totalPages rejected
  await assertFails(bookRef.update({ status: "Archived", updatedAt: updated }));
  await assertFails(bookRef.update({ currentPage: 500, updatedAt: updated }));

  // Valid page update succeeds
  await assertSucceeds(
    bookRef.update({
      currentPage: 135,
      updatedAt: updated,
    })
  );

  const quoteRef = aliceDb.collection(`users/${ALICE_UID}/quotes`).doc("quote_1");
  await assertSucceeds(
    quoteRef.set({
      userId: ALICE_UID,
      bookId: "book_1",
      bookTitle: "The Shadow of the Wind",
      quoteText: "Every book, every volume you see here, has a soul.",
      pageNumber: 5,
      reflectionNote: "Atmosphere for late-night writing.",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    quoteRef.update({
      reflectionNote: "Revisited over Chamomile Hearth tea.",
      updatedAt: updated,
    })
  );
});
