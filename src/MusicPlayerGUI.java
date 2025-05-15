import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

public class MusicPlayerGUI extends JFrame {

    // Color constant
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    // Allow file explorer
    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;



    public MusicPlayerGUI(){
        // Use JFrame constructor to configure the GUI
        super ("Music Player");

        // Set w and h
        setSize(400, 600);

        // End process if app is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Launch app at screen center
        setLocationRelativeTo(null);

        // prevent the app from being resized
        setResizable(false);

        setLayout(null);

        // Change color
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer();
        jFileChooser = new JFileChooser();

        // Set default path for file explorer
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        // Filter to only see mp3 file
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));


        addGuiComponents();

    }

    private void addGuiComponents(){
        // Add toolbar
        addToolbar();

        // Load music image
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth()-20, 225);
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
        JSlider playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
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

                    // Load song
                    musicPlayer.loadSong(song);

                    // Update song metadata
                    updateSongTitleAndArtist(song);

                    // Toggle Play Pause button
                    enablePauseButtonDisablePlayButton();

                }

            }
        });
        songMenu.add(loadSong);

        // Add playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
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
        playbackBtns.add(prevButton);

        // Play button
        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
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
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    private void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    private void enablePauseButtonDisablePlayButton(){

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

    private void enablePlayButtonDisablePauseButton(){

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

    private ImageIcon loadImage(String imagePath){
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
