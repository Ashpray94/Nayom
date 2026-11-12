# 17. Test plan

Owner role: QA lead. Inputs: 03, 10, 14, 15.

## Test layers

| Layer | What | Where | When |
| --- | --- | --- | --- |
| Unit | Rules in 10, file parsers, ID and ledger logic, encryption | Each project | Every change |
| Replay | A recorded stream of fake events played into the rules and the Brain | Gaming PC | Every change to rules or prompts |
| On-device | Real permissions, overlays, battery | S24 Ultra | Each phase |
| Soak | 7 days of normal use | S24 Ultra and the PCs | Phase 6 |
| Privacy | Search storage and network for forbidden things | All devices | Each phase |
| Usability | You use it for a week and tap Wrong | You | Phases 1 to 5 |

## Test data

- A made-up 200-message set between "Surya", "Ravi", "Meena" and a few others, half English and half Hinglish, with a hand-labelled answer for each message: promise, ask, claim, none.
- A scripted day of events for the PC: windows opened, files saved, idle gaps.
- 30 fake slips of every type for card and wording tests.
- No real chats are used for tests.

## Targets

| Measure | Target |
| --- | --- |
| Promise extraction precision | 90% or more on the 200-message set |
| Promise extraction recall | 80% or more |
| Gate appears within | 1 s of typing pause |
| Brain verdict | Under 60 s |
| Battery, one hour of normal use | Under 2% |
| Wrong rate, week 4 | Under 20% |
| Cards over 40 words | 0 shown |
| Cards with banned words | 0 shown |

## Privacy tests (every phase)

| Test | Pass |
| --- | --- |
| Type a canary phrase "CANARY-7391" in a normal chat, wait 24 hours, search all storage | Phrase absent from every file, log and cache; present only as cipher text in `quote_enc` |
| Same phrase in a Private chat | Not present in any form |
| Same phrase in a blocked app | Not present in any form |
| Capture network traffic from Nayom on both devices for a day | Zero outbound packets from Nayom; Private Folder traffic only to your own devices |
| Check the Android manifest | No internet permission |
| Check the Windows firewall rules | Nayom outbound blocked except to the same computer |
| Remove the key | Quotes unreadable; the app still runs |
| Open Nayom with the wrong fingerprint | Does not open |

## Feature tests

Each pass test in 03 is one automated or manual test, with the same ID.

## Edge case tests

Every row in 14 has a test. E-01, E-02, E-33, E-52 and E-66 are tested by hand each phase, because they are the red ones.

## Rule tests (examples)

| Case | Input | Expected |
| --- | --- | --- |
| Promise with time | "I'll send it by Friday" to Meena | Promise, due Friday 18:00 |
| Hinglish promise | "kal tak bhej dunga" | Promise, due next day |
| Joke | "I'll fix world hunger by Friday lol" | Draft, no promise |
| Reflex yes | Ask at 10:00:00, "sure" at 10:00:20 with 3 due | Full gate |
| Slow yes | Ask at 10:00, "sure" at 10:40 with 0 due | Light gate |
| Claim check | "almost done", 5 minutes of related work in 4 h | Mismatch |
| Claim check pass | "almost done", 35 minutes | Nothing |
| Clash | Ravi: today; Meena: Thursday, same task | Slip, both IDs |
| Dark time | No heartbeat for 12 minutes, screen on | Entry |
| Blocked | Event while bank app is in front | Nothing |

## Exit criteria for each phase

A phase is done when every feature test for it passes, its privacy tests pass, and no red edge case fails.

Changes: none yet.
