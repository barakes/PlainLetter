# PlainLetter

An Android app that explains official Hebrew letters (municipal tax, parking tickets, National Insurance)
in plain language: what the letter says, what to do, and by when – with a quote from the letter next to every claim.

Personal details (ID numbers, phone numbers, emails, bank accounts) are hidden on the phone before anything
is sent, and the user approves exactly what leaves the device.

*Work in progress.*

## Structure

| Folder | What it holds |
|---|---|
| `android/` | The Android app – Kotlin, Jetpack Compose |
| `server/` | A small Kotlin + Spring Boot server that passes the hidden text to the AI model (coming soon) |
