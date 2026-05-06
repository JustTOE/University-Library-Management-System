import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./client";
import {
  createBook,
  deleteBook,
  getBookById,
  getBookByIsbn,
  listBooks,
  searchBooks,
  updateBook,
} from "./books";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe("listBooks()", () => {
  it("GETs /api/books with paging and returns the page", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        content: [{ id: 1, title: "Java", availableCopies: 2 }],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
        first: true,
        last: true,
        empty: false,
      }),
    );

    const page = await listBooks({ page: 0, size: 20, sort: "title,asc" }, {
      fetchImpl,
      token: "tok",
    });

    expect(fetchImpl).toHaveBeenCalledOnce();
    const [url, init] = fetchImpl.mock.calls[0]!;
    const u = new URL(String(url));
    expect(u.pathname).toBe("/api/books");
    expect(u.searchParams.get("page")).toBe("0");
    expect(u.searchParams.get("size")).toBe("20");
    expect(u.searchParams.get("sort")).toBe("title,asc");
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer tok");
    expect(page.content?.[0]?.title).toBe("Java");
  });
});

describe("searchBooks()", () => {
  it("GETs /api/books/search with filters and paging", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { content: [], totalElements: 0, number: 0, size: 20 }),
    );

    await searchBooks({ title: "java" }, { page: 1, sort: "title,asc" }, {
      fetchImpl,
    });

    const [url] = fetchImpl.mock.calls[0]!;
    const u = new URL(String(url));
    expect(u.pathname).toBe("/api/books/search");
    expect(u.searchParams.get("title")).toBe("java");
    expect(u.searchParams.get("author")).toBeNull();
    expect(u.searchParams.get("page")).toBe("1");
    expect(u.searchParams.get("sort")).toBe("title,asc");
  });
});

describe("getBookById()", () => {
  it("returns the BookResponse on 200", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 7, title: "Effective Java" }),
    );
    const book = await getBookById(7, { fetchImpl });
    expect(book.id).toBe(7);
    const [url] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/books\/7$/);
  });

  it("throws ApiError(404) when missing", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(404, {
        status: 404,
        error: "Not Found",
        message: "Book not found: 99",
      }),
    );

    await expect(getBookById(99, { fetchImpl })).rejects.toMatchObject({
      name: "ApiError",
      status: 404,
    });
  });
});

describe("getBookByIsbn()", () => {
  it("threads the isbn as a query param", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 1, isbn: "978-0134685991" }),
    );

    await getBookByIsbn("978-0134685991", { fetchImpl });
    const [url] = fetchImpl.mock.calls[0]!;
    const u = new URL(String(url));
    expect(u.pathname).toBe("/api/books/by-isbn");
    expect(u.searchParams.get("isbn")).toBe("978-0134685991");
  });

  it("surfaces ApiError on a 503 network failure", async () => {
    const fetchImpl = vi.fn().mockRejectedValue(new TypeError("fetch failed"));
    try {
      await getBookByIsbn("X", { fetchImpl });
      expect.fail("expected ApiError");
    } catch (caught) {
      expect(caught).toBeInstanceOf(ApiError);
      expect((caught as ApiError).status).toBe(503);
    }
  });
});

describe("createBook()", () => {
  it("POSTs the CreateBookRequest and returns the BookResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        id: 42,
        title: "Effective Java",
        author: "Bloch",
        isbn: "978-0134685991",
      }),
    );

    const book = await createBook(
      {
        title: "Effective Java",
        author: "Bloch",
        isbn: "978-0134685991",
        publicationYear: 2018,
        subject: "Programming",
        totalCopies: 3,
        availableCopies: 3,
        shelfNumber: "PRG-7",
      },
      { fetchImpl, token: "tok" },
    );

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/books$/);
    expect(init?.method).toBe("POST");
    expect(JSON.parse(init?.body as string)).toMatchObject({
      title: "Effective Java",
      isbn: "978-0134685991",
      totalCopies: 3,
      availableCopies: 3,
    });
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer tok");
    expect(book.id).toBe(42);
  });

  it("surfaces ApiError(409) on duplicate ISBN", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "could not execute statement",
      }),
    );

    await expect(
      createBook(
        {
          title: "Dup",
          author: "X",
          isbn: "978-0134685991",
          totalCopies: 1,
          availableCopies: 1,
        } as never,
        { fetchImpl },
      ),
    ).rejects.toMatchObject({ name: "ApiError", status: 409 });
  });
});

describe("updateBook()", () => {
  it("PUTs /api/books/{id} and returns the updated BookResponse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(200, { id: 5, title: "Edited", availableCopies: 2 }),
    );

    const book = await updateBook(
      5,
      {
        title: "Edited",
        author: "X",
        isbn: "111",
        totalCopies: 2,
        availableCopies: 2,
      } as never,
      { fetchImpl },
    );

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/books\/5$/);
    expect(init?.method).toBe("PUT");
    expect(book.title).toBe("Edited");
  });
});

describe("deleteBook()", () => {
  it("DELETEs /api/books/{id} and resolves to undefined on 204", async () => {
    const fetchImpl = vi
      .fn()
      .mockResolvedValue(new Response(null, { status: 204 }));

    const result = await deleteBook(5, { fetchImpl });

    const [url, init] = fetchImpl.mock.calls[0]!;
    expect(String(url)).toMatch(/\/api\/books\/5$/);
    expect(init?.method).toBe("DELETE");
    expect(result).toBeUndefined();
  });

  it("surfaces ApiError(409) on BookInUse", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(
      jsonResponse(409, {
        status: 409,
        error: "Conflict",
        message: "Book has active loans and cannot be deleted: 5",
      }),
    );

    await expect(deleteBook(5, { fetchImpl })).rejects.toMatchObject({
      status: 409,
      message: "Book has active loans and cannot be deleted: 5",
    });
  });
});
