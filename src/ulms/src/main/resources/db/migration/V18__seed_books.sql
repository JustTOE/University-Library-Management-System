-- Seed a small sample catalog so a fresh database is browsable/searchable.
-- Idempotent via ON CONFLICT on the unique isbn column.
INSERT INTO book (title, author, isbn, publication_year, subject, total_copies, available_copies, shelf_number)
VALUES
    ('Effective Java', 'Joshua Bloch', '9780134685991', 2018, 'Programming', 4, 4, 'CS-A1'),
    ('Java Concurrency in Practice', 'Brian Goetz', '9780321349606', 2006, 'Programming', 3, 3, 'CS-A2'),
    ('Clean Code', 'Robert C. Martin', '9780132350884', 2008, 'Programming', 5, 5, 'CS-A3'),
    ('The Pragmatic Programmer', 'Andrew Hunt', '9780201616224', 1999, 'Programming', 2, 2, 'CS-A4'),
    ('Design Patterns', 'Erich Gamma', '9780201633610', 1994, 'Software Engineering', 3, 3, 'CS-B1'),
    ('Refactoring', 'Martin Fowler', '9780134757599', 2018, 'Software Engineering', 2, 2, 'CS-B2'),
    ('Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 2009, 'Algorithms', 4, 4, 'CS-C1'),
    ('The Art of Computer Programming, Vol. 1', 'Donald E. Knuth', '9780201896831', 1997, 'Algorithms', 1, 1, 'CS-C2'),
    ('Database System Concepts', 'Abraham Silberschatz', '9780078022159', 2010, 'Databases', 3, 3, 'CS-D1'),
    ('Computer Networks', 'Andrew S. Tanenbaum', '9780132126953', 2010, 'Networking', 2, 2, 'CS-E1'),
    ('Operating System Concepts', 'Abraham Silberschatz', '9781118063330', 2012, 'Operating Systems', 3, 3, 'CS-F1'),
    ('Structure and Interpretation of Computer Programs', 'Harold Abelson', '9780262510875', 1996, 'Programming', 2, 2, 'CS-A5')
ON CONFLICT (isbn) DO NOTHING;

-- Keep the single catalog summary row in sync with the seeded count.
INSERT INTO library_catalog (total_books, last_updated)
VALUES ((SELECT COALESCE(SUM(total_copies), 0) FROM book), CURRENT_DATE);
