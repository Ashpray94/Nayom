# 05. User flows

Owner role: interaction designer. Inputs: 03, 04.

Rules for every flow here: one straight main line, branches hang off it, no screen without a connection, nothing loops back on itself for a single link. Retry and error moments are a toast (the existing Snackbar style from Private Folder, restyled with Nayom tokens).

---

## F1. A normal day (the main line)

```mermaid
flowchart LR
  A["Wake: Today shows 3 tasks + 1 care item"] --> B["Work"]
  B --> C["Ask arrives or promise typed"]
  C --> D["Gate or capture"]
  D --> E["Promise logged"]
  E --> F["Due time: done check"]
  F --> G["Evening sheet"]
  G --> H["Bedtime nudge"]

  B -.-> B1["Drift: focus card"]
  C -.-> C1["Call or meeting ends: debrief"]
  D -.-> D1["Reflex yes stopped: draft ready"]
  F -.-> F1["Missed: repair loop"]
  G -.-> G1["To approve: review queue"]
```

Real example: Surya wakes, sees Meena's file due 17:00. Ravi asks for a banner tweak. The gate stops a reflex yes. The file goes out at 16:50 and is marked kept. At 20:30 he sees the day's facts and approves one note edit.

---

## F2. Capture a promise (typed)

```mermaid
flowchart LR
  A["You type: I'll send it by Friday"] --> B["Reader sees the text in the chat app"]
  B --> C["Phone model extracts: who, what, when"]
  C --> D["Promise file written, quote encrypted"]
  D --> E["Small toast: Logged for Meena, Fri 18:00"]

  B -.-> B1["App is Private: only time and app logged"]
  C -.-> C1["Not sure it is a promise: written as a draft, shown in To approve"]
  C -.-> C2["No due time found: default time used, shown on toast"]
```

Real example: Surya types "I'll send the final by Friday" to Meena. A toast shows "Logged for Meena, Friday 18:00". If Friday 18:00 is wrong, he taps the toast and changes it.

---

## F3. The yes-gate

```mermaid
flowchart LR
  A["New ask seen: Ravi, banner, today"] --> B["You type: sure"]
  B --> C["Typing pause"]
  C --> D["Gate card, 5 s pause"]
  D --> E["You pick: Yes, Later, No"]
  E --> F["Draft shown, you send it"]
  F --> G["Promise or decline logged"]

  C -.-> C1["Load low and no rule hit: one-line card, no pause"]
  D -.-> D1["House rule hit: Yes button is off"]
  E -.-> E1["Yes: promise logged, what it pushes out shown"]
  E -.-> E2["Dismiss without choosing: counted as no decision"]
```

Gate card content, top to bottom: the ask in your own words (person, task, due), "Open and due in 48 hours: 3", the item it would push out, then three buttons.

Real example: "Meena's file is due today 17:00. Saying yes puts it at risk." He taps "Later". The draft reads: "Can do Friday morning, not today."

---

## F4. A promise comes due

```mermaid
flowchart LR
  A["2 h before: reminder"] --> B["Due time"]
  B --> C["Done check: look for evidence"]
  C --> D["Kept: proof wall entry"]

  C -.-> C1["No evidence at due + 30 min: slip card"]
  C1 --> C2["Repair loop: pick fix time or Release"]
  C2 --> C3["Fix time logged, one new reminder"]
  C2 -.-> C4["Release: draft to the person"]
```

Real example: the file for Meena has no export and no message by 17:30. Slip card: "File for Meena was due 17:00. Not sent." Surya picks "Fix by 18:30". Nayom reminds once at 18:00. If missed again, the second slip counts toward escalation (08).

---

## F5. Claim versus activity

```mermaid
flowchart LR
  A["You write: almost done"] --> B["Claim noted with project guess"]
  B --> C["PC checks related work in the last 4 h"]
  C --> D["Under 20 min: mismatch card"]
  D --> E["Draft: honest version with a real time"]
  E --> F["You send or you do the work"]

  C -.-> C1["20 min or more: nothing shown"]
  C -.-> C2["PC off: phone checks only phone activity, marked provisional"]
  D -.-> D1["Wrong tap: reason sheet, pattern learned"]
```

Real example: "Almost done with the banner" with zero minutes in the design tool today. Card: "You told Ravi almost done. No work on it today." Draft: "I haven't started it. I can have it Friday 11:00."

---

## F6. Avoidance to stretch ladder

```mermaid
flowchart LR
  A["Message read, not answered past its limit"] --> B["Added to the avoided list"]
  B --> C["Ladder built: smallest first"]
  C --> D["Today's rung shown"]
  D --> E["You do it"]
  E --> F["Evidence found: rung done"]
  F --> G["Proof wall entry, next rung unlocks after 3 done"]

  E -.-> E1["No evidence: marked unverified, no step-up"]
  D -.-> D1["Skipped 2 days: ladder drops one rung"]
```

Real example: a vendor's call has gone unanswered for 3 days. Rung 1: read the message again. Rung 2: type a reply and keep it. Rung 3: send it. Rung 4: call.

---

## F7. Evening sheet and review

```mermaid
flowchart LR
  A["20:30 notification"] --> B["Evening sheet: facts"]
  B --> C["Review queue: one proposal at a time"]
  C --> D["Approve or reject"]
  D --> E["Tomorrow's three tasks set"]

  B -.-> B1["Nothing happened: one line, no review"]
  C -.-> C1["Approve: note adopted, edit made"]
  C -.-> C2["Reject: not proposed again for that note for 30 days"]
```

---

## F8. First run and setup

```mermaid
flowchart LR
  A["Install"] --> B["Allow restricted settings"]
  B --> C["Accessibility on"]
  C --> D["Notification access"]
  D --> E["Display over other apps"]
  E --> F["Usage access"]
  F --> G["Link the vault folder"]
  G --> H["Heartbeat check passes: ready"]

  B -.-> B1["Sideloaded app: Android blocks Accessibility until this is allowed in App info"]
  C -.-> C1["Battery set to Unrestricted so the service stays alive"]
  G -.-> G1["Vault not yet in shared folder: setup stops, toast says where to move it"]
  H -.-> H1["Blocklist seed list checked against installed apps"]
```

---

## F9. Private and Pause

```mermaid
flowchart LR
  A["Tap Pause or Private"] --> B["Pause: choose 15 min, 1 h or 4 h"]
  B --> C["Reading stops"]
  C --> D["Timer ends: reading resumes"]
  D --> E["Pause logged as dark time"]

  A -.-> A1["Private: pick app, window, project, person or chat"]
  A1 -.-> A2["Only time and app name logged from then on"]
  C -.-> C1["Phone call and emergency dialer are never read anyway"]
```

---

## Retry and error moments

| Moment | What you see |
| --- | --- |
| Vault write failed | Toast: "Couldn't save. Trying again." Full-width icon-only button shows a loader, then returns to its start state |
| Sync behind | Small line on Today: "Phone is 1 h behind the PC" |
| Model slow | Card appears with facts only; the draft arrives a moment later |
| Reader stopped | Persistent notification turns berry: "Reading is off" with a Turn on button |
