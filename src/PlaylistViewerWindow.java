import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

public class PlaylistViewerWindow extends JFrame {
    private JPanel songListPanel;
    private JPanel selectedPanel = null;
    private JPanel buttonPanel;
    private JLabel nowPlayingText;
    private JLabel nowPlayingLabel;
    private String playlistName;
    private MusicPlayer musicPlayer;

    public PlaylistViewerWindow(String playlistName, LinkedList<Song> playlist, MusicPlayer musicPlayer, MusicPlayerGUI musicPlayerGUI, MusicLibraryWindow musicLibraryWindow) {
        this.playlistName = playlistName;
        this.musicPlayer = musicPlayer;

        setTitle("Current Playlist - " + playlistName);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // === TOP CONTAINER: holds Back button and topPanel ===
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        add(topContainer, BorderLayout.NORTH);

        // Top button rows
        JPanel topButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0 , 0));

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            musicLibraryWindow.setGUIVisible(true);
            setVisible(false);

        });
        JButton viewQueue = new JButton("View Queue");
        viewQueue.addActionListener(e -> musicLibraryWindow.showQueueViewer());


        topButtonRow.add(backButton);
        topButtonRow.add(viewQueue);
        topButtonRow.setBounds(0, 0, topButtonRow.getWidth(), 20);


        // --- Top: Header with Info + Buttons ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel to hold playlist name and song count
        JPanel playlistTextPanel = new JPanel();
        playlistTextPanel.setLayout(new BoxLayout(playlistTextPanel, BoxLayout.Y_AXIS));
        playlistTextPanel.setOpaque(false); // Match background with parent

        // Playlist name
        JLabel playlistNameLabel = new JLabel(playlistName);
        playlistNameLabel.setFont(new Font("Dialog", Font.BOLD, 16));

        // Song count (e.g., "5 Songs")
        JLabel songCountLabel = new JLabel(playlist.size() + " Songs");
        songCountLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        songCountLabel.setForeground(Color.GRAY);

        // Add both labels to the panel
        playlistTextPanel.add(playlistNameLabel);
        playlistTextPanel.add(songCountLabel);

        topPanel.add(playlistTextPanel, BorderLayout.WEST);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton shuffleButton = new JButton("Shuffle");
        shuffleButton.addActionListener(e -> {
            Collections.shuffle(playlist);
            musicPlayer.setIndex(musicPlayer.getIndex(musicPlayer.getCurrentSong()));
            renderSongPanels(playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI, musicLibraryWindow);
        });
        JButton playButton = new JButton("▶");
        playButton.addActionListener(e -> {
            musicPlayer.setIndex(0);
            musicPlayer.playCurrentPlaylist();
        });

        buttonPanel.add(shuffleButton);
        buttonPanel.add(playButton);


        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Add to topContainer and the window
        topContainer.add(topButtonRow, BorderLayout.NORTH);
        topContainer.add(topPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // --- Center: Song List Panel ---
        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(songListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Render each song
        renderSongPanels(playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI, musicLibraryWindow);

        // Show the window
        setVisible(true);

        // --- Currently Playing ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.setBackground(new Color(245, 245, 245));

        nowPlayingLabel = new JLabel();
        nowPlayingText = new JLabel();
        nowPlayingText.setFont(new Font("Dialog", Font.BOLD, 14));

        bottomPanel.add(nowPlayingLabel, BorderLayout.WEST);
        bottomPanel.add(nowPlayingText, BorderLayout.CENTER);

        updateNowPlayingSong(musicPlayer.getCurrentSong());

        bottomPanel.addMouseListener(new MouseAdapter() {
            private long lastClickTime = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double click = play song
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < 400) {  // Double click threshold
                    musicPlayerGUI.setVisible(true);
                }
                lastClickTime = currentTime;
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);

        // Timer to monitor current playing song
        Timer songChecker = new Timer(1000, e -> {
            Song current = musicPlayer.getCurrentSong();
            updateNowPlayingSong(current);
        });
        songChecker.start();
    }



    private void renderSongPanels(LinkedList<Song> playlist, MusicPlayer musicPlayer, MusicPlayerGUI musicPlayerGUI, MusicLibraryWindow musicLibraryWindow) {
            songListPanel.removeAll();

            for (Song song : playlist){
                SongPanel panel = new LibrarySongPanel(song, this, musicPlayer, musicLibraryWindow.getQueueViewerWindow()){
                    @Override
                    public void onClick(Song song) {
                        if (selectedPanel != null) {
                            selectedPanel.setBackground(Color.WHITE);
                        }
                        setBackground(new Color(220, 220, 255));
                        selectedPanel = this;
                    }

                    @Override
                    public void onDoubleClick(Song song) {
                        musicPlayer.setIndex(song); // Set the playlist index to the song
                        musicPlayer.playCurrentPlaylist();
                        updateNowPlayingSong(song);
                    }
                    @Override
                    public void onRemove(Song song) {
                        File songFile = new File(song.getFilePath());
                        if (songFile.exists()) songFile.delete();
                        playlist.remove(song);
                        renderSongPanels(playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI, musicLibraryWindow);
                    }
                };

                songListPanel.add(panel);
            }

            songListPanel.revalidate();
            songListPanel.repaint();
    }

    private void updateNowPlayingSong(Song song){
        if (song != null) {
            // Is currently play a playlist
            if (musicPlayer.playingFromPlaylist){
                String text = String.format("🎵 Now Playing from %s: ", playlistName);
                nowPlayingLabel.setText(text);
            }
            else nowPlayingLabel.setText("🎵 Now Playing: ");

            String display = song.getSongTitle() + " - " + song.getSongArtist();
            if (!nowPlayingText.getText().equals(display)) {
                nowPlayingText.setText(display);
            }
        } else if (!nowPlayingText.getText().equals("No song playing")) {
                nowPlayingText.setText("No song playing");
        }
    }

}