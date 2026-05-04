package dev.tmmc.ulms.objects.exceptions;

public class BookInUseException extends RuntimeException {
    public BookInUseException(String message) {
        super(message);
    }
}
