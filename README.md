# Simple Interest Calculator (Firebase)

Android app (Java) for COM 4137 Mobile Systems Programming. It reads a principal, rate and time, calculates simple interest (P x R x T / 100), displays it, then saves the record to Firebase Realtime Database.

## Assumption
Loans longer than 10 years get a 0.12 percentage-point premium on the rate, applied to the whole loan period.

## Running it
This repo does not include `google-services.json`. To run the app:
1. Create a Firebase project with a Realtime Database.
2. Register an Android app with the package name `com.danielk.simpleinterest`.
3. Download `google-services.json` into the `app/` folder.
4. Open the project in Android Studio and run.
