# Status, 12 Nov 2026 (night)

## Done
- Spec 00-18 in `/spec`. D-21 added: app icon is a robot mum's face.
- `/reference`: runnable rules in plain Node (`cd reference && npm test`, 45 tests): signals 01-08 and 12, gate, escalation, scaffold fade, praise, day shape, ledger, vault one-writer rule, quote encryption, adoption, retention, model-output guard, copy lint of spec 13.
- Private Folder, branch `lan-only-mode` (not merged, not compiled): one switch blocks relay, update checks and internet peers. Default on.
- Icon: robot-mum, light and dark, plus Android adaptive layers and themed monochrome (sent as files).

## Needs you
1. Icon: light or dark as the main one.
2. Palette: the icon is blue-tinted teal; the UI spec is marigold and cream. Keep marigold in the app, or move the UI to the teal family?
3. LAN-only default on (as built) or off?
4. Hinglish copy in spec 13 needs your read.

## Spec points the reference had to decide (please confirm)
See `reference/README.md`: capture rule when there is no time phrase, confidence scoring, repeated-letter squeeze.

## Blocked here
- No Dart or Kotlin compiler in the sandbox (package downloads blocked). Anything for Private Folder or the Android app is uncompiled until run on your PC.

## Next, in order
1. Merge `lan-only-mode` after `flutter test` on your PC.
2. Nayom Phone shell (Android, Kotlin): vault read/write, Today screen, gate overlay. Port `/reference` tests as vectors.
3. Key screens in design once palette is decided.
