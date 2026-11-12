# 16. Known limits

Owner role: sceptic / red team. Inputs: all files. Plain statements of what will not work, and what to do about it.

## What the app cannot do

| Limit | Why | What we do instead |
| --- | --- | --- |
| It cannot stop the Send button | Android lets an app draw over another app, not block its buttons | The card appears when you pause typing; if you send faster, the yes is logged as a reflex yes |
| It cannot tell a lie from a mistake | It only sees that what you said does not match what it saw | The card says "doesn't match", never "lied". The Wrong button fixes false flags |
| It does not see work done off-device | Paper, a call, a whiteboard | Mismatch can fire wrongly; Wrong reasons teach it |
| It cannot hear calls or meetings | We chose not to record them | Debrief: you say what was agreed in 20 seconds |
| It cannot read blank or secure screens | The app marks them secure | Time and app only |
| It cannot read Devanagari | D-08 | Ignored; English and Hinglish only |
| It does not treat anxiety | It is an app, not a clinician | Daily stretch ladder, proof wall, 14416 on the menu |
| It cannot guarantee the PC is on | The PC may be off | Phone rules run alone and flags are marked provisional |

## What may not work yet

| Risk | Detail | Check |
| --- | --- | --- |
| One UI 9 behaviour | We have not tested Accessibility, overlays and battery rules on your exact build | Phase 0 test on the real phone |
| Private Folder is young | Its own docs say the full device matrix has not been tested and it is an unsigned build | Phase 0 fixes; test with real devices |
| Android background limits | Private Folder's foreground service is capped around six hours a day on Android 15 and later | DEP-2 uses WorkManager |
| Small phone model quality | A small on-device model will misread sarcasm and Hinglish | Rules first; Brain re-judges on the PC |
| Windows UI Automation | Some apps expose no text | OCR fallback for that app; costs more CPU |
| Weak laptop | May struggle with the reader | Window titles only (E-84) |
| Android blocking sideloaded Accessibility | "Allow restricted settings" is required on recent Android | Setup step F8 |
| Package names of blocked apps change | The seed list can go stale | Keyword blocking plus the review in S-14 |
| Battery under 5% a day | A target, not measured | Phase 1 measurement |
| Verdict delay | Sync and the model take time | Budget in 12 |

## What the design leaves risky (decided, not hidden)

- **Client content.** Unmarked client text on your screen can be read and stored as an encrypted quote (D-14). Marking Private is on you. A contract may forbid this; the choice is yours.
- **Other people's messages.** She reads them. They are encrypted and local, and quotes are deleted after 30 days.
- **Plain fields in the vault.** Names, times and app names are not encrypted. Use full-disk encryption on every device.
- **Existing notes under her control after one review (D-13).** Every later change keeps a snapshot for 30 days. A bad edit can be restored from `trash/`.
- **Reliance.** A firm mother voice can become something you lean on. Scaffold fade, the weekly nudge to tell a real person, and the support card exist for this.
- **Self-harm text.** The support rule uses phrase matching. It will miss some cases and fire on some harmless ones. It is a safety net, not a safeguard.

## What we chose not to do

- Auto-send messages (D-11).
- Auto-apply Private to anything.
- Install anything from the internet during use.
- Use any cloud model.
- Score you out of 100 or rank you.
- Shame, compare or threaten.

Changes: none yet.
