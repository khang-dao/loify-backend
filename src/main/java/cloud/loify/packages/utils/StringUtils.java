package cloud.loify.packages.utils;

public class StringUtils {

    public static String customizePlaylistName(String playlistName, String genre){
        return "loify (" + genre.toLowerCase() + ") - " + playlistName + " 🍃";
    }

    public static String customizePlaylistDescription(String playlistName, String genre){
        return "a " + genre.toLowerCase() + " version of playlist: " + playlistName;
    }

    public static String customizeTrackName(String trackName, String genre){
        return trackName + " " + genre.toLowerCase();
    }
}
