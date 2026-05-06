import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./client";
import {
  getBookById,
  getBookByIsbn,
  listBooks,
  searchBooks,
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
