package cloud.loify.packages.utils;

public class StringUtils {

    private String loifyPlaylistName(String playlistName){
        return "loify - " + playlistName + " 🍃";
    }

    private String loifyPlaylistDescription(String playlistName){
        return "a loify-ed version of playlist: " + playlistName;
    }

    private String loifyTrackName(String trackName){
        return trackName + " lofi";
    }
}
