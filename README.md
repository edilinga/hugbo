# Hugbúnaðarverkefni 1

## Brief notes on project status, progress, exceptions

### Data model (current)

(M:1) = ManyToOne

| Entity           | Fields                                                 | Constraints                        |
| :--------------- | :----------------------------------------------------- | :--------------------------------- |
| **User**         | id, ssn, email, password, createdAt                    | unique email, unique ssn           |
| **ClassSession** | id, type, teacher, capacity, startAt, endAt, createdAt | —                                  |
| **Booking**      | id, user(M:1), classSession(M:1), createdAt            | unique (user_id, class_session_id) |

---

## Endapunktar sem eru tilbúnir og prófaðir

| Method   | Path                       | UC       | Notes                                                                                                    |
| :------- | :------------------------- | :------- | :------------------------------------------------------------------------------------------------------- |
| `POST`   | `/auth/register`           | UC7      | Body `{ ssn, email, password }`                                                                          |
| `POST`   | `/auth/login`              | UC6      | Body `{ email, password }`; stillir uid í `HttpSession`                                                  |
| `POST`   | `/auth/logout`             | UC6      | Invalidates session                                                                                      |
| `GET`    | `/me`                      | UC6      | Current user from session                                                                                |
| `GET`    | `/api/classes`             | —        | Admin list classes; Til að geta sótt alla tíma                                                           |
| `POST`   | `/api/classes`             | —        | Admin create class; Til að geta búið til tíma fyrir notendur til að velja úr                             |
| `GET`    | `/timetable`               | UC4      | Read-only; inniheldur `freeSeats`                                                                        |
| `GET`    | `/minar-bokanir`           | UC5      | Read-only; Krefst login                                                                                  |
| `POST`   | `/bookings/{classId}`      | UC1      | **Capacity** + **uniqueness** checks; Krefst login                                                       |
| `DELETE` | `/bookings/{classId}`      | UC2      | Cancel booking; Krefst login                                                                             |
| `GET`    | `/classes`                 | **UC10** | Síun með `days` _eða_ `from/to` (ISO-8601), **pagination** (`page`,`size`) og **sorting** (`sort`,`dir`) |
| `GET`    | `/users/{userId}/bookings` | **UC11** | Notanda-tengd gögn með **síu**, **pagination**, **sorting**; krefst að `userId == session uid`           |
| `DELETE` | `/bookings/id/{bookingId}` | **UC13** | Eyðir tiltekinni bókun; aðeins eigandi; skilar `204`                                                     |

### Authentication gates (UC12)

- Verndað með `HttpSession` interceptor: `"/bookings/**"`, `"/minar-bokanir"`, `"/users/*/bookings"`.
- Opinir endapunktar: `/health`, `/timetable`, `/classes`, `/auth/*`, `/api/classes` (admin demo).

---

## Árangur miðaður við áætlun

Öll UC kláruð á ásettum tíma.

| Week | Sprint | Use cases                                   | Status            |
| ---- | ------ | ------------------------------------------- | ----------------- |
| 6    | 2      | UC6 Login, UC7 Register                     | ✅ Klárað         |
| 7    | 3      | UC4 Load timetable, UC5 My bookings         | ✅ Klárað         |
| 8    | 3      | UC1 Book, UC2 Cancel                        | ✅ Klárað         |
| 9    | 3      | UC8 Edit user, UC12 Security                | ✅ Klárað         |
| 10   | 4      | UC3 Waitlist, UC9 Delete account            | ✅ Klárað         |
| 11   | 4      | UC10 Filtered fetch, UC11 User-scoped fetch | ✅ Klárað         |
| 12   | 4      | UC13 Delete specific data, UC14 Pagination  | ✅ UC13 · ✅ UC14 |

> Ath: UC14 (pagination) er **uppfyllt í reynd** með UC10 og UC11 þar sem bæði veita síu + pagination + sorting með `PagedResponse<T>`.

---

## Tilbúnu Use case-in og hvernig þau eru útfærð

- **UC6, UC7**: `UserController`, `UserService`, `UserRepository`, DTOs (`InnskraningRequest`, `NyskraningRequest`, `UserResponse`)
- **UC4**: `TimeTableController`, `ClassSessionRepository`, `BookingRepository`, DTO `ClassSessionResponse` (`freeSeats = capacity − count(bookings)`)
- **UC5**: `BookingController` (`GET /minar-bokanir`), DTO `BookingResponse`
- **UC1/UC2**: `POST`/`DELETE` `/bookings/{classId}` með **capacity** og **uniqueness** vörn
- **UC10**: `FilteredClassesController` (`GET /classes`) + `PagedResponse<T>` + **síur/pagination/sorting**
- **UC11**: `UserBookingsController` (`GET /users/{userId}/bookings`) með **eigendavörn** og **síu/pagination/sorting**
- **UC12**: `AuthInterceptor` + `WebConfig` til að krefjast innskráningar á viðkvæmum slóðum
- **UC13**: `BookingController.deleteById` (`DELETE /bookings/id/{bookingId}`) + `BookingService.deleteOwned`

---

## Villu meðhöndlun

409 þegar requestan er í lagi en í ósamræmi við state (t.d. `class_full`).

| Code                                                                                   | HTTP               |
| :------------------------------------------------------------------------------------- | :----------------- |
| `invalid_credentials`                                                                  | 401                |
| `email_taken`                                                                          | 400                |
| `ssn_taken`                                                                            | 400                |
| `forbidden`                                                                            | **403**            |
| `not_found`                                                                            | **404**            |
| `class_full`                                                                           | 409                |
| `already_booked`                                                                       | 409                |
| `bad_page`, `bad_size`, `bad_sort`, `bad_days`, `bad_datetime`, `bad_range`, `bad_dir` | 400                |
| Missing session                                                                        | 401 (Unauthorized) |

---

## Aukaatriði

- Lykilorð eru enn geymd sem plain-text í gagnagrunni. Höshum seinna.
- Allar prófanir fóru fram í gegnum Postman.
- Serverside sessions sjá um auðkenningu; engin JWT/CSRF í þessu demo.
