import java.io.File;
import java.util.*;


public class MusicLibrary {

    SongTree songTree = new SongTree();

    File dir = new File("src/songs");

    // Constructor for the music library class
    MusicLibrary(){
        File[] lists = dir.listFiles();

        if (lists != null) {
            for (File file : lists) {
                // Create songs objects
                Song song = new Song(file.getPath());

                songTree.insert(song);

            }
        }

        songTree.inOrder(songTree.root);

        System.out.println();

        // Search algorithm
//        songTree.getClosestSongs("Piano Music", 5);
        songTree.searchNearestLexico("lonly", 5);


    }




}
