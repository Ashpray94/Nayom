# 10. Detection signals

Owner role: AI / agent behaviour designer, with the data architect. Inputs: 03, 08, 09.

Every number below is a default. They live in `Nayom/_system/settings.md` and can be changed. A signal fires only with evidence IDs (08).

## Language handling

English and Hinglish (D-08). Text is lower-cased, repeated letters squeezed ("sureee" becomes "sure"), and punctuation dropped. Hindi in Devanagari script is not read; such text is ignored.

### Seed phrase lists (editable in `learning.md`)

| List | Seeds |
| --- | --- |
| Acceptance | sure, ok, okay, yes, yep, will do, i'll do it, done, no problem, haan, ha kar dunga, ho jayega, kar deta hu, kar dunga, theek hai, pakka, bilkul |
| Time phrases | today, tonight, tomorrow, eod, by monday to sunday, this week, in an hour, aaj, aaj shaam tak, kal tak, subah tak, ek ghante mein, raat tak |
| Claim phrases | almost done, almost there, on it, working on it, nearly, bas ho gaya, ho gaya, kar raha hu, bhej raha hu, sent, done |
| Undersell phrases | free of cost, no charge, no problem i'll adjust, i'll just do it, no worries it's quick |
| Feed apps | Instagram, YouTube, X, Facebook, Reddit, Snapchat |

## Tier learning (D-07)

Tier measures stakes, not how fast you reply. If it used reply speed it would copy your people-pleasing into the ranking.

Score from: client or contract words, money words, formal greeting, deadlines named in their messages, how many promises you made to them that were kept or missed, and how often they appear. Tier 1 (high stakes), 2 (medium), 3 (low or unknown). Re-scored daily. Your override wins.

## Signals

### SIG-01 Promise capture
- Input: outgoing text containing a time phrase and a verb of doing.
- Rule: extract person from the open chat title, what from the sentence, due from the time phrase. No time phrase gives `default_due_time` (18:00 same day if before 15:00, else 11:00 next day).
- Guard: confidence below 0.6 is written as a draft and goes to To approve.
- Evidence: the typed event.

### SIG-02 Ask capture
- Input: incoming notification or visible message containing an imperative or "can you / could you / please / kar do / kar dena".
- Rule: person, what, due as above. Confidence below 0.6 is not written.

### SIG-03 Reflex yes (the gate)
- Input: acceptance phrase typed in the reply box of a chat that has an ask from the same person in the last 10 minutes (`gate.ask_window_min`).
- Fires: at a typing pause of 800 ms (`gate.pause_ms`).
- Gate type: full if open promises due within 48 h is 3 or more (`load.open_due_48h`), or a house rule hits, or the ask came less than 60 s before the yes (`gate.reflex_s`). Otherwise light: one line, no pause.
- Cannot do: stop the send button. It can only appear before you reach it.

### SIG-04 Missed promise
- Input: a promise past due plus `promise.grace_min` (30).
- Rule: search for evidence: an outgoing message to the person with an attachment, a file exported or saved with a name matching the promise keywords, or an outgoing message containing "sent". No evidence is a miss.
- Wrong guard: if you tapped Done, accept it, mark `unverified`, and do not add to the escalation count.

### SIG-05 Claim versus activity
- Input: a claim phrase in an outgoing message tied to an ask or promise.
- Rule: look back `claim.window_h` (4 hours). Count minutes of foreground time in apps or windows whose title or file name shares at least one keyword with the task. Below `claim.min_minutes` (20) is a mismatch.
- "Sent" or "done" claims: need a file-saved, file-exported or sent-with-attachment event since the ask. None is a mismatch.
- If the PC is off: check phone activity only, mark `provisional: true`.

### SIG-06 Statement clash
- Input: two promises or asks about the same task, to different people.
- "Same task" means two or more shared keywords.
- Rule: due times differ by more than `clash.min_gap_h` (4).
- Evidence: both promise IDs.

### SIG-07 Avoidance
- Read and unanswered: from tier 1, 2 h; tier 2, 4 h; tier 3, 24 h (`avoid.read_h.t1/t2/t3`). Counted only inside learned work hours.
- Typed and deleted: a reply typed and deleted 2 or more times within 30 minutes (`avoid.deleted_count`).
- Missed call: not returned within 24 hours (`avoid.call_h`).
- Result: added to the avoided list. No card for the first of each kind; it feeds the ladder.

### SIG-08 Distraction
- Input: feed app in foreground for 10 minutes continuously (`drift.feed_min`), or 40 app switches in one hour (`drift.switches_h`).
- Condition: a promise due within 24 hours and learned work block active.
- Result: focus card. At most 3 per day (`drift.max_per_day`).

### SIG-09 Care
- Stand: 120 minutes of screen-on at the PC with no 5-minute idle (`care.stand_min`).
- Bedtime: phone or PC screen on past learned bedtime. First nudge at bedtime, second 60 minutes later, then stop.
- Eat and water: reminder at the learned usual times. Taps are accepted without checking.
- One care item is shown on Today each day, rotating by what was lacking.

### SIG-10 Dark time
- Input: no heartbeat from a device reader for more than 5 minutes while the device was in use or unknown.
- Reason recorded: paused, permission revoked, battery kill, device off.
- Result: a line in the evening sheet. During learned work hours it also counts as a slip of type `unseen`.

### SIG-11 Undersell
- Input: undersell phrase in a chat with a tier 1 or 2 person.
- Result: scope card. At most 2 per day.

### SIG-12 Escalation count
- Per type per person, rolling 7 days. A kept promise lowers it by 1. See 08.

## Day shape learning (D-09)

- Wake: median first unlock on each weekday over 14 days.
- Sleep: median start of the longest screen-off and PC-idle gap each night.
- Work block: time spans where work apps or project windows are active for 20 minutes or more.
- Corrections you make count as three nights of data.
- Initial values before learning: wake 07:30, bedtime 23:30, work 09:30 to 18:30, evening sheet 20:30.

## Metrics and targets

Targets to verify on real use, not promises.

| Measure | Target |
| --- | --- |
| Promise extraction precision | 90% or more on a labelled set |
| Promise extraction recall | 80% or more |
| Share of flags tapped Wrong, week 4 | Under 20% |
| Gate shown within | 1 s of typing pause |
| Brain verdict latency | Under 60 s when PC on |

## settings.md defaults

```
schema: 1
default_due_time: 18:00 same day if typed before 15:00, else 11:00 next day
gate.pause_seconds: 5
gate.pause_ms: 800
gate.ask_window_min: 10
gate.reflex_s: 60
load.open_due_48h: 3
promise.remind_before_h: 2
promise.grace_min: 30
claim.window_h: 4
claim.min_minutes: 20
clash.min_gap_h: 4
avoid.read_h: {t1: 2, t2: 4, t3: 24}
avoid.deleted_count: 2
avoid.call_h: 24
drift.feed_min: 10
drift.switches_h: 40
drift.max_per_day: 3
care.stand_min: 120
quote.max_chars: 140
pause.max_h: 4
evening_sheet: 20:30
weekly_sitdown: Sunday 19:00
```

Changes: Default due time written as plain words in the settings block.
