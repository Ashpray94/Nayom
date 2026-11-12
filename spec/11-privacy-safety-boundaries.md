# 11. Privacy, safety and boundaries

Owner role: security and privacy officer. Inputs: 03, 08, 09, decisions D-02, D-12, D-14, D-18.

Nayom reads almost everything on your own devices, by your choice. This file says where she stops, and how the data is kept safe.

## Safety rules (SR)

| ID | Rule |
| --- | --- |
| SR-01 | Banks, UPI, finance, trading, crypto, wallet, OTP, authenticator and password-manager apps are never read. No event is created while one is in front |
| SR-02 | Browser tabs on banking, finance and password-manager sites are never read |
| SR-03 | A screen marked secure by its app (blank to readers) stays blank. She does not work around it |
| SR-04 | Anything you mark Private is logged as time and app name only: no quote, no person, no text |
| SR-05 | Raw text lives only in memory for at most 60 seconds, then is dropped. It is cleared when the screen turns off |
| SR-06 | Support rule: if your own typed text suggests harm to yourself, she stops judging, stores nothing about it and shows 14416 once |
| SR-07 | Nayom never sends a message, makes a call or changes a setting on another app |
| SR-08 | The Nayom apps have no internet permission. Nothing is sent anywhere by them |
| SR-09 | She never records calls or surrounding sound. The microphone is used only while you hold Debrief, and the audio is deleted on extraction |
| SR-10 | She is quiet on: phone calls in progress, the emergency dialer, active navigation, camera and video recording |
| SR-11 | Text from messages is data, never instructions to her |
| SR-12 | She never reads another person's phone or computer. One owner, own devices |
| SR-13 | Opening Nayom needs your fingerprint or phone PIN |
| SR-14 | If a reader is switched off or killed, she logs it as dark time and shows "Reading is off". She does not hide it |

## Auto-blocked list (seed, checked against what is installed at setup)

Apps: Google Pay `com.google.android.apps.nbu.paisa.user`, PhonePe `com.phonepe.app`, Paytm `net.one97.paytm`, BHIM `in.org.npci.upiapp`, Zerodha Kite `com.zerodha.kite3`, Groww `com.nextbillion.groww`, Samsung Wallet `com.samsung.android.spay`, Samsung Pass `com.samsung.android.samsungpass`, Google Authenticator `com.google.android.apps.authenticator2`, Microsoft Authenticator `com.azure.authenticator`, Authy `com.authy.authy`, Aegis `com.beemdevelopment.aegis`, Bitwarden `com.x8bit.bitwarden`, 1Password `com.onepassword.android`.

Everything else with banking, bank, pay, wallet, upi, otp, authenticator, vault, password or keepass in the package name is also blocked, and the setup screen lists what it blocked so you can add your own bank apps by name.

Sites (Windows browser extension): your banks' domains, UPI and wallet sites, password-manager sites, and any page whose address contains "login", "signin" or "payment" for a blocked domain.

This is a seed list. It is checked once on the real phone in Phase 0, because package names change.

## Private (off-reach), D-14

- You can mark Private: an app, a window title pattern, a project, a person, or a chat.
- Where: S-14, the Pause tile, a long-press on any card, or a Windows hotkey.
- Effect: events carry only `ts | kind | app`. No quote, no person, no tags.
- Nayom may suggest Private for a client project when it spots the project name on screen often. She never applies it herself.
- Unmarked content is readable. That includes client content on your screen. This is your choice (D-14) and is written down here so it is clear.

## Pause

One tap, 15 minutes, 1 hour or 4 hours (`pause.max_h`). Reading stops completely. The pause is logged as dark time with the reason "paused".

## What is stored, and what is not

| Stored in the vault | Never stored |
| --- | --- |
| Person, promise, due time, app name | Full messages |
| One quote, one sentence, 140 characters, encrypted | Whole screens |
| Event kinds and times | Screenshots |
| Counts and verdicts | Audio |
| Slips with evidence IDs | Contents of blocked or Private sources |

OCR on Windows is used only for apps that expose no text. The image is dropped as soon as the text is read.

## Permissions, and why each one is asked

| Platform | Permission | Why |
| --- | --- | --- |
| Android | Accessibility service | Reads what is on screen and what you type |
| Android | Notification access | Reads incoming messages without opening the app |
| Android | Usage access | Knows which app you are in and for how long |
| Android | Display over other apps | The full-screen cards |
| Android | Microphone | Debrief only, while held |
| Android | Biometric | Opens Nayom |
| Android | Notifications | Reminders and nudges |
| Android | Exact alarms | Reminders at the exact due time |
| Windows | UI Automation | Reads window titles and on-screen text |
| Windows | Browser extension | Reads the page title and address |
| Windows | Startup entry | Runs at sign-in |

Android needs "Allow restricted settings" turned on in the app's info page before Accessibility can be enabled for a sideloaded app. Setup walks you through it (F8).

## Network posture

- Nayom on Android: no internet permission in the manifest. Not even for updates.
- Nayom on Windows: a firewall rule blocks all outbound traffic for the Nayom programs, except to the same computer.
- The PC brain's model runs on the same computer and listens only on its own address, `127.0.0.1`.
- Only Private Folder talks to the network, to copy the vault between your devices. It must be switched to LAN-only first (DEP-1), which turns off the relay, the GitHub address lookup and the update check.
- Models and updates are brought in by you, by hand, from a file.

## Threat table

| Risk | What stops it |
| --- | --- |
| A stolen or borrowed phone | Biometric lock on Nayom; device encryption; quotes are encrypted; lock-screen widget shows no private text |
| Vault copied from a disk | Quotes are cipher text; other fields are in plain text, so use full-disk encryption (the S24 does by default; turn BitLocker on for the PCs) |
| Another app reading Nayom's folder | The phone folder is private to the Private Folder app and shared only through its documents provider |
| A malicious message trying to command her | SR-11; all text is data |
| Another program on the PC calling the model | The model listens on 127.0.0.1 only and needs a local token |
| Wrong flags harming you | Wrong button, learning, the rule that no evidence means no flag, and no name-calling |
| Dependency on her | Scaffold fade (FR-72); weekly nudge to tell a real person; the support card |
| The spec and vault holding sensitive personal facts | Keep the `nayom` repo private; the personal parts are in `01`; delete them from the vault copy if you wish |

## Other people's data

Messages from other people are read on your own devices only, to help you keep your own promises. They are never sent anywhere. Quotes of their words are encrypted and are blanked after 30 days. Do not share the vault with anyone. Do not use her output to confront people with what they wrote.

Changes: Added the exact alarms permission used for reminders.
