# Smart Academic Scheduler

JavaFX desktop application for university routine scheduling. **No framework** – pure Java, JavaFX, OOP, file-based storage.

## Features

- **Auth**: Admin (hardcoded `admin` / `admin123`), Teacher (file-based login; accounts created by Admin only).
- **Departments**: Add/remove (initial: CSE, EEE, Civil, Law, Bangla, English, BBA, Education, Pharmacy, Agri, Journalism, History).
- **Batches**: Add batch with **courses created inline** (no separate “Add Course” screen). Program: HSC or Diploma.
- **Teachers**: Admin creates teacher account and sets login password. Teacher can view/update profile, set availability, view assigned routine only.
- **Rooms**: Theory/Lab, capacity, equipment (projector, sound; lab: PCs).
- **Merged classes**: Same teacher, room, time; max 4 batches; Admin selects manually.
- **Routine**: Fully automatic generation; conflict detection (teacher/room/time/credit/capacity); auto retry until conflict-free.
- **Output**: TableView with filter by Department/Batch/Teacher; export to print-ready file (print to PDF from OS).

## Time Rules

- **HSC**: Saturday–Tuesday. **Diploma**: Friday–Saturday.
- **Slots**: 9:30–11:00, 11:10–12:40, **12:40–2:00 (Lunch)**, 2:00–3:10, 3:30–5:00.
- **Theory**: 3 credit → 3 classes/week (auto). **Lab**: 1 credit → 1 class, 2 hours (auto).

## Data Storage

File-based only (no database):

- `data/teacher.txt`
- `data/batch.txt`
- `data/room.txt`
- `data/routine.txt`
- `data/department.txt`
- `data/merged.txt`

## Build & Run

**Requirements**: JDK 17+, JavaFX 21 (included via Maven). No need to install Maven – use the Maven Wrapper (`mvnw`).

**Git Bash / Linux / Mac:**
```bash
./mvnw clean compile
./mvnw javafx:run
```

**Windows CMD / PowerShell:**
```cmd
mvnw.cmd clean compile
mvnw.cmd javafx:run
```

Or run main class `com.scheduler.App` from VS Code (with Java extension).

## Default Login

- **Admin**: ID `admin`, Password `admin123`
- **Teacher**: Create via Admin → Teachers → Add Teacher (set ID and password).

testing the db in terminal
sqlite3 scheduler.db
.mode box <!--  /Turn on the clean table formatting: -->
SELECT * FROM departments;
SELECT * FROM rooms;
.quit