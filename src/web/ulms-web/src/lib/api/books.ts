import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type BookResponse = components["schemas"]["BookResponse"];
export type CreateBookRequest = components["schemas"]["CreateBookRequest"];
export type PageBookResponse = components["schemas"]["PageBookResponse"];

export type BookSearchFilters = {
  title?: string;
  author?: string;
  subject?: string;
};

export type PageQuery = {
  page?: number;
  size?: number;
  sort?: string;
};

export function listBooks(q: PageQuery = {}, opts: FetchOptions = {}) {
  return apiFetch<PageBookResponse>("/api/books", {
    ...opts,
    query: { page: q.page, size: q.size, sort: q.sort },
  });
}

export function searchBooks(
  filters: BookSearchFilters = {},
  q: PageQuery = {},
  opts: FetchOptions = {},
) {
  return apiFetch<PageBookResponse>("/api/books/search", {
    ...opts,
    query: {
      title: filters.title,
      author: filters.author,
      subject: filters.subject,
      page: q.page,
      size: q.size,
      sort: q.sort,
    },
  });
}

export function getBookById(id: number, opts: FetchOptions = {}) {
  return apiFetch<BookResponse>(`/api/books/${id}`, opts);
}

export function getBookByIsbn(isbn: string, opts: FetchOptions = {}) {
  return apiFetch<BookResponse>("/api/books/by-isbn", {
    ...opts,
    query: { isbn },
  });
}

export function createBook(body: CreateBookRequest, opts: FetchOptions = {}) {
  return apiFetch<BookResponse>("/api/books", {
    ...opts,
    method: "POST",
    body,
  });
}

export function updateBook(
  id: number,
  body: CreateBookRequest,
  opts: FetchOptions = {},
) {
  return apiFetch<BookResponse>(`/api/books/${id}`, {
    ...opts,
    method: "PUT",
    body,
  });
}

export function deleteBook(id: number, opts: FetchOptions = {}) {
  return apiFetch<void>(`/api/books/${id}`, {
    ...opts,
    method: "DELETE",
  });
}
