package cloud.loify.packages.playlist.exceptions;

public class PlaylistNotFoundException extends RuntimeException {

    public PlaylistNotFoundException(String message) {
        super(message);
    }

    public PlaylistNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}