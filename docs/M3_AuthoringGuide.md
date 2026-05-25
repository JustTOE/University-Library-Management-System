# M3 Authoring Guide

> **What this is.** A workflow guide for turning [`M3_Design.md`](./M3_Design.md) into the actual deliverable: a Google Doc with embedded `.drawio` diagrams. This is a *workflow* doc for you, not a course deliverable — it does not need to be polished or submitted.
>
> **Tools.** draw.io (diagrams.net) for every diagram; Google Docs for prose; PNG export for embedding.
>
> **Source of truth.** `M3_Design.md` is authoritative. Every Mermaid block in there has been cross-checked against `src/ulms`. When you draw a diagram in draw.io, copy the *content* from M3_Design.md — class names, method names, arrows, multiplicity. Don't re-derive anything from the code; that work is already done.
>
> **Time estimate.** ~17 hours total per the plan budget. Breakdown is at the bottom.

---

## Table of contents

- [Phase 0 — One-time setup](#phase-0--one-time-setup)
- [Phase 1 — Google Doc skeleton](#phase-1--google-doc-skeleton)
- [Phase 2 — Section 3a: 14 design class diagrams](#phase-2--section-3a-14-design-class-diagrams)
- [Phase 3 — Section 3b: 14 design sequence diagrams](#phase-3--section-3b-14-design-sequence-diagrams)
- [Phase 4 — Section 3c: Loan statechart](#phase-4--section-3c-loan-statechart)
- [Phase 5 — Section 3d: software architecture](#phase-5--section-3d-software-architecture)
- [Phase 6 — Final assembly and submission](#phase-6--final-assembly-and-submission)
- [Effort budget](#effort-budget)
- [Common pitfalls](#common-pitfalls)

---

## Phase 0 — One-time setup

### 0.1 Install / open draw.io

Two equivalent options:

- **Web app** (no install): https://app.diagrams.net/ — pick "Device" or "Google Drive" when it asks where to save.
- **Desktop app** (recommended for offline + autosave): download from https://github.com/jgraph/drawio-desktop/releases.

**Recommendation:** save your `.drawio` files to Google Drive directly. Then in step 6 you can right-click → *Open with → Google Docs* won't work, but you'll see live thumbnails and can re-export PNGs without re-uploading source.

### 0.2 Create the working folders

Match the M2 convention. M2 used `Homework/HW2/SoftwareUseCaseDiagram.drawio` (referenced in `docs/M2_Requirements.md:23`), so for M3:

```
Homework/HW3/
├── 3a-class-diagrams/
│   ├── UC1-Register.drawio
│   ├── UC2-Login.drawio
│   ├── ... (14 files)
├── 3b-sequence-diagrams/
│   ├── UC1-Register.drawio
│   ├── ... (14 files)
├── 3c-statechart/
│   └── Loan-statechart.drawio
└── 3d-architecture/
    ├── logical-view.drawio
    ├── package-map.drawio
    └── physical-view.drawio
```

These don't need to be committed to the repo — keep them on Google Drive or your local disk and submit alongside the Doc if the course wants source files. (M2's `.drawio` source isn't in the repo either.)

### 0.3 Set draw.io defaults

Once, in any `.drawio` file:

- **File → Page Setup** → set page size to **A4 Landscape**. Class and sequence diagrams are wider than tall.
- **Extras → Theme** → "Atlas" or "Kennedy" (whichever you prefer; just be consistent across all 30+ diagrams).
- **View → Grid** on. Snap-to-grid on. Makes alignment painless.
- Default font: 12pt. 9pt for stereotypes (`<<Service>>` etc.). Set this in **Format panel → Edit Style** when you create the first class.

### 0.4 Open M3_Design.md side-by-side

Put `M3_Design.md` in one window (VS Code, GitHub preview, or just the rendered file) and draw.io in the other. You will copy *from* M3_Design.md *into* draw.io constantly.

---

## Phase 1 — Google Doc skeleton

Before drawing anything, build the empty Google Doc so you have a place to drop the images as you finish them.

### 1.1 Create the doc

1. Go to docs.google.com → New document.
2. Name it `ULMS — M3 Design` (or whatever the course filename convention is).
3. **Page Setup** (File → Page Setup): A4, margins 2.5 cm. Matches a printed thesis-style submission.
4. Add a **header** with `ULMS — M3 Design` on the left and your name on the right.

### 1.2 Title block

Mirror M2's header. Type at the top:

```
Milestone 3 — Design

Project: ULMS — University Library Management System
Course: Software Development Methods · Politehnica University of Bucharest
Author: Mihai Tintareanu
Milestone: 3 — Design
```

Apply *Title* style to "Milestone 3 — Design", then a horizontal line.

### 1.3 Insert table of contents

1. Insert → Table of contents → "With page numbers" or "With links" (your preference).
2. Below the ToC, add **section headings** in this exact order, using *Heading 1* and *Heading 2* styles:

```
3a. Design Class Diagrams                 [H1]
  3a.1 Use-case → class participation map [H2]
  3a.2 Notation conventions               [H2]
  3a.3 Per-use-case class diagrams        [H2]
    UC1 — Register Account                [H3]
    UC2 — Log In                          [H3]
    ... (UC1 through UC14)
3b. Design Sequence Diagrams              [H1]
  3b.1 Notation conventions               [H2]
  3b.2 Per-use-case sequence diagrams     [H2]
    UC1 — Register Account                [H3]
    ... (UC1 through UC14)
3c. Loan Statechart                        [H1]
3d. Software Architecture                  [H1]
  3d.1 Logical view                        [H2]
  3d.2 Physical view                       [H2]
3e. Verification                           [H1]
```

3. Refresh the ToC (right-click → *Update table of contents*) to confirm all headings appear.

### 1.4 Copy prose from M3_Design.md now

For every section that is *prose only* (no diagrams), copy the markdown text from `M3_Design.md` straight into the Google Doc and fix the formatting:

- The opening paragraph of section 3a (about Book vs LibraryCatalog).
- Section 3a.1 — paste the participation-map table (Insert → Table → 7 cols × 15 rows; or paste into a draw.io grid and screenshot if it's faster).
- Section 3a.2 notation conventions (bullet list).
- Section 3b.1 notation conventions.
- Section 3c "States and triggers" table + "Guards" bullets.
- Section 3d.1 prose between diagrams (cross-cutting concerns paragraphs, design-pattern table).
- Section 3d.2 tech-stack rationale table + configuration management bullets.
- Section 3e verification list.

**Tip.** Markdown tables don't paste directly into Google Docs. Either rebuild them with Insert → Table, or paste into a Google Sheet and copy back as a formatted table.

By the end of Phase 1 the Doc has all the prose; only the diagrams are missing. Now go draw them.

---

## Phase 2 — Section 3a: 14 design class diagrams

### 2.1 Set up a class-diagram template (do this once)

Open `Homework/HW3/3a-class-diagrams/UC1-Register.drawio` and build a reusable template:

1. From the left shape panel, expand **UML** (search "UML" if not visible).
2. Drag a **"Class"** shape onto the canvas. Edit its title to `ClassName`. Click the body and add three compartments separated by a horizontal line: stereotype line (e.g. `<<Service>>`), attributes block, operations block.
3. Style it: light blue header, white body, 1pt black border. Right-click → *Edit style* and copy the style string — you'll paste this into every other class to keep them visually consistent.
4. Drag connectors:
   - **Dependency** (`..>` in Mermaid): dashed line, open arrow. UML's "Dependency" connector.
   - **Association** (`-->` in Mermaid): solid line, open arrow. UML's "Association" with multiplicity labels.
   - **Realization** (`..|>` in Mermaid): dashed line, hollow triangle arrow. UML's "Realization".
5. **File → Save as Template** (or just keep this file open and "Save As" for each UC).

### 2.2 Per-UC checklist (repeat for UC1–UC14)

For each of the 14 use cases, do **exactly this**:

1. **Open** `M3_Design.md` and scroll to the UC's class diagram. Read the Mermaid source — it's the spec.
2. **Copy the file** `UC1-Register.drawio` → `UC{N}-{Name}.drawio`.
3. **Add a class box** for every `class X { ... }` block in the Mermaid source.
   - Title = class name from Mermaid.
   - Stereotype line = the `<<Stereotype>>` from Mermaid (`<<RestController>>`, `<<Service>>`, `<<Repository>>`, `<<Entity>>`, `<<Mapper>>`, `<<Component>>`, `<<interface>>`).
   - Attributes = the `-fieldName: type` lines.
   - Operations = the `+methodName(args) ReturnType` lines.
4. **Add the relationships.** Walk through every `A ..> B`, `A --> B`, `A ..|> B` line at the bottom of the Mermaid block and draw the matching connector.
   - For associations with multiplicity (`Loan "*" --> "1" User`), put `*` on the Loan end and `1` on the User end. Add role names if Mermaid has any.
   - For dependencies, leave them unlabelled unless Mermaid annotates them (e.g. `LoanService ..> LoanMapper`).
5. **Add any notes** from the Mermaid block as `note` shapes (yellow sticky-note style).
6. **Layout**: arrange so controllers are at the top, services in the middle, repositories below them, entities at the bottom. Keep the same vertical hierarchy across every UC — graders skim faster when the layout is predictable.
7. **Export to PNG**: File → Export as → PNG. Settings: zoom 200%, transparent background OFF (white background), border 10px. Save as `UC{N}-class.png` in the same folder.
8. **Insert into Google Doc** under the matching UC heading: Insert → Image → Upload from computer → centre-align → caption `Figure 3a.{N}: UC{N} class diagram`.
9. **Tick off the UC** in your todo list and move on.

### 2.3 UC-by-UC class list (cross-check before drawing)

Before exporting each diagram, sanity-check it against this checklist. If a class is missing from your draw.io file, add it before exporting.

| UC | Classes that must appear |
|----|--------------------------|
| UC1 | AuthController, AuthService, UserService, UserRepository, User, PasswordEncoder, RegistrationCompletedEvent, RegistrationMailListener, MailService |
| UC2 | AuthController, AuthService, UserRepository, User, PasswordEncoder, JwtService, AuthResponse |
| UC3 | NotificationController, NotificationService, NotificationRepository, Notification, User, NotificationMapper |
| UC4 | BookController, BookService, BookRepository, Book, BookMapper |
| UC5 | ReservationController, ReservationService, ReservationRepository, BookRepository, UserRepository, Reservation, Book, User, ReservationMapper |
| UC6 | ReservationController, ReservationService, ReservationRepository, Reservation, OwnershipChecker |
| UC7 | LoanController, LoanService, LoanRepository, BookRepository, UserRepository, FineRepository, Loan, Book, User, Fine, LoanMapper |
| UC8 | LoanController, LoanService, LoanRepository, BookRepository, FineRepository, ReservationRepository, Loan, Book, Fine, Reservation |
| UC9 | LoanController, LoanService, LoanRepository, FineRepository, Loan, Fine, OwnershipChecker |
| UC10 | PaymentController, PaymentService, PaymentRepository, FineRepository, PaymentGatewayClient «interface», MockPaymentGatewayClient, ChargeResult, Payment, Fine, PaymentMapper |
| UC11 | BookController, LibraryCatalogController, BookService, LibraryCatalogService, BookRepository, LoanRepository, LibraryCatalogRepository, Book, LibraryCatalog, BookMapper, BookInUseException |
| UC12 | UserController, UserService, LoanService, FineService, UserRepository, User, PasswordEncoder, UserMapper, UserHistoryResponse |
| UC13 | FineCalculationJob «Component», SchedulerRunRecorder, FineService, LoanRepository, FineRepository, SchedulerRunRepository, Loan, Fine, SchedulerRun |
| UC14 | NotificationDispatchJob «Component», SchedulerRunRecorder, NotificationService, LoanRepository, ReservationRepository, NotificationRepository, Loan, Reservation, Notification, SchedulerRun |

If any cell looks wrong, the truth is in `M3_Design.md` section 3a.3 — do not invent classes.

---

## Phase 3 — Section 3b: 14 design sequence diagrams

### 3.1 Set up a sequence-diagram template

In draw.io, sequence diagrams live under **UML → "Sequence"** in the shape panel.

1. Open `Homework/HW3/3b-sequence-diagrams/UC1-Register.drawio`.
2. Drag a **"Lifeline"** shape (long vertical rectangle with a name at top) — one for each participant.
3. Use **"Message"** arrows between lifelines:
   - Solid arrow with filled head = synchronous call (`->>` in Mermaid).
   - Dashed arrow with open head = return (`-->>` in Mermaid).
4. **Activation bars** (thin vertical rectangles on the lifeline) mark when an object is processing a call. Drop one for each `Note over X: @Transactional` boundary.
5. For UC2 and UC10, you'll need an **"Alt" frame** (a rectangle with a divider): UML → "Frame" → Alt.

### 3.2 Per-UC checklist

For each UC1–UC14:

1. Read the matching `sequenceDiagram` Mermaid block in `M3_Design.md` § 3b.2.
2. **Lifelines first.** Look at every `participant X as Y` and `actor X` line. Drop one lifeline per participant, in the order they appear. Actor on the far left, then controller, service, repositories, external systems on the right.
3. **Messages next.** Walk each `A->>B: messageName(args)` line top-to-bottom and draw the arrow. Label = `messageName(args)`.
4. **Returns.** Each `A-->>B: returnValue` becomes a dashed return arrow.
5. **Notes.** Each `Note over X: text` becomes a yellow note attached to that lifeline.
6. **Alt frames.** Only UC2 and UC10. Draw an alt frame around the divergent branches. Branch labels: `[correct credentials]`, `[invalid credentials]`, `[account locked]` for UC2; `[gateway accepts]`, `[gateway declines]` for UC10.
7. **Loops.** UC13 and UC14 use `loop for each X` blocks. Wrap the looped messages in a UML "Loop" frame.
8. **Scheduler convention.** UC13 and UC14 have no human actor. The leftmost lifeline is `Scheduler` with the comment `triggered by @Scheduled` written above it.
9. **Export PNG**: same settings as Phase 2.7. File name: `UC{N}-sequence.png`.
10. **Insert into Google Doc** under the matching UC subheading. Caption: `Figure 3b.{N}: UC{N} sequence diagram`.

### 3.3 Special cases worth flagging while drawing

- **UC1.** The async-mail path runs *after the HTTP response*. Show this with a separator comment `Note over RegistrationMailListener: AFTER_COMMIT, @Async("ulmsTaskExecutor")` and put the listener's lifeline visually *below* the return arrow, not aligned with the synchronous flow.
- **UC2.** The `alt` has three branches, not two. Make sure all three are drawn (success, invalid credentials, locked). The locked branch persists `lockedUntil = now + 15 minutes`.
- **UC8.** Has a nested `alt` for "return is overdue" (the inline UC13 fine creation). Keep this *inside* the main return flow, not as a separate diagram.
- **UC10.** `noRollbackFor = PaymentDeclinedException` — note this on the activation bar so the grader sees that FAILED Payment rows persist even on the failure branch.
- **UC13.** Show the `SchedulerRunRecorder.record(...)` call *before* the candidate query and the loop, and the `save(SchedulerRun{finishedAt, status=OK})` call *after* the loop closes. The lifecycle of the `SchedulerRun` row is part of the diagram.
- **UC14.** Three sequential loops (due reminders → overdue alerts → reservation ready). Don't merge them; each is a separate `loop` frame.

---

## Phase 4 — Section 3c: Loan statechart

Only one diagram. Should take ~45 minutes.

### 4.1 Steps

1. Open `Homework/HW3/3c-statechart/Loan-statechart.drawio`.
2. **States.** Drop UML state shapes (rounded rectangles) for: `ACTIVE`, `RENEWED`, `OVERDUE`, `RETURNED`, `LOST`. Plus an initial pseudo-state (filled black circle) and final pseudo-state (filled black circle with a ring around it).
3. **Layout.** Initial state on the left, terminal states (`RETURNED`, `LOST`) on the right. ACTIVE → RENEWED side by side; OVERDUE below them.
4. **Transitions.** Copy each row from the table in `M3_Design.md` § 3c. Label each arrow with `trigger [guard] / action`:
   - `borrow / LoanService.borrowBook`
   - `renew [renewal_count<3 ∧ noUnpaidFines] / LoanService.renewLoan` (this one is a self-loop on RENEWED)
   - `schedulerDetectsOverdue / FineCalculationJob`
   - `return / LoanService.returnBook`
   - `return [creates Fine]` (the OVERDUE → RETURNED arrow)
   - `markLost (future)` — **draw these three transitions with dashed lines** to indicate they are not yet implemented.
5. **Guards box.** Add a note off to the side listing: `MAX_RENEWALS = 3`, `LOAN_PERIOD_DAYS = 14`, `noUnpaidFines = fineRepository.findByLoanUserAndStatusNot(user, PAID).isEmpty()`.
6. **LOST annotation.** Add a note on the LOST state: `Future state — enum value present in LoanStatus, no setter path implemented yet`. The grader needs to know this is intentional.
7. **Export PNG** as `Loan-statechart.png`. Insert into Google Doc under "3c. Loan Statechart".

---

## Phase 5 — Section 3d: software architecture

Three diagrams here: logical view (layered architecture), package map, and physical view (deployment).

### 5.1 Logical view diagram

1. Open `Homework/HW3/3d-architecture/logical-view.drawio`.
2. **Layout.** Top-to-bottom layered. Use **container** shapes (rectangles labelled at the top):
   - **Presentation** layer: contains "React components — shadcn/ui + Tailwind".
   - **API layer** (Spring Boot 4): contains four boxes stacked vertically — REST Controllers, Service layer, Repositories, JPA Entities.
   - **Cross-cutting** column on the right side: SecurityConfig + JwtAuthenticationFilter, AsyncConfig (ulmsTaskExecutor), Scheduler (jobs/* + SchedulerRunRecorder), GlobalExceptionHandler, Flyway.
   - **Database**: cylinder shape labelled "Postgres 18".
3. **Arrows.**
   - Solid arrow: HTTPS/JSON from Browser → Presentation → API.
   - Solid arrow: API layer top-to-bottom (Controllers → Services → Repositories → Entities → Postgres).
   - Dashed arrows: cross-cutting boxes pointing into the API layer (security → controllers, async → services, etc.) — these signal "applies to" rather than "calls".
4. **Export** as `3d1-logical-view.png`. Insert under "3d.1 Logical view".

### 5.2 Package map

You can render this as a tree-style diagram or as a folder-style listing. The folder listing is faster:

1. Open `Homework/HW3/3d-architecture/package-map.drawio`.
2. Use draw.io's **Tree** shapes (under "General" → search "tree") or just nested rectangles.
3. Mirror the package tree from `M3_Design.md` § 3d.1 exactly. Highlight the four most important packages (controllers, services, repositories, entities) in a different colour.
4. **Alternatively**: skip the diagram entirely and use a styled code block in the Google Doc (Format → Paragraph styles → Code). The plain-text package tree from `M3_Design.md` reads fine as text. Many SDM submissions do this. Decision depends on whether your lecturer wants visual diagrams or is OK with text — when in doubt, both: keep the text *and* add a diagram.

### 5.3 Physical view (deployment)

1. Open `Homework/HW3/3d-architecture/physical-view.drawio`.
2. **Nodes.** Use **UML → "Node"** shapes (3D cube outline) for: Browser, Next.js (Node 22), Spring Boot (JVM 25), Postgres 18, MailHog, Payment Gateway.
3. **Container box.** Wrap Next.js, Spring Boot, Postgres, and MailHog inside a labelled box: `docker-compose`. Use a dashed border.
4. **Arrows.** Annotated with protocols, exactly as in M3_Design.md § 3d.2:
   - `Browser ──HTTPS / JSON──▶ Next.js (:3000)`
   - `Next.js ──HTTPS / JSON──▶ Spring Boot (:8080)`
   - `Spring Boot ──JDBC / TLS──▶ Postgres 18 (:5432)`
   - `Spring Boot ──SMTP──▶ MailHog`
   - `Spring Boot ──HTTPS / stub in dev──▶ Payment Gateway` (this one *exits* the docker-compose box).
5. **Export** as `3d2-physical-view.png`. Insert under "3d.2 Physical view".

### 5.4 Tech stack rationale table

This is a table, not a diagram. Insert → Table in Google Docs (3 columns × ~13 rows) and copy the rows from `M3_Design.md` § 3d.2.

### 5.5 Cross-cutting concerns and design patterns

Both are bullet/table content — no drawing. Copy from `M3_Design.md` directly. The patterns table fits well as a 3-column Google Docs table (`#`, Pattern, Where it appears).

---

## Phase 6 — Final assembly and submission

### 6.1 Walk-through pass

Read the Doc top-to-bottom in one sitting:

- Are headings consistent (same Heading 1 / 2 / 3 style throughout)?
- Are figure captions sequential (`Figure 3a.1`, `3a.2`, …, `Figure 3b.1`, …)?
- Does every figure have a caption *below* it, italic, centred?
- Does the table of contents match the actual heading structure? (Right-click → *Update*.)
- Are all 14 UC class diagrams **and** 14 sequence diagrams present? Count them.
- Did you actually paste section 3a.1's participation table? It's the largest piece of text content — easy to skip by accident.

### 6.2 Image quality check

Open one of the inserted PNGs at 100% zoom in the Doc. If the text looks fuzzy, re-export at 300% zoom from draw.io and re-insert. PNG export at 200% is usually fine for A4; 300% if you anticipate a printed submission.

### 6.3 Page count and pagination

Section breaks: insert a *Page break* (Ctrl+Enter) before each H1 heading (3a, 3b, 3c, 3d, 3e). This gives the Doc clean section starts when printed.

Expect ~30–45 pages total: 14 class diagrams + 14 sequence diagrams + ~10 pages of prose and tables.

### 6.4 Export

- **Google Doc submission**: File → Share → set sharing to "Anyone with the link can view" (or whatever the course requires).
- **PDF submission**: File → Download → PDF Document. Verify the PDF renders all images correctly (sometimes large PNGs get downscaled — open the PDF and zoom in to a sequence diagram to check legibility).
- **Source files**: zip `Homework/HW3/` and submit alongside if required.

### 6.5 Cross-link with M3_Design.md (optional)

Inside the Google Doc, near the title, add a footnote:

> Diagram source: `Homework/HW3/*.drawio` · Verified Mermaid source: [`docs/M3_Design.md`](https://github.com/JustTOE/ULMS/blob/dev/docs/M3_Design.md)

If a grader spots an inconsistency between the Doc and the Mermaid, they can compare. (And you have a paper trail showing the diagrams are grounded in real code.)

---

## Effort budget

Mirrors the plan's ~17-hour estimate. Spread across 2–3 days:

| Phase | Block | Time |
|---|---|---|
| 0 | One-time setup (folders, draw.io defaults, template class diagram) | 30 min |
| 1 | Google Doc skeleton + paste all prose | 1.5 h |
| 2 | Class diagrams: UC7 first as worked example (~30 min), then 13 × 15 min | 4 h |
| 3 | Sequence diagrams: UC7 first (~30 min), then 13 × 18 min | 4.5 h |
| 4 | Loan statechart | 45 min |
| 5 | Logical view + package map + physical view + tables | 2.5 h |
| 6 | Walk-through, image check, pagination, export | 1.5 h |
| — | Buffer / proofread / fixes | 1.5 h |
| | **Total** | **~16 h** |

Pace yourself: do Phase 1 + the UC7 worked examples on day 1, the bulk of the diagrams on day 2, architecture + assembly on day 3.

---

## Common pitfalls

- **Drawing UC2 first.** It has the only `alt` frame and is more complex than the average UC. Start with **UC7** (the worked example in M3_Design.md) — it's the one with the most concrete grounding and you'll build muscle memory faster.
- **Inventing classes.** If a class isn't in `M3_Design.md`, don't add it. The 14 class lists in section 2.3 above are the truth.
- **Wrong arrow direction.** In sequence diagrams, the arrow points *to* the receiver. `LoanController->>LoanService` means the controller calls a method on the service. Easy to draw backwards.
- **Forgetting multiplicity.** On class diagrams, every association needs `1`, `*`, `0..1`, etc. on both ends. Graders dock points for missing multiplicity — this is the most common SDM mistake.
- **Inconsistent stereotypes.** Use `<<RestController>>`, `<<Service>>`, `<<Repository>>`, `<<Entity>>` everywhere — never `<<Controller>>` or `<<DAO>>` or other variants. Pick the M3_Design.md spelling and stick with it.
- **PNG aliasing.** If your exported PNGs look pixelated in the Doc, the export zoom was too low. Re-export at 200–300% and replace.
- **The LOST state.** It is dashed *because* it isn't implemented. If you draw it solid like the others, the grader will assume the code does something it doesn't, and may grade against a feature that doesn't exist.
- **`Book` vs `LibraryCatalog`.** They are not duplicates. Read the opening paragraph of section 3a in M3_Design.md before drawing UC4 or UC11 — the distinction matters and it's an obvious checkpoint question if a grader is testing whether you understood the model.
- **Async mail in UC1 sequence diagram.** It runs *after* the HTTP response. Draw it visually below the `AC-->>Student: 201` arrow with the AFTER_COMMIT / @Async note. Easy to draw it as if it were synchronous.
- **Mermaid copy-paste shortcut.** If you're truly out of time and need to ship: paste the Mermaid blocks into https://mermaid.live, screenshot the rendered diagrams, and embed those PNGs. The visual style is plainer than draw.io but the content is identical and the graders care more about content than aesthetics. Treat this as the *fallback*, not the plan.
