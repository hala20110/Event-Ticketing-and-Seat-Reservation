# Event Ticketing & Seat Reservation API 🎟️
 
A robust, concurrency-safe backend for an event ticketing platform. Built with Spring Boot, it allows organizers to create events, customers to reserve and pay for seats, and includes automatic background cleanup for abandoned reservations.
 
##  Features
 
* **Role-Based Access Control (RBAC):** JWT authentication with `CUSTOMER`, `ORGANIZER`, and `ADMIN` roles.
* **Event Management:** Organizers can create venues and events. The system automatically generates a unique seat map (tickets) for each event.
* **Reservation Lifecycle:** Customers can reserve multiple seats simultaneously. Reservations follow a strict state machine: `PENDING` → `CONFIRMED` or `CANCELLED` or `EXPIRED`.
* **Concurrency Control (Double-Booking Prevention):** Utilizes **Pessimistic Locking** (`SELECT ... FOR UPDATE`) at the database level to ensure that two users can never book the same seat at the same time.
* **Simulated Payment Gateway:** A simulated payment endpoint that confirms reservations and permanently books seats upon success, or releases them upon failure.
* **Automated Expiry Job:** A Spring `@Scheduled` background task runs every 30 seconds to automatically expire unpaid `PENDING` reservations and release their held seats back to the public pool.
* **Swagger/OpenAPI:** Fully documented interactive API UI with JWT Bearer token authorization built-in.
* **Global Exception Handling:** Consistent JSON error responses for validation failures, business logic conflicts, and security exceptions.

##  Tech Stack
 
* **Language:** Java 21
* **Framework:** Spring Boot 3.3.5
* **Security:** Spring Security + JJWT (JSON Web Tokens)
* **Database:** PostgreSQL 16
* **ORM:** Spring Data JPA / Hibernate
* **Documentation:** SpringDoc OpenAPI (Swagger UI)
* **Utilities:** Lombok, MapStruct
  
##  Architectural Highlights
 
### 1. The Concurrency Lock (The Core Challenge)
To prevent double-booking, the naive approach is to read a seat's status, check if it's `AVAILABLE`, and then update it. This creates a Time-of-Check to Time-of-Use (TOCTOU) race condition.
 
This project solves this in `SeatRepository` using **Pessimistic Write Locking**:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.event.id = :eventId")
List<Seat> findAllByIdInAndEventIdForUpdate(@Param("ids") List<Long> ids, @Param("eventId") Long eventId);
```
 
When a customer attempts to reserve a seat, Hibernate issues a `FOR UPDATE` SQL statement. If a second customer tries to reserve the same seat concurrently, their transaction blocks at the database level. Once the first transaction completes, the second transaction resumes, sees the seat is now `HELD`, and gracefully fails with a `409 Conflict`.
 
### 2. Reservation Expiry Background Job
To prevent seats from being locked forever by users who abandon the checkout process, a `ReservationExpiryService` runs asynchronously.
* **Configurable:** Uses `application.properties` (`reservation.hold-minutes=3`, `reservation.expiry-job-rate=30000`).
* **Transaction Safe:** When it finds expired reservations, it updates the reservation to `EXPIRED` and loops through all associated `ReservationItem`s to flip seats back to `AVAILABLE` within a single `@Transactional` block.
### 3. Price Snapshotting
The `ReservationItem` entity stores the `priceAtReservation`. Even if an Organizer changes the `Event.ticketPrice` a week later, old reservations retain the price the customer agreed to at checkout.
 
##  Project Structure
 
```text
com.netpoint.ticketing
 ├── Config         # Security & Swagger configuration
 ├── Controller     # REST API endpoints
 ├── DTO            # Request/Response objects (Validation applied here)
 ├── Enum           # Roles, SeatStatus, ReservationStatus, PaymentStatus
 ├── Exceptions     # Custom exceptions & GlobalExceptionHandler
 ├── Filter         # JWT parsing and authentication
 ├── Model          # JPA Entities (User, Event, Seat, Reservation, Payment)
 ├── Repository     # Spring Data JPA interfaces (with locking queries)
 └── Service        # Core business logic & Scheduled tasks
```
 
##  Setup & Run
 
### 1. Database Setup
1. Open pgAdmin or your PostgreSQL terminal.
2. Create a new database named `TicketingApp`.
### 2. Application Properties
Update `src/main/resources/application.properties` with your PostgreSQL credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/TicketingApp
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```
 
### 3. Run the App
Run the `TicketingApplication.java` main method. Hibernate will automatically create the necessary tables (`ddl-auto=update`).
 
### 4. Explore the API
Navigate to the Swagger UI in your browser:
 **[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)**
 
*Click the **"Authorize"** button at the top right and paste a valid JWT to test secure endpoints directly from the browser.*
 
##  Main API Endpoints
 
### Auth
* `POST /api/auth/register` - Create a CUSTOMER, ORGANIZER, or ADMIN.
* `POST /api/auth/login` - Authenticate and receive a JWT.
### Events & Venues (Public / Organizer)
* `POST /api/venues` (ORGANIZER/ADMIN)
* `POST /api/events` (ORGANIZER) - Auto-generates seats based on `seatCapacity`.
* `GET /api/events/{id}/seats/available` (Public)
### Reservations (Customer)
* `POST /api/reservations` - Create a PENDING reservation (Locks seats).
* `GET /api/reservations/my` - View booking history.
* `POST /api/reservations/{id}/pay` - Simulate payment (`{ "simulateSuccess": true }`).
* `PATCH /api/reservations/{id}/cancel` - Manually cancel a PENDING or CONFIRMED reservation.
