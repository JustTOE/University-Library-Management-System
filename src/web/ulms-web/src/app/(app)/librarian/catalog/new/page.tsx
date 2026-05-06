import { BookForm } from "@/components/librarian/book-form";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function NewBookPage() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>New book</CardTitle>
        <CardDescription>
          Add a book to the catalog. Title, author, and ISBN are required.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <BookForm mode="create" />
      </CardContent>
    </Card>
  );
}
