# EduConnect Firebase setup

1. Create a Firebase project.
2. Add Android app package:
   `com.edutechconnect.app`
3. Download `google-services.json` and place it in:
   `app/google-services.json`
4. Enable:
   - Authentication (Email/Password or Google)
   - Cloud Firestore
   - Storage
   - Cloud Messaging
5. Publish `firestore.rules`.
6. Create an `admins` Firestore collection. For each authorized admin, create a document whose ID is that user's Firebase Auth UID.
7. Build the app.

Important: do not commit `google-services.json` to a public repository if your project policy requires keeping Firebase configuration private.
