# 04. Information architecture

Owner role: information architect. Inputs: 03.

## Main objects (the things in the app)

Promise, Ask, Person, Slip, Day, Stretch, Proof, House rule, Note, Proposal, Debrief.
Each is one file kind in the vault (see 09). The app shows them; it never invents a second list.

## Navigation

Phone, bottom bar with four tabs, one top-right menu. Maximum depth: three taps from Today to any item.

```
Today ─ Promises ─ People ─ Notes        (menu: ⋮)
                                           ├ Evening sheet
                                           ├ Review queue
                                           ├ Stretch ladder
                                           ├ Proof wall
                                           ├ Weekly sit-down
                                           ├ House rules
                                           ├ Off-reach and blocklist
                                           ├ Pause
                                           ├ Talk to someone
                                           └ Settings
```

Windows: a tray icon opens a small window with Today and Promises, plus the same menu. No full app needed.

## Screen inventory

| ID | Screen | Reached from | Shows | Main action |
| --- | --- | --- | --- | --- |
| S-01 | Today | Tab 1, lock-screen widget | Date and day, three tasks, one care item | Open a task |
| S-02 | Promises | Tab 2 | Open promises by due time, a kept and slipped switch | Open a promise |
| S-03 | Promise detail | S-02, notifications | Who, what, due, evidence, history | Done, Move, Release |
| S-04 | People | Tab 3 | Everyone, grouped by tier | Open a person |
| S-05 | Person detail | S-04 | Tier, open promises, last asks, avoidance count | Retag tier, Off-reach |
| S-06 | Notes | Tab 4 | Vault notes from Obsidian, search first | Open a note |
| S-07 | Note viewer | S-06 | Read-only note with a link list | Request edit (goes to review queue) |
| S-08 | Evening sheet | Menu, 20:30 notification | The day's facts | Review proposals |
| S-09 | Review queue | Menu, S-08 | Proposals to existing notes, one at a time | Approve, Reject |
| S-10 | Stretch ladder | Menu | Today's rung and the ladder | Mark done |
| S-11 | Proof wall | Menu | Dated list of kept things | Open evidence |
| S-12 | Weekly sit-down | Menu, Sunday notification | Week facts, one decision | Write a house rule |
| S-13 | House rules | Menu, S-12 | Rules, on or off | Add, edit, switch off |
| S-14 | Off-reach and blocklist | Menu | Auto-blocked apps, your off-reach list | Add, remove (off-reach only) |
| S-15 | Pause | Menu, Quick Settings tile | Duration choice | Start pause |
| S-16 | Talk to someone | Menu | 14416 and a call button | Call |
| S-17 | Setup | First run, Settings | Permission steps, vault link | Grant, Check |
| S-18 | Settings | Menu | Times, language, learned day shape, sync state | Edit |
| S-19 | Debrief recorder | Today button, notification action | Mic, 20-second ring | Stop |
| S-20 | Wrong sheet | Any slip card | Why it was wrong | Send |

## Interrupt surfaces (outside the app)

| ID | Surface | When | Pause | Dismiss |
| --- | --- | --- | --- | --- |
| I-01 | Gate card (full screen) | Acceptance typed in reply to an ask | 5 s | After 5 s |
| I-02 | Slip card (full screen) | A mismatch or slip is confirmed | 5 s | After 5 s |
| I-03 | Focus card (full screen) | Feed drift with something due | 5 s | After 5 s |
| I-04 | Care nudge (notification) | Learned times | none | Swipe |
| I-05 | Lock-screen widget | Always | none | none |
| I-06 | Persistent status notification | Always, silent, with Pause and Debrief buttons | none | Not dismissible |

## Naming used on screen

| Internal | On screen |
| --- | --- |
| Promise | Promise |
| Ask | Ask |
| Slip | Missed |
| Mismatch | Doesn't match |
| House rule | My rule |
| Off-reach | Private |
| Proposal | To approve |
| Stretch | Stretch |
| Proof | Proof |

## Tab 2 and Today content rules

- Today shows three tasks. If there are more than three, the three chosen are: overdue first, then due soonest, then tier 1 first. The rest are one tap away in Promises.
- The care item rotates through: eat, water, stand, outside for 5 minutes, bedtime. Exactly one per day, chosen by what the last 7 days lacked.
- Empty state for Today: "Nothing due. Check Promises for next week."

Changes: none yet.
