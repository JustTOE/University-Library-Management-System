# System Sequence Diagrams — Mermaid Source

One diagram per use case, main flow only (per the course convention). UC2 Login is the one exception where an `alt` frame is shown, matching the professor's Login example.

All diagrams use the black-box `System` as a single lifeline, with solid arrows for actor-to-system messages and dashed (return) arrows for system-to-actor responses.

---

## UC1 — Register Account

```mermaid
sequenceDiagram
    actor Student
    participant System

    Student->>System: openRegistration()
    System-->>Student: registrationForm
    Student->>System: register(name, email, universityId, password)
    System-->>Student: registrationConfirmation
```

---

## UC2 — Log In

```mermaid
sequenceDiagram
    actor Actor
    participant System

    Actor->>System: login(email, password)
    alt correct credentials
        System-->>Actor: success(sessionToken, role)
    else
        System-->>Actor: fail
    end
```

---

## UC3 — View Notifications

```mermaid
sequenceDiagram
    actor Student
    participant System

    Student->>System: listNotifications()
    System-->>Student: notifications
    Student->>System: openNotification(notification)
    System-->>Student: notificationDetails
```

---

## UC4 — Search Catalog

```mermaid
sequenceDiagram
    actor User
    participant System

    User->>System: browseCatalog()
    System-->>User: bookList
    User->>System: searchBooks(title, author, subject)
    System-->>User: filteredBookList
    User->>System: selectBook(book)
    System-->>User: bookDetails
```

---

## UC5 — Reserve Book

```mermaid
sequenceDiagram
    actor Student
    participant System

    Student->>System: reserve(book)
    System-->>Student: reservationConfirmation(queuePosition)
```

---

## UC6 — Cancel Reservation

```mermaid
sequenceDiagram
    actor Student
    participant System

    Student->>System: listMyReservations()
    System-->>Student: reservations
    Student->>System: cancel(reservation)
    System-->>Student: cancellationConfirmation
```

---

## UC7 — Borrow Book

```mermaid
sequenceDiagram
    actor Student
    participant System

    Student->>System: borrow(book)
    System-->>Student: loanConfirmation(loan, dueDate)
```

---

## UC8 — Return Book

```mermaid
sequenceDiagram
    actor Librarian
    participant System

    Librarian->>System: lookupLoan(loanId)
    System-->>Librarian: loanDetails
    Librarian->>System: return(loan)
    System-->>Librarian: returnConfirmation(fineAmount)
```

---

## UC9 — Renew Loan

```mermaid
sequenceDiagram
    actor Student
    participant System

    Student->>System: listMyLoans()
    System-->>Student: activeLoans
    Student->>System: renew(loan)
    System-->>Student: newDueDate
```

---

## UC10 — Pay Fine

```mermaid
sequenceDiagram
    actor Student
    participant System
    participant PaymentGateway as Payment Gateway

    Student->>System: listMyFines()
    System-->>Student: unpaidFines
    Student->>System: pay(fine, method)
    System->>PaymentGateway: charge(amount, method)
    PaymentGateway-->>System: transactionResult
    System-->>Student: receipt
```

---

## UC11 — Manage Catalog

```mermaid
sequenceDiagram
    actor Librarian
    participant System

    Librarian->>System: addBook(title, author, isbn, subject, copies, shelf)
    System-->>Librarian: bookSaved
    Librarian->>System: editBook(book, updatedFields)
    System-->>Librarian: bookUpdated
    Librarian->>System: deleteBook(book)
    System-->>Librarian: bookDeleted
```

---

## UC12 — Manage Users

```mermaid
sequenceDiagram
    actor Administrator
    participant System

    Administrator->>System: listUsers()
    System-->>Administrator: users
    Administrator->>System: createUser(name, email, role)
    System-->>Administrator: userCreated
    Administrator->>System: deactivateUser(user)
    System-->>Administrator: userDeactivated
```

---

## UC13 — Calculate Fine

```mermaid
sequenceDiagram
    actor Scheduler
    participant System

    Scheduler->>System: calculateFines()
    System-->>Scheduler: finesCreated(count, totalAmount)
```

---

## UC14 — Send Notification

```mermaid
sequenceDiagram
    actor Scheduler
    participant System

    Scheduler->>System: dispatchNotifications()
    System-->>Scheduler: notificationsSent(count)
```
