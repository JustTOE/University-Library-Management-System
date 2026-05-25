import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type UserResponse = components["schemas"]["UserResponse"];
export type CreateUserRequest = components["schemas"]["CreateUserRequest"];
/**
 * Update payload: same as create but with an optional password. A blank/omitted
 * password tells the backend to keep the user's existing one.
 */
export type UpdateUserRequest = Omit<CreateUserRequest, "password"> & {
  password?: string;
};
export type UserHistoryResponse = components["schemas"]["UserHistoryResponse"];
export type UserRole = NonNullable<UserResponse["role"]>;

export function listUsers(opts: FetchOptions = {}) {
  return apiFetch<UserResponse[]>("/api/users", opts);
}

export function getUserById(id: number, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>(`/api/users/${id}`, opts);
}

export function getUserByEmail(email: string, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>("/api/users/by-email", {
    ...opts,
    query: { email },
  });
}

export function getUserByUniversityId(
  universityId: string,
  opts: FetchOptions = {},
) {
  return apiFetch<UserResponse>("/api/users/by-university-id", {
    ...opts,
    query: { universityId },
  });
}

export function getUserByStaffId(staffId: string, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>("/api/users/by-staff-id", {
    ...opts,
    query: { staffId },
  });
}

export function createUser(body: CreateUserRequest, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>("/api/users", {
    ...opts,
    method: "POST",
    body,
  });
}

export function updateUser(
  id: number,
  body: UpdateUserRequest,
  opts: FetchOptions = {},
) {
  return apiFetch<UserResponse>(`/api/users/${id}`, {
    ...opts,
    method: "PUT",
    body,
  });
}

export function activateUser(id: number, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>(`/api/users/${id}/activate`, {
    ...opts,
    method: "PUT",
  });
}

export function deactivateUser(id: number, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>(`/api/users/${id}/deactivate`, {
    ...opts,
    method: "PUT",
  });
}

export function deleteUser(id: number, opts: FetchOptions = {}) {
  return apiFetch<void>(`/api/users/${id}`, {
    ...opts,
    method: "DELETE",
  });
}

export function getUserHistory(id: number, opts: FetchOptions = {}) {
  return apiFetch<UserHistoryResponse>(`/api/users/${id}/history`, opts);
}
