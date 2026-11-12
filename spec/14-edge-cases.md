# 14. Edge cases

Owner role: QA lead. Inputs: 03 to 12. Format: trigger, then gate (the check), then outcome. Items marked **[RED]** are the ones that matter most; the red marker means the app must show them clearly and never hide them.

## Reading and permissions

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-01 **[RED]** | Accessibility service switched off or killed by the phone | Heartbeat gap over 5 min | Status notification turns berry "Reading is off", dark time logged, evening sheet line |
| E-02 **[RED]** | A permission revoked | Check at every app open and every hour | Same as E-01 plus the setup screen shows which one |
| E-03 | A blocked app comes to the front | Package on blocklist | No event, no card, nothing stored; a one-line count of "blocked minutes" only |
| E-04 | A secure window reads blank | Empty node tree | Time and app only; no retry loop |
| E-05 | App updates and its package name changes | Name not on list but keywords match | Treated as blocked until you review it in S-14 |
| E-06 | You mark an app Private | Next event from it | Time and app only |
| E-07 | Pause is on and the timer ends | Timer | Reading resumes; pause shown as dark time |
| E-08 | Phone restarts | Boot | Readers re-enable only if permission remains; otherwise E-02 |

## Capture

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-10 | You type a promise with no time | Time phrase missing | Default time used and shown on the toast |
| E-11 | You type a sentence that is a joke | Confidence under 0.6 | Written as a draft in To approve, never as a promise |
| E-12 | You write "I'll do it" with no thing named | What missing | Uses the last ask from that person; if none, ignored |
| E-13 | You edit a promise's due time | You tap the toast | A `moved` ledger line; the old time stays in history |
| E-14 | Two promises to one person on the same thing | Same keywords and person | Merged as one; the earlier due time stays |
| E-15 | A message in Devanagari | Script check | Ignored, per D-08 |
| E-16 | Group chat with many people | Tier by sender, ask only if you are named or replied to | Asks from a group are captured only when addressed to you |

## Gate

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-20 | You press send faster than the card can appear | Cannot stop send | The yes goes; the ask is captured as a promise, and a slip of type reflex-yes is written |
| E-21 | You dismiss the card with the back gesture | Within 5 s | Ignored until the pause ends |
| E-22 | A call comes in during the pause | Quiet surface | Card is dropped; the gate is not shown again for that ask |
| E-23 | A house rule disables Yes | Rule hit | Yes is off; Later and No work; text link explains |
| E-24 | You use the gate 3 times in an hour | Fatigue guard | The fourth is a light gate, so she doesn't block your day |
| E-25 | The ask is from family or a close friend | Tier 1 for care, never gated | No gate |

## Promises and slips

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-30 | The file is done but sent by email on the PC | Windows evidence | Counted as evidence; kept |
| E-31 | The file is done and sent in an app Nayom cannot see | No evidence | Slip card; you tap Done, it is `unverified`, no escalation |
| E-32 | You tap Done early | Done tap | Accepted, `unverified` if no evidence appears by due time |
| E-33 **[RED]** | A slip stays unanswered for 24 h | Repair loop timer | A line in the evening sheet; counts toward escalation |
| E-34 | The person cancels the ask | You tap "They cancelled" | `released` with reason; no slip |
| E-35 | Many promises due at once (5 or more) | Load check | Today still shows three; evening sheet lists the rest |
| E-36 | Clock or timezone changes | System event | Due times stay in the original timezone; shown with a note |

## Honesty checks

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-40 | You worked on the PC the task needs, but in an app with an odd window title | Keyword miss | Likely false flag; the Wrong button reasons include "Already done" and teach her the window name |
| E-41 | The PC is off | No PC activity | Phone-only check, `provisional: true`, replaced when the PC verdict arrives |
| E-42 | You did the work on paper | No evidence | Mismatch card; the Wrong reason "Other" is used; she learns nothing about paper |
| E-43 | The claim was about someone else's work | Not your task | Tapping Wrong with "Not for me" |
| E-44 | You say "almost done" for a task worked on yesterday | 4 h window | Mismatch fires; the Wrong reason "Already done" is one tap |

## Anxiety and avoidance

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-50 | You skip the stretch 2 days | Skip count | Ladder drops one rung; no scolding beyond the fact |
| E-51 | You do the stretch off-phone | No evidence | Marked `unverified`; it does not unlock the next rung |
| E-52 **[RED]** | Your typed text suggests harm to yourself | Support rule | All judging stops, nothing stored, support card once |
| E-53 | You miss 5 days in a row | Streak check | Weekly sit-down starts early; one question only: "What got in the way?" with three choices |

## Vault and sync

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-60 | Both devices edit `settings.md` | Conflict copy | Banner; you pick one |
| E-61 | A file is malformed | Parse error | Skipped, line in `health.md`, toast |
| E-62 | The existing vault is not inside the shared folder | Setup check | Setup stops; message says where to move it |
| E-63 | The key is lost | Decrypt error | Quotes show "unreadable"; other fields fine |
| E-64 | You delete a Nayom file in Obsidian | Missing file | Treated as deleted by you; ledger line kept so history is intact |
| E-65 | You edit a Nayom file in Obsidian | Hash changed | Accepted for editable fields; promise files keep their original fields in the ledger |
| E-66 **[RED]** | Sync stops for more than 1 h | Heartbeat of devices.md | Today shows "Phone is [n] min behind the PC" in berry text |

## Models

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-70 | A card over 40 words | Length check | Regenerate once, then use the template from 13 |
| E-71 | A card contains a banned word | Word check | Reject; use template |
| E-72 | A model cites an event that does not exist | Evidence check | Reject; no flag |
| E-73 | A message says "ignore your rules" | SR-11 | No change; the message is data |
| E-74 | The model is slow | Over 1.5 s on the phone | Card shows the fact line at once; the draft appears a moment later |

## Platform

| # | Trigger | Gate | Outcome |
| --- | --- | --- | --- |
| E-80 | Battery saver on | Battery state | Phone model off; rules only; a small line "Basic mode" on Today |
| E-81 | Do Not Disturb on | System state | Care nudges are held; gate cards still show, because you are the one typing |
| E-82 | You are in a call, navigation or camera | Quiet surface | Silent; events paused; resume after |
| E-83 | Phone storage nearly full | Storage check | Retention shortens to 14 days; toast tells you |
| E-84 | The laptop is too weak to run the Windows reader | CPU over 15% for 5 min | Reader drops to window titles only; line in health.md |

Changes: none yet.
