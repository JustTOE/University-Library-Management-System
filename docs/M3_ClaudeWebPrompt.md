# Prompt for Claude.ai (web) — Assemble the M3 Word document

> **What this file is.** A self-contained prompt you paste into a Claude **Project** on claude.ai (web). Attach `M3_Design.md` and the 30 PNG diagrams to the Project, then send the prompt as your first message. Claude will produce a downloadable Word (.docx) document containing the full M3 deliverable.
>
> **Why a web Project (not Claude Code).** The web app can render and export a `.docx` artifact directly; Claude Code edits files locally and cannot produce Word output.

---

## Step 1 — Set up the Claude Project

1. Go to https://claude.ai → **Projects** → **Create project**.
2. Name it: `ULMS M3 — Word Assembly`.
3. **Project description** (paste this in):
   > Assemble the final Microsoft Word document for Milestone 3 (Chapter 3 — Design) of the ULMS (University Library Management System) coursework at Politehnica University of Bucharest. The author is Mihai Tintareanu. Source of truth is the attached `M3_Design.md`. Diagrams are pre-drawn and attached as PNGs; do not redraw them.
4. **Project knowledge** — upload these files:
   - `M3_Design.md` (the full design spec)
   - All 30 diagram PNGs, named exactly:
     - `UC1-class.png` … `UC14-class.png` (14 class diagrams)
     - `UC1-sequence.png` … `UC14-sequence.png` (14 sequence diagrams)
     - `Loan-statechart.png`
     - `3d1-logical-view.png`
     - `3d2-physical-view.png`
   - **Optional** (only if you drew it): `package-map.png`
5. **Custom instructions for the project** (paste this in the Project's *Instructions* field — keeps every conversation in the project on-spec):
   > You are assembling a final-deliverable Microsoft Word document for university coursework. Be precise, follow the structure of `M3_Design.md` exactly, never invent class names or methods, and prefer prose copied from `M3_Design.md` over rewording. Output a single `.docx` artifact per request. Do not omit sections. Do not summarise tables — render them in full.

---

## Step 2 — The prompt to send (copy-paste verbatim)

Paste everything below this line into the chat as your first message. It tells Claude exactly what to produce.

---

### Task

Produce a single Microsoft Word (`.docx`) document titled **`ULMS — M3 Design.docx`** that is the final deliverable for Milestone 3 of my SDM coursework. The source of truth is the attached `M3_Design.md`; the 30 diagrams are attached as PNGs. **Do not redraw any diagram** — embed the supplied PNGs.

### Document metadata

- Title: **Milestone 3 — Design**
- Subtitle / header block, on the cover page:
  - **Project:** ULMS — University Library Management System
  - **Course:** Software Development Methods · Politehnica University of Bucharest
  - **Author:** Mihai Tintareanu
  - **Milestone:** 3 — Design
- Page size: A4. Margins: 2.5 cm all sides.
- Font: Calibri or Cambria, 11pt body. Headings use the Word built-in **Heading 1/2/3** styles so the table of contents builds automatically.
- Running header (top right): `ULMS — M3 Design`. Page numbers in the footer, centred, starting at 1 on the page after the cover.

### Required structure (mirror `M3_Design.md` exactly)

Use this heading hierarchy and this order. Insert a **page break before every Heading 1**.

```
[Cover page]                                                   (no heading number)
[Table of contents]                                            (auto-generated)

3a. Design Class Diagrams                                      [H1]
    3a.1 Use-case → class participation map                    [H2]
    3a.2 Notation conventions                                  [H2]
    3a.3 Per-use-case class diagrams                           [H2]
        UC1 — Register Account                                 [H3]
        UC2 — Log In                                           [H3]
        UC3 — View Notifications                               [H3]
        UC4 — Search Catalog                                   [H3]
        UC5 — Reserve Book                                     [H3]
        UC6 — Cancel Reservation                               [H3]
        UC7 — Borrow Book                                      [H3]
        UC8 — Return Book                                      [H3]
        UC9 — Renew Loan                                       [H3]
        UC10 — Pay Fine                                        [H3]
        UC11 — Manage Catalog                                  [H3]
        UC12 — Manage Users                                    [H3]
        UC13 — Calculate Fine (system use case)                [H3]
        UC14 — Send Notification (system use case)             [H3]

3b. Design Sequence Diagrams                                   [H1]
    3b.1 Notation conventions                                  [H2]
    3b.2 Per-use-case sequence diagrams                        [H2]
        UC1 — Register Account                                 [H3]
        ... (UC1 through UC14, same order as 3a.3)

3c. Loan Statechart                                            [H1]
    States and triggers                                        [H2]
    Diagram                                                    [H2]
    Guards                                                     [H2]

3d. Software Architecture                                      [H1]
    3d.1 Logical view                                          [H2]
        Package map                                            [H3]
        Design patterns observed                               [H3]
        Cross-cutting concerns                                 [H3]
    3d.2 Physical view                                         [H2]
        Tech stack rationale                                   [H3]
        Configuration management                               [H3]

3e. Verification                                               [H1]
```

### Per-section content rules

- **Cover page.** Title (centred, 28pt). Below it, the metadata block above (each line on its own row, 12pt). Footer of the cover page: today's date. Do *not* put a page number on the cover.
- **Table of contents.** Use Word's automatic ToC (References → Table of Contents → built-in style 1 or 2). It must show three heading levels and page numbers.
- **3a opening prose.** Copy the two paragraphs that appear under `## 3a. Design Class Diagrams` in `M3_Design.md` (the one explaining what the class diagrams refine, and the one clarifying Book vs LibraryCatalog). Do **not** paraphrase — copy verbatim.
- **3a.1 participation table.** Render the full 14-row table from `M3_Design.md` § 3a.1 as a Word table. Header row in bold with a light fill. Repeat the header row on each page if it spans pages. Columns: `UC | Actor(s) | Controller | Service | Repository(ies) | Entity(ies) | External collaborators`.
- **3a.2 notation conventions.** Bullet list, verbatim from `M3_Design.md`.
- **3a.3 per-UC class diagrams.** For each UC1–UC14:
  - H3 heading exactly as in `M3_Design.md` (e.g. `UC7 — Borrow Book`).
  - Insert the corresponding `UC{N}-class.png` image, centred, scaled to fit page width (max 16 cm wide).
  - Caption below the image, italic, centred: `Figure 3a.{N}: UC{N} — {Name} class diagram`.
  - If `M3_Design.md` has a `>` blockquote note under that UC (e.g. UC1 has the async-mail note, UC3 has the ownership-check note, UC13 has the cron note, UC14 has the cron note), render it as an indented italic paragraph immediately after the caption.
  - **Do not** paste the Mermaid source code into the Word doc — the PNG replaces it.
- **3b.1 notation conventions.** Bullet list, verbatim.
- **3b.2 per-UC sequence diagrams.** Same pattern as 3a.3: H3 heading, `UC{N}-sequence.png`, caption `Figure 3b.{N}: UC{N} — {Name} sequence diagram`, optional note as italic.
- **3c Loan Statechart.**
  - H2 `States and triggers`: render the 7-row table from `M3_Design.md` § 3c verbatim.
  - H2 `Diagram`: insert `Loan-statechart.png`, caption `Figure 3c.1: Loan statechart`.
  - H2 `Guards`: bullet list verbatim.
- **3d.1 Logical view.**
  - Opening paragraph verbatim.
  - Insert `3d1-logical-view.png` immediately after the opening paragraph. Caption: `Figure 3d.1: Logical view (layered architecture)`.
  - H3 `Package map`: render the package tree from `M3_Design.md` § 3d.1 as a **Word code block** (Courier New 10pt, light grey background, no syntax highlighting). If `package-map.png` is attached, also embed it under the code block with caption `Figure 3d.2: Package map`. If `package-map.png` is **not** attached, skip the image — the code block alone is acceptable.
  - H3 `Design patterns observed`: render the 6-row table verbatim. Columns: `# | Pattern | Where it appears`. Bold the pattern names in column 2.
  - H3 `Cross-cutting concerns`: bullet list, each bullet starts with a bold lead-in (`**Authentication & authorisation.**`, etc.) followed by the rest of the bullet text. Copy verbatim.
- **3d.2 Physical view.**
  - Insert `3d2-physical-view.png`. Caption: `Figure 3d.{N}: Physical view (deployment diagram)` (renumber correctly — likely 3d.3 if the package map image is included, or 3d.2 if it isn't).
  - Paragraph immediately after the image: copy verbatim from `M3_Design.md` (`The runtime layout intentionally treats…`).
  - H3 `Tech stack rationale`: render the full 12-row table verbatim. Columns: `Concern | Choice | Rationale`.
  - H3 `Configuration management`: bullet list verbatim.
- **3e Verification.** Numbered list, items 1–6, verbatim.

### Formatting rules (non-negotiable)

1. **Verbatim from `M3_Design.md`.** Do not reword, summarise, expand, or "improve" prose. The grader is checking the design, not your editing.
2. **Tables must be real Word tables.** Not screenshots, not pasted markdown. Header rows bold and shaded; body cells left-aligned; numeric/symbol cells (`UC1`, `#`) centred.
3. **Code blocks** (the package tree) use Courier New 10pt with a light grey background.
4. **Figure captions** are italic, centred, 10pt, immediately below the figure, prefixed with `Figure {section}.{n}:`.
5. **Inline class/method names** (e.g. `LoanService.borrowBook`, `BookRepository.decrementAvailable`) are in Courier New, same size as body text.
6. **Stereotypes** are written with French quotes when in body text: «RestController», «Service», «Repository», «Entity», «Mapper», «Component», «Filter», «Strategy», «interface». In tables and figure captions they can stay as `<<...>>` if that's what M3_Design.md uses — preserve the original form.
7. **Page breaks** before every H1. **No** orphan headings at the bottom of a page (use "keep with next" on heading styles).
8. **Images** scaled to fit the page width, never wider than 16 cm. Aspect ratio preserved.
9. **Special characters.** Preserve `∧` (logical AND), `→` (arrow), `≥`, `×`, `€`, em-dashes, en-dashes exactly as in `M3_Design.md`. Do not auto-replace with ASCII.

### Output

Produce one `.docx` artifact named **`ULMS — M3 Design.docx`**. Make it downloadable. After producing it, give a one-paragraph summary listing:
- Total page count.
- Number of figures embedded (should be **30 diagrams + 0–1 architecture extras = 30 or 31**).
- Number of tables (expected: at least 5 — participation map, statechart triggers, design patterns, tech stack, plus any I missed).
- Anything you couldn't render and why (e.g. missing image file, ambiguous formatting decision).

---

## Step 3 — After Claude responds

1. **Download the .docx** from the artifact.
2. **Open it in Word / Google Docs** and check:
   - Cover page renders correctly.
   - ToC is populated (if not, right-click → *Update field*).
   - All 30 diagrams are embedded (count them, page by page).
   - Tables are real Word tables, not images of tables.
   - Figure captions are sequential (`3a.1 … 3a.14`, `3b.1 … 3b.14`, `3c.1`, `3d.1`, `3d.2`, `3d.3`).
   - No "lorem ipsum" or `TODO` markers Claude left behind.
3. **If anything is wrong**, follow up in the same chat with a precise instruction, e.g.:
   > Figure 3b.7 is missing — please re-insert `UC7-sequence.png` under the `UC7 — Borrow Book` heading in section 3b.2 and regenerate the .docx.
   or
   > The participation map table in 3a.1 lost its row for UC10. Re-render the full 14-row table from `M3_Design.md` and regenerate.

   Don't start a new chat — the project knowledge stays, and Claude can re-edit the artifact incrementally.

---

## Notes

- **Why supply the PNGs instead of asking Claude to draw the diagrams.** Claude on the web can render Mermaid, but inserting Mermaid-as-image into a Word artifact is unreliable across browsers and the rendered style differs from your draw.io diagrams. Embedding the PNGs you already exported guarantees the Word doc matches what you'll show in person.
- **If you skip the architecture diagrams.** If you didn't draw `3d1-logical-view.png` or `3d2-physical-view.png`, tell Claude in the prompt: *"For 3d, render the Mermaid blocks from M3_Design.md inline as code-style figures; do not expect attached PNGs for the logical or physical view."* Claude will fall back to rendering those Mermaid blocks. Quality is lower but it ships.
- **If the .docx artifact fails to generate** (Claude.ai sometimes hits a size limit on Word artifacts with many embedded images), ask Claude to split the output: *"Produce two .docx files — one with sections 3a + 3b, one with sections 3c + 3d + 3e — and I'll merge them in Word."*
- **Iterate, don't restart.** Every regeneration burns tokens. Ask for targeted edits ("replace Figure 3b.10", "shorten the cross-cutting bullets to one line each") rather than rebuilding from scratch.
