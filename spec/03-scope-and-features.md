# 03. Scope and features

Owner role: product manager. Inputs: 01, 02, decisions D-01 to D-20.

## Scope statement

**In:** a private Android app, a private Windows companion with a browser extension, a PC "brain" that does the heavier judging, and an Obsidian vault as the single database, all on devices you own.
**Out:** any cloud account, any server, other users, iPhone, call recording, auto-sending messages, reading of finance and secret-code apps.

Priority labels: **M** must, **S** should, **C** could. All four problem areas are V1 (D-04), so every feature below is M or S. Phases are in [15](15-roadmap.md).

## A. Capture and memory

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-01 | **Promise capture.** Reads what you type in chats and notes down any commitment: who, what, by when | M | 1 | You type "I'll send it by Friday" to Meena. Within 60 s a promise exists in the vault with person Meena and due Friday 18:00 (default time from 10 if none is stated) |
| FR-02 | **Ask capture.** Reads incoming notifications and open chats and notes down any request made of you | M | 1 | Ravi writes "can you tweak the banner today?" and an ask file exists with person, task and due today |
| FR-03 | **Debrief.** After a call or meeting, you tap Debrief and speak up to 20 seconds. Speech is turned to text on the phone; promises and asks are extracted; the audio is deleted at once | M | 1 | A 20-second debrief naming two promises creates two promise files and no audio file remains |
| FR-04 | **Agreement log.** Each promise or ask is a file in the vault | M | 1 | The file validates against the schema in 09 |
| FR-05 | **Recall.** Search "Meena" or "banner" and see everything agreed, newest first | M | 1 | Search returns results in under 1 s on 5,000 files |

## B. Promises and accountability

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-20 | **Promise ledger.** Every open promise, sorted by due time | M | 1 | The Promises tab matches the vault exactly after a sync |
| FR-21 | **Done check.** At the due time, Nayom looks for evidence the thing was done (sent file, sent message, saved file, work time) | M | 1 (phone), 3 (PC) | A promise due 17:00 with an exported file and an outgoing message by 17:05 is marked kept with the evidence IDs |
| FR-22 | **Repair loop.** Every slip needs an answer from you: a fix and a time, or a release | M | 1 | A slip stays "open" until you choose a fix time or tap Release |
| FR-23 | **Release.** You formally drop a promise and Nayom drafts the message to the person | M | 1 | A released promise has `status: released` and a draft was shown |
| FR-24 | **Reminders.** First reminder 2 h before, second at due time. Fewer as reliability rises | M | 1 | Reminder times follow `promise.remind_before_h` |

## C. Yes-gate

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-10 | **Gate trigger.** Detects you typing an acceptance in reply to a new ask | M | 2 | Typing "sure" in reply to an ask triggers the card within 1 s of a typing pause |
| FR-11 | **Gate card.** Full screen, 5-second pause, then dismissable. Shows load and what gets pushed out | M | 2 | Buttons stay disabled for 5 s, then enable. Back gesture also waits 5 s |
| FR-12 | **House rules.** Rules you wrote while calm, enforced when they trigger | M | 2 | A rule "no new asks from tier 2 or 3 when 2 promises are overdue" blocks the Yes button |
| FR-13 | **Honest scripts.** Ready drafts for no, later, late, and truth. You send them yourself | M | 2 | Each gate outcome shows a draft in the matching language (English or Hinglish) |

## D. Honesty

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-30 | **Statement clash.** Two things you told two people that cannot both be true | M | 3 | Told Ravi "today" and Meena "Thursday" for the same task: one slip card cites both messages |
| FR-31 | **Claim versus activity.** "Almost done" with no matching work | M | 3 | "Almost done" with under 20 min of related work in the last 4 h flags a mismatch |
| FR-32 | **Truth repair.** A draft of the honest version, with a real new time | M | 3 | Every mismatch card carries a draft |
| FR-52 | **Think first.** Before a big task, three prompts: goal, proper way, shortcut and its cost | S | 4 | Starting a task tagged big shows the three prompts once per task |

## E. Focus and day

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-40 | **Today.** Date, day, three tasks, one care item. Nothing else | M | 1 | Today shows exactly three tasks, chosen by due time and tier |
| FR-41 | **Focus card.** Fires when you drift into a feed during a work block with something due | M | 4 | 10 continuous minutes in a feed app with a promise due in 24 h shows the card |
| FR-42 | **Care nudges.** Eat, water, stand, bedtime. One care item per day | M | 4 | A 120-minute screen-on stretch at the PC gives a stand-up nudge |
| FR-43 | **Day shape.** Wake, work and sleep hours are learned | M | 4 | After 7 days the learned hours are within 60 minutes of your correction |
| FR-44 | **Dark time.** When the readers were off or blocked, Nayom logs it | M | 1 | Switching the accessibility service off for 30 minutes creates a dark-time entry |

## F. Anxiety and self-image

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-50 | **Avoidance spotting.** Messages read but not answered, replies typed and deleted, calls not returned | M | 4 | A message read at 10:00 and unanswered at 12:00 from a tier 1 person is flagged |
| FR-51 | **Stretch ladder.** One small uncomfortable action a day, built from your avoidance list | M | 4 | The first rung is the easiest avoided thing; a rung is done only with evidence or marked unverified |
| FR-60 | **Proof wall.** Dated evidence of what you did and kept | M | 4 | Every entry links to evidence IDs |
| FR-61 | **Identity lines.** A short statement of who you are, only from evidence | S | 5 | A line such as "You kept 9 of 11 this week" needs 9 kept promises in the vault |
| FR-62 | **Respect check.** Notices when you undersell yourself or take on work outside your role | S | 5 | Typing "free of cost, no problem" to a client shows the scope card |

## G. Review and autonomy

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-70 | **Evening fact sheet.** Plain facts, no scores | M | 1 | Counts match the vault |
| FR-71 | **Weekly sit-down.** A three-minute review with one decision | S | 5 | Fires Sunday evening (default) and ends with one written house-rule decision |
| FR-72 | **Scaffold fade.** Nayom reminds less as you do more on your own | M | 5 | Reminder count per promise falls when kept-without-reminder rate is above 80% for two weeks |
| FR-73 | **Review queue.** Her proposed edits to your existing notes wait for your one-time approval | M | 1 | Nothing in an existing note changes before approval |

## H. Vault

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-80 | **Nayom folder.** All her files live in `Nayom/` | M | 0 | No Nayom file outside `Nayom/` unless adopted |
| FR-81 | **Note retrieval.** Open any vault note inside Nayom | M | 1 | A note synced from the PC opens on the phone |
| FR-82 | **Adoption.** An existing note you approved can be edited by Nayom from then on | M | 1 | An approved note gets `nayom: adopted` in its properties |

## I. Safety and control

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-90 | **Blocklist.** Banks, finance, OTP, authenticators and password managers are never read | M | 0 | With a blocked app in front, no event is created |
| FR-91 | **Off-reach flag.** You mark an app, window, project, person or chat as off-reach. Only time and app name are logged | M | 0 | An off-reach window creates events with no quote and no text fields |
| FR-92 | **Pause.** One tap pauses all reading, up to 4 hours | M | 0 | While paused, no events are written; the pause itself is logged as dark time |
| FR-93 | **Support card.** A permanent "Talk to someone" entry and a one-time card when needed | M | 0 | The card shows 14416 and never stores the triggering text |
| FR-94 | **Quiet surfaces.** She never interrupts calls, emergency dialer, navigation or camera | M | 2 | No overlay appears over these screens |

## J. Platform

| ID | Feature | P | Phase | Pass test |
| --- | --- | --- | --- | --- |
| FR-A1 | **Phone readers.** Accessibility, notifications, usage, overlay | M | 0–1 | All four permissions granted and a heartbeat shows once a minute |
| FR-W1 | **Windows readers.** Active window, UI text, browser tab, idle time | M | 3 | Active window changes appear within 2 s |
| FR-B1 | **PC brain.** A local model on the gaming PC that judges the events | M | 3 | A slip event gets a verdict file within 60 s of arriving, when the PC is on |
| FR-L1 | **Learning.** Wrong taps change future flags | M | 5 | Three Wrong taps of the same pattern stop that pattern from flagging |

## Dependencies on Private Folder (Phase 0)

| ID | Change | Reason |
| --- | --- | --- |
| DEP-1 | LAN-only switch that turns off the internet relay, the GitHub bootstrap fetch and the update check | Meets "nothing leaves your network" |
| DEP-2 | Android background sync with WorkManager and restart after reboot | Foreground sync is capped around six hours a day on Android 15+ |
| DEP-3 | Nayom may read and write the synced folder through its documents provider | Needed for the vault on the phone |
| DEP-4 | Your existing vault moves into the shared folder on the home PC | Single shared folder means one vault |
