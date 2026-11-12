# Nayom: Product Spec

Nayom ("Naya Mom") is a private, on-device app for one person. She watches what you do and say on your phone and PCs, remembers what you agreed to, and holds you to it like a firm, fair second mother: no pity, no shaming, no rescuing.

This repo holds the spec only. The file set follows the usual product order, so any developer or agent can read it top to bottom.

> This repo must stay private. `01-vision-and-problem.md` describes the owner's personal struggles.

## Reading order

| # | File | What it settles |
| --- | --- | --- |
| 00 | [roles-and-process](00-roles-and-process.md) | The roles, in order, and what each hands over |
| 01 | [vision-and-problem](01-vision-and-problem.md) | Why Nayom exists, the problems, goals, non-goals |
| 02 | [personas](02-personas.md) | Who uses it, who is around them, a real day |
| 03 | [scope-and-features](03-scope-and-features.md) | Every feature with an ID and a pass/fail test |
| 04 | [information-architecture](04-information-architecture.md) | Screens, navigation, interrupt surfaces |
| 05 | [user-flows](05-user-flows.md) | Step-by-step flows, one straight line plus branches |
| 06 | [design-language](06-design-language.md) | Colours, type, spacing, components, motion |
| 07 | [brand-and-identity](07-brand-and-identity.md) | Name, character, voice, logo direction |
| 08 | [agent-rules-and-context](08-agent-rules-and-context.md) | How Nayom thinks, what she may and may not do |
| 09 | [vault-data-model](09-vault-data-model.md) | Obsidian folders, file formats, sync safety |
| 10 | [detection-signals](10-detection-signals.md) | Every slip signal, with its numbers |
| 11 | [privacy-safety-boundaries](11-privacy-safety-boundaries.md) | What she never reads, never stores, never does |
| 12 | [architecture-and-sync](12-architecture-and-sync.md) | Phone, Windows, PC brain, Private Folder |
| 13 | [copy-library](13-copy-library.md) | The actual words on screen |
| 14 | [edge-cases](14-edge-cases.md) | Trigger, gate, outcome for odd situations |
| 15 | [roadmap](15-roadmap.md) | Build order and done-when tests |
| 16 | [known-limits](16-known-limits.md) | What cannot be done, stated plainly |
| 17 | [test-plan](17-test-plan.md) | How every feature and every privacy rule is tested |
| 18 | [build-release-operations](18-build-release-operations.md) | Install, update, rollback, backup, checklist |

## Conventions

- Feature IDs look like `FR-10`. Safety rules look like `SR-03`. Settings keys look like `gate.pause_seconds`.
- All numbers marked "default" live in `10-detection-signals.md` and are copied into the vault file `Nayom/_system/settings.md`.
- The owner is called "you" in the spec. People around you are called Ravi, Meena and so on in examples. They are made up.
- Words used consistently: **promise** (something you agreed to), **ask** (something someone wants from you), **slip** (a promise or rule you broke), **mismatch** (what you said does not match what you did), **house rule** (a rule you wrote for yourself).

## Decisions made so far

| ID | Decision | Source |
| --- | --- | --- |
| D-01 | The Obsidian vault is the only database. Existing vault, taken under Nayom's control | Owner |
| D-02 | Raw screen and message text is never stored. Only extracted fields plus one short quote | Owner |
| D-03 | Voice: a firm, fair second mother. Facts first, then the expectation, then the next step. No pity | Owner |
| D-04 | V1 covers all four areas: promises, yes-gate, mismatch catcher, focus and self-care | Owner |
| D-05 | Interrupts are a full-screen card with a 5-second pause, then dismissable | Owner |
| D-06 | Slip alerts are always shown, with a one-tap Wrong button. Wrong taps train her | Owner |
| D-07 | People tiers are learned automatically; you tag where you want to override | Owner |
| D-08 | Languages: English and Hinglish | Owner |
| D-09 | Day shape (wake, work, sleep) is learned from device activity; you can correct it | Owner |
| D-10 | The stretch ladder is built by Nayom from situations you avoid | Owner |
| D-11 | Drafts only. Nayom never sends a message for you | Owner |
| D-12 | Never reads banks, finance, OTP, authenticator or password-manager apps | Owner |
| D-13 | Anything Nayom wrote, she may edit and delete. Your existing notes need your one-time review | Owner |
| D-14 | Anything you flag as off-reach is logged as time and app only. Everything else is readable | Owner |
| D-15 | Visual identity: warm, colourful, cheerful | Owner |
| D-16 | The spec lives in a private `nayom` repo, `/spec` folder | Owner |
| D-17 | Stack: native Kotlin on Android, Dart on Windows | Chosen by Claude on the owner's instruction "whichever fits best" |
| D-18 | Quotes are stored encrypted inside the vault file, so private chat text is never plain text in Obsidian | Chosen by Claude as the safest way to meet D-01 and D-02 |
| D-19 | Nayom holds you to your own promises and house rules. She does not set goals for you | Chosen by Claude to protect your autonomy |
| D-20 | Reminders fade as your reliability rises (scaffold fade) | Chosen by Claude, from the "teaches responsibility" requirement |
