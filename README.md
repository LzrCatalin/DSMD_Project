# ReadingCorner

A Goodreads-inspired Android app built as a final project for the DSMD course at the University of Bucharest.

Search for books, track your reading progress, discuss in per-book forums, join reading clubs with live chat, and earn **bookstars** reputation on your profile.

---

## Features

- **Authentication** — Sign up / Log in with email and password (Firebase Auth)
- **Book Search** — Search millions of books via the Google Books API
- **My Shelf** — Save books to *To Read*, *Reading*, or *Read*; rate them locally
- **Home Feed** — See what you're currently reading, your want-to-read list, and personalised recommendations
- **Book Detail** — Full description, cover, rating, and forum threads per book
- **Forums** — Create and chat in per-book discussion threads
- **Reading Clubs** — Create or join clubs, live group chat, see members' shelves
- **Bookstars** — Reputation system earned through reading and social activity
- **Profile** — Username, tier badge with progress bar, shelf stats, and earning guide

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.4 |
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| Auth | Firebase Authentication |
| Cloud DB | Firebase Firestore (real-time) |
| Local DB | Room |
| Preferences | DataStore |
| Networking | Retrofit + Gson + OkHttp |
| Images | Coil |
| Build | AGP 9.2.1 · Gradle 9.4.1 · KSP |

---

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- Android SDK API 24+
- A Firebase project with **Authentication** (Email/Password) and **Firestore** enabled

### Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd DSMD_Project
   ```

2. **Add `google-services.json`**  
   Download it from the [Firebase Console](https://console.firebase.google.com) and place it in the `app/` directory.  
   *(This file is gitignored — every developer needs their own copy.)*

3. **Set Firestore security rules**  
   In Firebase Console → Firestore → Rules, publish:
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{uid} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && request.auth.uid == uid;
         match /shelf/{bookId} {
           allow read: if request.auth != null;
           allow write: if request.auth != null && request.auth.uid == uid;
         }
       }
       match /forums/{bookId}/threads/{threadId} {
         allow read, write: if request.auth != null;
         match /posts/{postId} {
           allow read, write: if request.auth != null;
         }
       }
       match /clubs/{clubId} {
         allow read, write: if request.auth != null;
         match /messages/{msgId} {
           allow read, write: if request.auth != null;
         }
       }
     }
   }
   ```

4. **(Optional) Google Books API Key**  
   Without a key, search works but has a low daily quota. To add one:
   - Google Cloud Console → enable **Books API** → create an API key
   - Paste it into `BooksApiConfig.API_KEY` in `app/src/main/java/com/example/readingcorner/data/remote/NetworkModule.kt`

5. **Run the app**  
   Open the project in Android Studio → **Sync Now** → select an emulator or device → **▶ Run**

### CLI Build

```bash
./gradlew :app:assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Project Structure

```
app/src/main/java/com/example/readingcorner/
├── data/
│   ├── local/          # Room database, ShelfBook entity, DAO
│   ├── prefs/          # DataStore preferences
│   ├── remote/         # Retrofit, Google Books API, DTOs
│   └── repository/     # BookRepository, ShelfRepository, SocialRepository, UserRepository
├── navigation/         # AppNavigation (top-level routes)
└── ui/
    ├── auth/           # Login & Sign Up screens + AuthViewModel
    ├── clubs/          # Clubs list + Club detail (3-tab layout)
    ├── components/     # BookRow, BookCover, RatingBar (reusable)
    ├── detail/         # Book detail screen
    ├── forum/          # Forum thread screen
    ├── home/           # Home feed screen
    ├── main/           # MainScreen (bottom navigation host)
    ├── mybooks/        # My Books screen (tabbed by status)
    ├── profile/        # Profile screen
    ├── search/         # Search screen
    └── theme/          # Compose theme
```

---

## Bookstars System

Bookstars are earned through reading and social activity:

| Action | Points |
|---|---|
| Finish a book (Read) | +10 |
| Currently reading a book | +3 |
| Add a book to To Read | +1 |
| Join a reading club | +15 |
| Create a forum thread | +5 |
| Post in a forum | +3 |

| Tier | Name | Required |
|---|---|---|
| ★☆☆☆☆ | Beginner | 0+ |
| ★★☆☆☆ | Reader | 20+ |
| ★★★☆☆ | Bookworm | 60+ |
| ★★★★☆ | Avid Reader | 120+ |
| ★★★★★ | Scholar | 200+ |

