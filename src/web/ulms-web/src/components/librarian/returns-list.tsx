"use client";

import Link from "next/link";
import { useMemo, useState } from "react";

import { ConfirmReturnButton } from "@/components/librarian/confirm-return-button";
import { LoanStatusBadge } from "@/components/common/status-pill";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { LoanResponse } from "@/lib/api/loans";
import { daysUntil, formatDateOnly } from "@/lib/format";

function matches(loan: LoanResponse, term: string): boolean {
  const haystack = [
    loan.userName,
    `user ${loan.userId ?? ""}`,
    loan.bookTitle,
    String(loan.id ?? ""),
  ]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
  return haystack.includes(term);
}

export function ReturnsList({ loans }: { loans: LoanResponse[] }) {
  const [query, setQuery] = useState("");

  const filtered = useMemo(() => {
    const term = query.trim().toLowerCase();
    if (!term) return loans;
    return loans.filter((loan) => matches(loan, term));
  }, [loans, query]);

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-col gap-1">
        <label htmlFor="returns-search" className="text-sm font-medium">
          Search
        </label>
        <Input
          id="returns-search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Borrower, book title, or loan id"
          data-testid="returns-search"
        />
      </div>

      <div className="rounded-lg border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Borrower</TableHead>
              <TableHead>Book</TableHead>
              <TableHead className="hidden md:table-cell">Borrowed</TableHead>
              <TableHead>Due</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={6}
                  className="py-8 text-center text-sm text-muted-foreground"
                >
                  No loans match your search.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((loan) => <ReturnRow key={loan.id} loan={loan} />)
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}

function ReturnRow({ loan }: { loan: LoanResponse }) {
  const due = daysUntil(loan.dueDate);

  return (
    <TableRow>
      <TableCell className="font-medium">
        {loan.userName ?? `user ${loan.userId ?? "?"}`}
      </TableCell>
      <TableCell>
        {loan.bookId ? (
          <Link href={`/catalog/${loan.bookId}`} className="hover:underline">
            {loan.bookTitle ?? `Book #${loan.bookId}`}
          </Link>
        ) : (
          <>{loan.bookTitle ?? "—"}</>
        )}
      </TableCell>
      <TableCell className="hidden text-muted-foreground md:table-cell">
        {formatDateOnly(loan.borrowDate)}
      </TableCell>
      <TableCell>
        <div className="flex flex-col">
          <span>{formatDateOnly(loan.dueDate)}</span>
          {due != null ? (
            <span
              className={
                due < 0
                  ? "text-xs text-destructive"
                  : due <= 3
                    ? "text-xs text-foreground"
                    : "text-xs text-muted-foreground"
              }
            >
              {due < 0
                ? `${Math.abs(due)} days overdue`
                : due === 0
                  ? "due today"
                  : `${due} days left`}
            </span>
          ) : null}
        </div>
      </TableCell>
      <TableCell>
        <LoanStatusBadge status={loan.status} />
      </TableCell>
      <TableCell className="text-right">
        <div className="flex justify-end">
          <ConfirmReturnButton loanId={loan.id ?? 0} alreadyReturned={false} />
        </div>
      </TableCell>
    </TableRow>
  );
}
