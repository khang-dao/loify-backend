package cloud.loify.packages.me.exceptions;

public class PlaylistCreationException extends RuntimeException {
    public PlaylistCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}