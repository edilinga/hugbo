# Hugbúnaðarverkefni 1
## Brief notes on project status, progress, exceptions

### Data model (current)
(M:1) = ManyToOne

| Entity | Fields | Constraints |
| :--- | :--- | :--- |
| **User** | id, ssn, email, password, createdAt | unique email, unique ssn |
| **ClassSession** | id, type, teacher, capacity, startAt, endAt, createdAt | — |
| **Booking** | id, user(M:1), classSession(M:1), createdAt | unique (user_id, class_session_id) |

---

## Endapunktar sem eru tilbúnir og prófaðir

| Method | Path | UC | Notes |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | UC7 | Body `{ ssn, email, password }` |
| `POST` | `/auth/login` | UC6 | Body `{ email, password }`; stillir uid í `HttpSession` |
| `POST` | `/auth/logout` | UC6 | Invalidates session |
| `GET` | `/me` | UC6 | Current user from session |
| `GET` | `/api/classes` | — | Admin list classes; Til að geta sótt alla tíma |
| `POST` | `/api/classes` | — | Admin create class; Til að geta búið til tíma fyrir notendur til að velja úr |
| `GET` | `/timetable` | UC4 | Read-only; inniheldur `freeSeats` |
| `GET` | `/minar-bokanir` | UC5 | Read-only; Krefst login |
| `POST` | `/bookings/{classId}` | UC1 | **Capacity** + **uniqueness** checks; Krefst login |
| `DELETE` | `/bookings/{classId}` | UC2 | Cancel booking; Krefst login |

---
## Árangur miðaður við áætlun
Allir endapunktar til og með 9. viku eru tilbúnir og prófaðir.
Allir endapunktar frá og með 10. viku eru enn ókláraðir.
Við erum semsagt nákvæmlega þar sem við ætluðum okkur að vera á þessum tíma.

| Week | Sprint | Use cases                                   | Status     |
|------|--------|----------------------------------------------|------------|
| 6    | 2      | UC6 Login, UC7 Register                      | ✅ Klárað    |
| 7    | 3      | UC4 Load timetable, UC5 My bookings          | ✅ Klárað    |
| 8    | 3      | UC1 Book, UC2 Cancel                         | ✅ Klárað    |
| 9    | 3      | UC8 Edit user, UC12 Security                 | ✅ Klárað    |
| 10   | 4      | UC3 Waitlist, UC9 Delete account             | ☐ Óklárað  |
| 11   | 4      | UC10 Filtered fetch, UC11 User-scoped fetch  | ☐ Óklárað  |
| 12   | 4      | UC13 Delete specific data, UC14 Pagination   | ☐ Óklárað  |


---

## Tilbúnu User casein og hvernig þau eru útfærð

* **UC6, UC7**: `UserController`, `UserService`, `UserRepository`, DTOs (`InnskraningRequest`, `NyskraningRequest`, `UserResponse`)
* **UC4**: `TimeTableController`, `ClassSessionRepository`, `BookingRepository`, DTO `ClassSessionResponse` (`freeSeats = capacity − count(bookings)`)
* **UC5**: `BookingController` (GET `/minar-bokanir`), DTO `BookingResponse`
* **UC1/UC2**: `POST`/`DELETE` `/bookings/{classId}` with capacity and uniqueness checks

---

## Villu meðhöndlun
409 þegar requestan er í lagi en í ósamræmi við núverandi state. M.ö.o. þegar notandi reynir að bóka sig í tíma sem er fullbókaður.

| Code | HTTP |
| :--- | :--- |
| `invalid_credentials` | 401 |
| `email_taken` | 400 |
| `ssn_taken` | 400 |
| `not_found` | 400 |
| `class_full` | 409 |
| `already_booked` | 409 |
| Missing session | 401 (Unauthorized) |

---

## Aukaatriði

* Lykilorð eru enn geymd sem plain-text í gagnagrunni. Höshum seinna.
* Allir prófanir fóru fram í gegnum Postman
* Serverside sessions eru það eina sem ber ábyrgð á innskráningu notanda