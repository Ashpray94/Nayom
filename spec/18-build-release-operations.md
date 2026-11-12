# 18. Build, release and operations

Owner role: release and operations. Inputs: 11, 12, 15.

## Repos and build

- One repo, `nayom`, with `spec/`, `android/`, `windows/`, `brain/`, `extension/`, `tools/` (12).
- Android: Gradle, Kotlin. Debug builds for testing. Release builds signed with your own key.
- Windows: Dart, built to a per-user installer, the same way Private Folder builds today.
- Extension: loaded unpacked in Chrome or Edge developer mode, because nothing goes through a store.
- Version numbers: three parts, for example 0.1.0. Each phase raises the middle number.
- Private Folder changes (DEP-1 to DEP-4) are released from the Private Folder repo before any Nayom release.

## Signing and keys

- The Android signing key is made once, on the gaming PC, and backed up to an encrypted USB drive and on paper.
- The vault quote key is separate (09). Its 24-word recovery phrase is on paper.
- Losing the signing key means a new install and fresh permissions. It does not affect the vault.

## Install, clean device

1. Install Private Folder, switch it to LAN-only, pair the phone, PC and laptop.
2. Move the vault into the shared folder on the home PC (DEP-4). Wait for the phone to show it.
3. Install Nayom Phone from the APK. Move the APK by Private Folder or USB.
4. In Android app info, allow restricted settings.
5. Run setup (F8): Accessibility, Notifications, Overlay, Usage, Microphone, Biometric, vault folder.
6. Set battery to Unrestricted for Nayom and Private Folder; turn off "put unused apps to sleep" for both; allow autostart.
7. Install Nayom Windows on the gaming PC and laptop. Run the firewall script once.
8. On the gaming PC, create the quote key. Scan its QR with the phone and the laptop. Write down the recovery phrase.
9. Load the browser extension on both PCs.
10. Check the heartbeat line once a minute from all three devices in `health.md`.
11. Review the blocklist screen (S-14) and add your own bank apps.

## Update

- Manual. Never automatic (SR-08).
- Copy the new APK or installer over, install on top, keep the vault.
- Before updating, if `schema` in `settings.md` will rise, the app makes a copy of `Nayom/` into `trash/_backup-<date>/`.

## Rollback

- Keep the previous APK and installer in a `releases/` folder outside the vault.
- To roll back: install the old build; if the schema rose, restore `_backup-<date>`.

## Backup

- The vault is already on two or three devices.
- Once a month, copy the `Nayom/` folder to an external drive. A reminder is put in your Promises by Nayom herself.
- Test a restore once, during Phase 6.

## Monitoring

- `Nayom/_system/health.md`: last heartbeat per device, dark time per day, model speed, battery cost estimate, sync delay, malformed files.
- If anything is red, the status notification shows it. There is no remote monitoring.

## Support and fixes

You are the only user. Issues go into a `docs/issues.md` in the repo. A fix is a new build, tested against the edge cases in 14 first.

## Uninstall

- Remove Nayom. The vault stays untouched.
- Optionally delete `Nayom/` from the vault. Nothing else needs undoing.
- Turn off the Accessibility service, Notification access and Overlay in Android settings.

## Release checklist

- [ ] All phase tests pass (17)
- [ ] Privacy tests pass on all devices
- [ ] Red edge cases tested by hand
- [ ] Manifest has no internet permission
- [ ] Firewall rule in place on both PCs
- [ ] Private Folder is on LAN-only
- [ ] Vault key and signing key backed up
- [ ] Previous build kept for rollback
- [ ] One restore test done

Changes: none yet.
