# 09. Vault data model

Owner role: data architect. Inputs: 03, 08, decisions D-01, D-02, D-13, D-18.

The Obsidian vault is the only database. It lives inside the Private Folder shared folder, so it exists on the home PC and the phone (DEP-3, DEP-4). Every record is a Markdown file or a line in one.

## Folder layout

All Nayom files live under one folder, `Nayom/`, at the top of the vault.

```
<vault>/
  (your own notes, untouched until adopted)
  Nayom/
    _system/
      settings.md      numbers from 10
      rules.md         your house rules
      blocklist.md     auto-blocked apps and sites, plus your additions
      offreach.md      what you flagged Private
      learning.md      Wrong patterns and the quiet list
      dayshape.md      learned wake, work, sleep
      devices.md       list of your devices and their roles
      health.md        reader heartbeat summary, dark time
    people/            one file per person
    promises/          one file per promise, written once
    asks/              one file per ask, written once
    ledger/<dev>/      append-only status lines, one file per device per day
    events/<dev>/      append-only event lines, one file per device per day
    slips/             one file per slip or mismatch
    verdicts/          written only by the PC brain, one file per day
    debriefs/          one file per debrief
    days/              one file per day (facts, counts)
    stretch/           ladder.md and one log file per day
    proof/             one file per week
    proposals/
      pending/  approved/  rejected/
    trash/             anything Nayom removed, kept 30 days
```

Device codes: `ph` phone, `pc` gaming PC, `lt` laptop.

## Who may write what

One writer per file. This is what stops sync conflicts.

| Path | Written by |
| --- | --- |
| `events/ph/`, `ledger/ph/` | Phone only |
| `events/pc/`, `ledger/pc/` | Gaming PC only |
| `events/lt/`, `ledger/lt/` | Laptop only |
| `verdicts/`, `days/`, `proof/` | PC brain only |
| `promises/`, `asks/`, `debriefs/` | The device that captured it, once, never edited after |
| `slips/` | The PC brain (or the phone, marked provisional, replaced by a PC verdict file with the same ID) |
| `people/` | The PC brain; your tier overrides go in `people/<name>.md` under an `override:` property, written by whichever device you tapped on, one edit at a time |
| `_system/settings.md`, `rules.md`, `offreach.md` | The device you edit on; a conflict keeps both copies and shows a banner |
| `proposals/` | The PC brain creates; the device you approve on moves it |

Current status of a promise = the latest line for its ID across all `ledger/*` files. Every device recomputes it.

## IDs and file names

- Format: `<kind>-<YYYYMMDD>-<HHMMSS>-<dev>-<nn>`, for example `P-20261112-091530-ph-01`.
- Kind letters: P promise, A ask, S slip, D debrief, R proposal.
- A person file is the lowercase name, for example `meena.md`. Two people with one name get `meena-2.md`.

## File formats

All properties are valid YAML so Obsidian shows them in its Properties panel. Links use `[[wikilinks]]`.

### Promise `promises/P-….md`

```yaml
---
nayom: promise
id: P-20261112-091530-ph-01
person: "[[meena]]"
what: "Send the final file"
due: 2026-11-12T17:00:00+05:30
due_source: stated        # stated | default
created: 2026-11-10T16:12:05+05:30
source_app: com.whatsapp
quote_enc: "v1:..."        # encrypted, see below
language: en               # en | hinglish
confidence: 0.92
private: false
---
```

Body is empty. Obsidian shows it as a properties-only note.

### Ask `asks/A-….md`

Same as a promise, with `nayom: ask`, `from:` instead of `person:`, and `decision:` filled later as a ledger line (`yes`, `later`, `no`, `none`).

### Ledger line `ledger/<dev>/2026-11-12.md`

One line per change, in a code block so Obsidian shows it as plain text:

```
ts | id | status | evidence | by | new_due
2026-11-12T16:50:11+05:30 | P-20261110-161205-ph-01 | kept | E-ph-20261112-1649-07,E-pc-20261112-1648-22 | pc |
```

Status values: `open`, `moved`, `kept`, `missed`, `released`, `fixed-by`. `new_due` is filled only for `moved` and `fixed-by` lines and is empty otherwise.

### Event line `events/<dev>/2026-11-12.md`

```
id | ts | kind | app | person | project | tags | quote_enc
E-ph-20261112-1649-07 | 2026-11-12T16:49:44+05:30 | sent | com.whatsapp | meena | banner | attachment | v1:...
```

Event kinds: `typed`, `sent`, `received`, `read`, `deleted-draft`, `open`, `close`, `idle`, `screen-on`, `screen-off`, `call-missed`, `call-returned`, `file-saved`, `file-exported`, `heartbeat-gap`, `pause`.
For Private sources only `ts | kind | app` are written.

### Person `people/meena.md`

```yaml
---
nayom: person
name: Meena
tier: 1                 # learned
override: null          # you set 1, 2 or 3 here
tier_reason: "client, deadlines, money words"
open_promises: 2
avoided_count: 0
private: false
---
```

### Slip `slips/S-….md`

```yaml
---
nayom: slip
id: S-20261112-141022-pc-01
type: mismatch          # missed | mismatch | clash | avoidance | reflex-yes | drift | unseen
level: 1
person: "[[ravi]]"
related: ["[[A-20261112-091530-ph-01]]"]
evidence: [E-ph-20261112-1405-03, E-pc-20261112-1000-11]
state: open             # open | fixed | released | wrong
fix_by: null
provisional: false
---
```

### Day `days/2026-11-12.md`
Counts of promises due, kept, missed, moved; asks gated, reflex yeses stopped; mismatches; dark minutes; care item and whether you tapped it; stretch rung and result. Facts only.

### Stretch `stretch/ladder.md`
Numbered rungs, each with its source (the avoided item), state and evidence.

### Proof `proof/2026-W46.md`
List of kept items for the week, each with its evidence IDs, and the one-line count ("Kept 9 of 11").

### Proposal `proposals/pending/R-….md`

```yaml
---
nayom: proposal
id: R-20261112-203000-pc-01
target: "[[Some existing note]]"
change: append            # append | edit | create-link | move
state: pending
---
```
Body: the exact text she wants to add or change, shown as a diff.

### House rule in `rules.md`

```
- id: HR-01
  when: tier>=2 and overdue>=2
  do: disable-yes
  on: true
```

## Your existing notes: adoption (D-13)

1. Nayom may read the whole vault except Private paths.
2. She never edits an existing note without a proposal.
3. You review a proposal once. On approval the note gets the property `nayom: adopted`, and from then on she may edit that note on her own.
4. Every change to an adopted note first saves a snapshot to `Nayom/trash/` for 30 days.
5. Deleting a whole adopted note still needs your tap.
6. Notes Nayom created herself she may edit and delete freely; deletions still go to `trash/` first.

## Quote encryption (D-18)

- `quote_enc` is AES-256-GCM. Format: `v1:` + base64(12-byte nonce + ciphertext + tag).
- Maximum quote: one sentence, 140 characters.
- The key is created on the gaming PC, shown as a QR, and scanned by the phone and laptop. On the phone it is stored in the Android Keystore; on Windows it is protected by the Windows data-protection API.
- A recovery phrase of 24 words is shown once. You keep it on paper.
- If the key is lost the quotes cannot be read. All other fields still work.
- Obsidian shows the cipher text, never the chat words.

## Retention (defaults)

| Data | Kept |
| --- | --- |
| Event lines | 30 days, then reduced to daily counts in `days/` |
| Quotes | 30 days, then blanked, unless the item is an open slip or on the proof wall |
| Asks | 90 days |
| Promises, slips, proof | Forever |
| Trash | 30 days |
| Debrief audio | Deleted the moment text is extracted; never in the vault |

## Schema version

`Nayom/_system/settings.md` has `schema: 1`. Any change to a file format raises it, and a migration step runs before the apps start.

Changes: Ledger line gained a `new_due` column.
