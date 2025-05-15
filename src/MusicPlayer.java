import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class MusicPlayer extends PlaybackListener {

    // Song class to store song details
    private Song currentSong;

    // AdvancedPlayer obj to handle music
    private AdvancedPlayer advancedPlayer;

    // Play/pause indicator
    private boolean isPaused;

    // Constructor
    public MusicPlayer(){

    }

    public void loadSong(Song song){

        currentSong = song;

        // Play song
        if (currentSong != null){
            playCurrentSong();
        }

    }

    public void pauseSong(){
        if (advancedPlayer!=null){
            isPaused = true;
            stopSong();

        }
    }

    public void stopSong(){
        if (advancedPlayer!=null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void playCurrentSong(){

        try {
            // Read auido data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Create new advancedPlayer
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            // Play
            startMusicThread();

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    // Create a thread that handle playing the music
    private void startMusicThread(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Play Music
                    advancedPlayer.play();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished");
    }
}
