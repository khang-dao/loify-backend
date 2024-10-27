package cloud.loify.packages.utils;

public class StringUtils {

    public static String loifyPlaylistName(String playlistName){
        return "loify - " + playlistName + " 🍃";
    }

    public static String loifyPlaylistDescription(String playlistName){
        return "a loify-ed version of playlist: " + playlistName;
    }

    public static String loifyTrackName(String trackName){
        return trackName + " lofi";
    }
}
