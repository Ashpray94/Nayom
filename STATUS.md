# Status, 12 Nov 2026 (night)

## Done
- Spec 00-18 in `/spec`. D-21 added: app icon is a robot mum's face.
- `/reference`: runnable rules in plain Node (`cd reference && npm test`, 45 tests): signals 01-08 and 12, gate, escalation, scaffold fade, praise, day shape, ledger, vault one-writer rule, quote encryption, adoption, retention, model-output guard, copy lint of spec 13.
- Private Folder, branch `lan-only-mode` (not merged, not compiled): one switch blocks relay, update checks and internet peers. Default on.
- Icon: robot-mum, light and dark, plus Android adaptive layers and themed monochrome (sent as files).

## Decided (13 Nov)
Light is the default icon and app mode (D-23). UI palette is the teal family (D-22, tokens in `spec/tokens.json`). LAN-only is on by default (D-24).

## Needs you
- Nothing blocking. You have not read the spec yet, so Hinglish copy review waits until the screens exist; it will be reviewed on the screens, not in md.

## Spec points the reference had to decide (please confirm)
See `reference/README.md`: capture rule when there is no time phrase, confidence scoring, repeated-letter squeeze.

## Blocked here
- No Dart or Kotlin compiler in the sandbox (package downloads blocked). Anything for Private Folder or the Android app is uncompiled until run on your PC.

## Next, in order
1. Merge `lan-only-mode` after `flutter test` on your PC.
2. Nayom Phone shell (Android, Kotlin): vault read/write, Today screen, gate overlay. Port `/reference` tests as vectors.
3. Key screens in the teal palette (Today, gate card, slip card, evening sheet, setup).
