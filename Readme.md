

# RecipeVault

**RecipeVault** is a modern Android app for managing personal recipes with **secure user-based storage, cloud backup, and offline-first architecture**.  
It is built using **Kotlin, Clean Architecture, Room, Firebase, and WorkManager**.

The app demonstrates **real-world Android development practices**, including authentication, per-user databases, background sync, and flexible data backup strategies.

---

## ✨ Features

- User authentication using **Firebase Auth (Email/Password)**
- Per-user local database (**Room**) for complete data isolation
- Add, edit, and delete recipes
- Attach images from gallery
- Offline-first architecture
- Cloud backup using **Firestore**
- Manual backup using system file picker (e.g., Google Drive)
- Share backup via Android share sheet
- Automatic periodic backup using **WorkManager**
- Clean and intuitive UI

---

## 🧠 Architecture

The app follows **Clean Architecture** with clear separation of concerns.

```
presentation
│
├── activities / fragments
├── viewmodels
└── adapters

domain
│
├── models
├── repositories
└── usecases

data
│
├── local (Room)
├── remote (Firebase)
└── repository
```

---

## 🔄 Data Flow

```
UI → ViewModel → UseCase → Repository → Room / Firebase
```

- Local-first (Room)
- Cloud sync via Firestore
- Background sync via WorkManager

---

## 📦 Tech Stack

- **Kotlin**
- **Coroutines & Flow**
- **Room Database**
- **Firebase Authentication**
- **Firebase Firestore**
- **WorkManager**
- **ViewBinding**
- **Material Design Components**

---

## ☁️ Backup System

RecipeVault supports multiple backup strategies:

- **Cloud Backup**
  - Uploads JSON to Firestore  
  - Path: `users/<uid>/backups/latest`

- **Local Backup**
  - Saves JSON via system file picker (e.g., Google Drive)

- **Share Backup**
  - Share JSON via Gmail, Drive, etc.

- **Auto Backup**
  - Runs every 6 hours using WorkManager

---

## 🔐 Firebase Setup

1. Create a project in **Firebase Console**
2. Add Android app:

```
Package name: com.flash.recipes
```

3. Download:

```
google-services.json
```

Replace:

```
app/google-services.json
```

---

### Enable in Firebase Console

- **Authentication**
  - Enable → Email/Password

- **Firestore Database**
  - Create database (test or production mode)

---

## 📱 Screens

### Login
Secure authentication using Firebase.

### Recipe List
View and manage recipes with optional images.

### Backup Options
Upload to cloud, save locally, or share.

---

## ⚡ Key Implementations

### Per-user Database

Each user has a separate Room database:

```
recipe_db_<uid>
```

Ensures complete data isolation.

---

### WorkManager Sync

- Periodic background backup every 6 hours
- Only runs when user is logged in

---

### Offline-first Design

- Works fully offline
- Syncs when network is available

---

## 🛠 Project Goals

This project demonstrates:

- Firebase integration in Android apps
- Offline-first architecture
- Background work using WorkManager
- Clean Architecture best practices
- Scalable and maintainable code

---

## 🚀 Future Improvements

- Real-time sync
- Multi-device sync conflict handling
- Image upload to cloud storage
- Push notifications for reminders
- Compose UI migration

---

## 👨‍💻 Author

**Sridhar Prasath**

Android / Flutter / iOS Developer

GitHub:  
https://github.com/sridharprasath94

---

## 📄 License

This project is licensed under the MIT License.