import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.ArrayList;

public class MusicPlayerGUI extends JFrame {

    // Color constant
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    public MusicPlayer musicPlayer;

    private MusicLibraryWindow musicLibraryWindow;

    private Song currentSong;

    // Allow file explorer
    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist;
    private JLabel songImage;
    private JPanel playbackBtns;

    private JSlider playbackSlider;

    public MusicPlayerGUI(){
        // Use JFrame constructor to configure the GUI
        super ("Music Player");

        // Set w and h
        setSize(400, 600);

        // Launch app at screen center
        setLocationRelativeTo(null);

        // prevent the app from being resized
        setResizable(false);

        setLayout(null);

        // Change color
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        musicLibraryWindow = new MusicLibraryWindow(this);

        jFileChooser = new JFileChooser();

        // Set default path for file explorer
        jFileChooser.setCurrentDirectory(new File("src/Library"));

        // Filter to only see mp3 file
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));


        addGuiComponents();

    }

    private void addGuiComponents(){
        // Add toolbar
        addToolbar();

        // Load music image
        songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth()-10, 225);
        add(songImage);

        // Music title
        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 285, getWidth()-10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        // Music artist
        songArtist = new JLabel("by Artist");
        songArtist.setBounds(0, 315, getWidth()-10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        // Playback slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Pause song when slider is pressed
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // When the slider is released
                JSlider source = (JSlider) e.getSource();

                // Get frame value to play
                int frame =  source.getValue();

                // Update current frame
                musicPlayer.setCurrentFrame(frame);

                // Update current time
                musicPlayer.setCurrentTimeInMilli(
                    (int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds()))
                );

                // Resume song
                musicPlayer.playCurrentSong();

                // Toggle play pause button
                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);

        // Playback buttons
        addPlaybackBtns();


    }

    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        // Prevent toolbar from being moved
        toolBar.setFloatable(false);

        // Add drop down menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // Add song menu
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        // Add option to load song
        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // int is to check if user has open the song or just selected it
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile!=null){
                    // Create a song obj
                    Song song = new Song(selectedFile.getPath());
                    currentSong = song;

                    // Update playback slider
                    updatePlaybackSlider(song);

                    // Load song
                    musicPlayer.loadSong(song);

                    // Update song metadata
                    updateSongTitleAndArtist(song);


                    // Update image

                    updateCoverImage(song);

                    // Toggle Play Pause button
                    enablePauseButtonDisablePlayButton();

                }

            }
        });
        songMenu.add(loadSong);


        // Add playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        // Add new playlist
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist dialog
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/playlist"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    // stop the music
                    musicPlayer.stopSong();

                    // load playlist
                    musicPlayer.loadPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        add(toolBar);



    }

    private void addPlaybackBtns(){
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth()-10, 80);
        playbackBtns.setBackground(null);

        // Prev button
        JButton prevButton = new JButton(loadImage("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the previous song
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        // Play button
        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseButtonDisablePlayButton();
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        // Pause button
        JButton pauseButton = new JButton(loadImage("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        // Next button
        JButton nextButton = new JButton(loadImage("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the next song
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    public void updateGUI(Song song){
        setPlaybackSliderValue(0);
        updateSongTitleAndArtist(song);
        updateCoverImage(song);
        updatePlaybackSlider(song);
        enablePauseButtonDisablePlayButton();
    }

    public void setPlaybackSliderValue(int frame){
        playbackSlider.setValue(frame);
    }

    public void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song){
        // Update max count for slider
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        // Create song length label
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        // Song beginning
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        // Song ending
        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);

    }

    public void updateCoverImage(Song song){
        BufferedImage cover = song.getCoverImage();
        if (cover != null) {
            Image scaledImage = cover.getScaledInstance(225, 225, Image.SCALE_SMOOTH);
            songImage.setIcon(new ImageIcon(scaledImage));
        }
        else{
            ImageIcon image = loadImage("src/assets/record.png");
            songImage.setIcon(image);
        }
    }

    public void enablePauseButtonDisablePlayButton(){

        // Get component at index 1/2 of Jbutton (play / pause)
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // Turn off play
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // Turn on pause
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);


    }

    public void enablePlayButtonDisablePauseButton(){

        // Get component at index 1/2 of Jbutton (play / pause)
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // Turn off pause
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);

        // Turn on play
        playButton.setVisible(true);
        playButton.setEnabled(true);


    }

    public ImageIcon loadImage(String imagePath){
        try {
            // Read image file from path
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);

        }catch (Exception e){
            e.printStackTrace();
        }

        // Could not load
        return null;
    }
}
