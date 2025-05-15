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

    // Current frame
    private int currentFrame;

    // Constructor
    public MusicPlayer(){

    }

    public void loadSong(Song song){

        if (advancedPlayer!=null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }

        currentSong = song;
        currentFrame = 0;

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

        if (currentSong == null){return;}

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
                    if (isPaused){
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else{
                        // Play Music
                        advancedPlayer.play();
                    }

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

        if (isPaused){
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        }

    }
}
