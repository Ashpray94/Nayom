# 15. Roadmap

Owner role: delivery manager. Inputs: 03, 12. One builder, so each phase is small enough to finish and use. Sizes: S about a week of focused work, M about two, L about four. These are rough, to be corrected after Phase 0.

All four problem areas are version one (D-04). Phases are ordered by what depends on what, so the whole of V1 arrives as quickly as the dependencies allow.

## Phase 0. Foundations (M)

Goal: the ground is safe and the app can see.

1. Private Folder: LAN-only switch (DEP-1).
2. Private Folder: Android background sync that survives Android limits and reboot (DEP-2).
3. Private Folder: let Nayom read and write the shared folder (DEP-3).
4. Move your existing vault into the shared folder on the home PC (DEP-4).
5. Vault layout and settings files created (09).
6. Nayom Phone shell: setup flow, permissions, heartbeat, blocklist check, Private marking, Pause, support card, biometric lock.
7. Benchmark of a small model on the S24 Ultra; benchmark of the 7–8B model on the 4060 Ti.

Done when: the heartbeat line appears once a minute in `events/ph/`; a blocked app produces no event; Pause stops all events; a note edited on the PC opens on the phone within 2 minutes; Private Folder shows no internet connection with LAN-only on.
Features: FR-80, FR-90 to FR-93, FR-A1, DEP-1 to DEP-4.

## Phase 1. Capture and promises (L)

Goal: she remembers.

1. Promise and ask capture (FR-01, FR-02, FR-04).
2. Debrief (FR-03).
3. Promise ledger, reminders, done check from phone evidence, repair loop, release (FR-20 to FR-24).
4. Today screen, evening sheet, review queue and adoption (FR-40, FR-70, FR-73, FR-81, FR-82).
5. Recall and Notes tab (FR-05, FR-81).
6. Dark time (FR-44).

Done when: the pass tests in 03 for these features all pass on the real phone, with the 200-message test set from 17; an hour of normal use costs under 2% battery.

## Phase 2. The yes-gate (M)

Goal: she stops the reflex yes.

1. Gate trigger and card (FR-10, FR-11).
2. House rules and enforcement (FR-12).
3. Scripts (FR-13).
4. Quiet surfaces (FR-94).

Done when: the gate appears within 1 s of a typing pause in WhatsApp and the default SMS app; Back is ignored for 5 s; a house rule turns off Yes.

## Phase 3. The PC, the brain and honesty (L)

Goal: she compares what you say with what you do.

1. Nayom Windows: readers, tray window, browser extension, blocklist (FR-W1).
2. Brain: file watcher, rules engine, local model server (FR-B1).
3. Mismatch, clash and truth repair (FR-30 to FR-32).
4. Done check upgraded with PC evidence (FR-21).
5. Key creation and pairing, quote encryption (D-18).

Done when: "almost done" with no work flags a mismatch within 60 s; a file exported on the PC marks a promise kept; the quote is cipher text in the vault.

## Phase 4. Focus, care and the stretch ladder (M)

1. Focus card (FR-41).
2. Care nudges and day shape (FR-42, FR-43).
3. Avoidance spotting and the stretch ladder (FR-50, FR-51).
4. Proof wall (FR-60).
5. Think first (FR-52).

Done when: a 10-minute feed drift with something due shows a focus card; a stretch rung is marked done only with evidence.

## Phase 5. Learning and growing up (M)

1. Learning from Wrong taps (FR-L1).
2. Scaffold fade (FR-72).
3. Weekly sit-down (FR-71).
4. Identity lines and respect check (FR-61, FR-62).

Done when: three Wrong taps of one pattern stop that pattern; reminders drop after two weeks above 80%.

## Phase 6. Hardening and polish (M)

1. Battery and storage soak test over 7 days.
2. All edge cases from 14 tested.
3. Hinglish tuning with real data.
4. Release checklist in 18 completed.
5. Logo and icon produced.

Done when: 7 days with no crash, no missed heartbeat other than a charged-off phone, and a Wrong rate under 20% in week 4.

## Order and dependencies

```
Phase 0 ─ Phase 1 ─ Phase 2
                 └─ Phase 3 ─ Phase 4 ─ Phase 5 ─ Phase 6
```

Phase 2 and Phase 3 can overlap once Phase 1 is done.

## What the engineers do first

Day one: open the Private Folder repo and start DEP-1, the LAN-only switch. Nothing in Nayom is safe to run until Private Folder stops using the internet.

Changes: none yet.
