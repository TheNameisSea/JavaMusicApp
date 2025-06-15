import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.*;

public class MusicPlayer extends PlaybackListener {

    private static final Object playSignal = new Object();

    public MusicPlayerGUI musicPlayerGUI;

    private Queue<Song> songQueue = new LinkedList<>();

    // Song class to store song details
    private Song currentSong;
    public Song getCurrentSong(){
        return currentSong;
    }
    public void setCurrentSong(Song song){
        currentSong = song;
    }

    // LinkedList of the Songs in a Playlist
    private LinkedList<Song> playlist;
    // Index to keep track of the current song in the linked list
    private int currentPlaylistIndex;

    // AdvancedPlayer obj to handle music
    private AdvancedPlayer advancedPlayer;

    // Play/pause indicator
    private volatile boolean isPaused;

    // boolean flag used to tell when the song has finished
    private volatile boolean songFinished;

    private volatile boolean pressedNext, pressedPrev;

    // Current frame
    private int currentFrame;
    public void setCurrentFrame(int frame){
        currentFrame = frame;
    }

    // Track time passed
    private int currentTimeInMilli;
    public void setCurrentTimeInMilli(int timeInMilli){
        currentTimeInMilli = timeInMilli;
    }

    public void resetVariable(){
        isPaused = false;
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    // Constructor
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
    }

    public void loadSong(Song song){
        currentSong = song;
        playlist = null;

        if (!songFinished){
            stopSong();
        }


        // Play song
        if (currentSong != null){
            currentFrame = 0;
            currentTimeInMilli = 0;

            // Update GUI
            musicPlayerGUI.setPlaybackSliderValue(0);

            // Reset values
            resetVariable();

            playCurrentSong();

        }

    }

    public void loadPlaylist(File playlistFile){
        playlist = new LinkedList<>();
        HashMap<String, String> songMap = MusicLibraryWindow.songMap;
        // store the paths from the text file into the playlist array list
        try{
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // reach each line from the text file and store the text into the songPath variable
            String songName;

            while((songName = bufferedReader.readLine()) != null){
                // create song object based on song path
                if (songMap.containsKey(songName)){
                    Song song = new Song(songMap.get(songName));
                    // add to playlist linked list
                    playlist.add(song);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(!playlist.isEmpty()){
            // reset playback slider
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli = 0;

            // update current song to the first song in the playlist
            currentSong = playlist.getFirst();

            // start from the beginning frame
            currentFrame = 0;

            // update gui
            musicPlayerGUI.updateGUI(currentSong);
            resetVariable();

            // start song
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

    public void nextSong(){
        // no need to go to the next song if there is no playlist
        if(playlist == null) return;

        // check to see if we have reached the end of the playlist, if so then don't do anything
        if(currentPlaylistIndex + 1 > playlist.size() - 1) return;

        pressedNext = true;

        // stop the song if possible
        if(!songFinished)
            stopSong();

        // increase current playlist index
        currentPlaylistIndex++;

        // update current song
        currentSong = playlist.get(currentPlaylistIndex);


        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;


        // update gui
        musicPlayerGUI.updateGUI(currentSong);
        resetVariable();

        // play the song
        playCurrentSong();

    }

    public void prevSong(){
        // no need to go to the next song if there is no playlist
        if(playlist == null) return;

        // check to see if we can go to the previous song
        if(currentPlaylistIndex - 1 < 0) return;

        pressedPrev = true;

        // stop the song if possible
        if(!songFinished)
            stopSong();

        // decrease current playlist index
        currentPlaylistIndex--;

        // update current song
        currentSong = playlist.get(currentPlaylistIndex);

        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;

        // update gui
        musicPlayerGUI.updateGUI(currentSong);
        resetVariable();

        // play the song
        playCurrentSong();
    }

    public void playCurrentSong(){

        if (currentSong == null){return;}

        try {
            // Read audio data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Create new advancedPlayer
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            // Play
            startMusicThread();

            // Start playback slider
            startPlaybackSliderThread();

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
                        synchronized (playSignal){
                            isPaused = false;
                            playSignal.notify();
                        }

                        // Resume from last frame
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

    // Create a thread that will handle updating the slider
    private void startPlaybackSliderThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isPaused){
                    try{
                        synchronized (playSignal){
                            playSignal.wait();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                while(!isPaused && !songFinished && !pressedNext && !pressedPrev){
                    try {
                        currentTimeInMilli++;

                        // Calculate frame
                        int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentSong.getFrameRatePerMilliseconds());

                        // Update GUI
                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);
                        musicPlayerGUI.updateTimeLabel(currentTimeInMilli);
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished");

        if (isPaused){
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        } else{
            if(pressedNext || pressedPrev) return;

            songFinished = true;

            // Check if we need to play the next song in queue
//            if (!songQueue.isEmpty()) {
//                currentSong = songQueue.poll();
//                playCurrentSong();
//            } else if (playlist != null && currentPlaylistIndex + 1 < playlist.size()) {
//                currentPlaylistIndex++;
//                currentSong = playlist.get(currentPlaylistIndex);
//                playCurrentSong();
//            }

            if (!songQueue.isEmpty()) {
                currentSong = songQueue.poll();

                currentFrame = 0;
                currentTimeInMilli = 0;
                musicPlayerGUI.updateGUI(currentSong);
                musicPlayerGUI.setPlaybackSliderValue(0);
                musicPlayerGUI.updatePlaybackSlider(currentSong);
                musicPlayerGUI.enablePauseButtonDisablePlayButton();

                playCurrentSong();




            } else if (playlist == null) {
                return;
            } else if (currentPlaylistIndex == playlist.size() - 1) {
                // update gui
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            } else {
                nextSong();
            }

//            if(playlist == null){
//                // update gui
//                musicPlayerGUI.enablePlayButtonDisablePauseButton();
//            }else{
//                // last song in the playlist
//                if(currentPlaylistIndex == playlist.size() - 1){
//                    // update gui
//                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
//                }else{
//                    // go to the next song in the playlist
//                    nextSong();
//                }
//            }
        }

    }

    public LinkedList<Song> getPlaylist() {
        return playlist;
    }

    public void addToQueue(Song song) {
        songQueue.add(song);
    }

    public void removeFromQueue(Song song) {
        songQueue.remove(song);
    }

    public List<Song> getQueue() {
        return new ArrayList<>(songQueue); // for viewing in UI
    }

    public void moveSongInQueue(int fromIndex, int toIndex) {
        List<Song> tempList = new ArrayList<>(songQueue);
        Song song = tempList.remove(fromIndex);
        tempList.add(toIndex, song);
        songQueue = new LinkedList<>(tempList);
    }

}