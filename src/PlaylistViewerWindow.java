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

    public PlaylistViewerWindow(String playlistName, LinkedList<Song> playlist, MusicPlayer musicPlayer, MusicPlayerGUI musicPlayerGUI, MusicLibraryWindow musicLibraryWindow) {
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
            renderSongPanels(playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI);
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
        renderSongPanels(playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI);

        // Show the window
        setVisible(true);

        // --- Currently Playing ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.setBackground(new Color(245, 245, 245));

        JLabel nowPlayingLabel = new JLabel("🎵 Now Playing: ");
        nowPlayingText = new JLabel("No song playing");
        nowPlayingText.setFont(new Font("Dialog", Font.BOLD, 14));

        bottomPanel.add(nowPlayingLabel, BorderLayout.WEST);
        bottomPanel.add(nowPlayingText, BorderLayout.CENTER);

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
            if (current != null) {
                // Is currently play a playlist
                if (musicPlayer.playingFromPlaylist){
                    String text = String.format("🎵 Now Playing from %s: ", playlistName);
                    nowPlayingLabel.setText(text);
                }
                else nowPlayingLabel.setText("🎵 Now Playing: ");

                String display = current.getSongTitle() + " - " + current.getSongArtist();
                if (!nowPlayingText.getText().equals(display)) {
                    nowPlayingText.setText(display);
                }
            } else {
                if (!nowPlayingText.getText().equals("No song playing")) {
                    nowPlayingText.setText("No song playing");
                }
            }
        });
        songChecker.start();
    }



    private void renderSongPanels(LinkedList<Song> playlist, MusicPlayer musicPlayer, MusicPlayerGUI musicPlayerGUI) {
        songListPanel.removeAll();

        for (Song song : playlist) {
            JPanel songPanel = new JPanel(new BorderLayout());
            songPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            songPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            songPanel.setBackground(Color.WHITE);
            songPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Mouse click behavior
            songPanel.addMouseListener(new MouseAdapter() {
                private long lastClickTime = 0;

                @Override
                public void mouseClicked(MouseEvent e) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime < 400) {
                        // Double click = play
                        musicPlayer.setIndex(song); // Set the playlist index to the song
                        musicPlayer.playCurrentPlaylist();
                    } else {
                        // Single click = select
                        if (selectedPanel != null) {
                            selectedPanel.setBackground(Color.WHITE);
                        }
                        songPanel.setBackground(new Color(220, 220, 255));
                        selectedPanel = songPanel;
                    }
                    lastClickTime = currentTime;
                }
            });

            // --- Image / Cover ---
            JLabel imageLabel = new JLabel();
            BufferedImage cover = song.getCoverImage();
            if (cover != null) {
                imageLabel.setIcon(new ImageIcon(cover.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            } else {
                imageLabel.setIcon(new ImageIcon(new ImageIcon("src/assets/record.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            }
            imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            songPanel.add(imageLabel, BorderLayout.WEST);

            // --- Title + Artist ---
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(song.getSongTitle());
            titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));

            JLabel artistLabel = new JLabel(song.getSongArtist());
            artistLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            artistLabel.setForeground(Color.GRAY);

            infoPanel.add(titleLabel);
            infoPanel.add(artistLabel);
            songPanel.add(infoPanel, BorderLayout.CENTER);

            // --- 3-dot menu button ---
            JButton menuButton = new JButton("⋮");
            menuButton.setFont(new Font("Dialog", Font.BOLD, 16));
            menuButton.setFocusPainted(false);
            menuButton.setBorderPainted(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setOpaque(false);
            menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Popup menu
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem queueItem = new JMenuItem("Queue");
            queueItem.addActionListener(e -> {
                musicPlayer.addToQueue(song);

            });
            JMenuItem playlistItem = new JMenuItem("Add to Playlist");
            playlistItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setFileFilter(new FileNameExtensionFilter("playlist", "txt"));
                    jFileChooser.setCurrentDirectory(new File("src/playlist"));

                    int result = jFileChooser.showOpenDialog(musicPlayerGUI);
                    File selectedFile = jFileChooser.getSelectedFile();

                    if(result == JFileChooser.APPROVE_OPTION && selectedFile != null && musicPlayer.loadPlaylist(selectedFile)){
                        // now we will write all of the song paths into this file
                        FileWriter fileWriter = null;
                        try {
                            fileWriter = new FileWriter(selectedFile, true);

                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                            bufferedWriter.write(song.getSongTitle() + "\n");
                            bufferedWriter.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        // Reload playlist
                        musicPlayer.loadPlaylist(selectedFile);

                    } else{
                        JOptionPane.showMessageDialog(PlaylistViewerWindow.this,
                                "Playlist file cannot be loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });

            JMenuItem removeItem = new JMenuItem("Remove");

            popupMenu.add(queueItem);
            popupMenu.add(playlistItem);
            popupMenu.add(removeItem);

            // remove
            removeItem.addActionListener(e -> {
                playlist.remove(song);
                renderSongPanels(playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI);
            });

            // Show popup on click
            menuButton.addActionListener(e -> popupMenu.show(menuButton, 0, menuButton.getHeight()));

            songPanel.add(menuButton, BorderLayout.EAST);

            songListPanel.add(songPanel);
        }

        songListPanel.revalidate();
        songListPanel.repaint();
    }

}
