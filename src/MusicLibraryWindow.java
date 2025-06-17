import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.FileWriter;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class MusicLibraryWindow extends JFrame {

    private final MusicPlayerGUI musicPlayerGUI;
    private QueueViewerWindow queueViewerWindow;
    private JPanel selectedPanel = null;
    private final MusicPlayer musicPlayer;
    private final ArrayList<Song> allSongs;
    private final ArrayList<Song> displayedSongs;
    private final JPanel songListPanel;
    private final JTextField searchBar;

    // HashMap to store song title -> file path
    public static final HashMap<String, String> songMap = new HashMap<>();
    private final SongTree songTreeNew = new SongTree();

    private final JLabel nowPlayingText;
    private final JLabel nowPlayingLabel;
    private boolean isAscending = true;  // Default is A → Z

    private String currentPlaylistName;

    public MusicLibraryWindow(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.musicPlayer = musicPlayerGUI.musicPlayer;
        this.allSongs = new ArrayList<>();
        this.displayedSongs = new ArrayList<>();

        setTitle("Music Library");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());




        // --- Top Bar (Search + Add + Menu) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        toolBar.setFloatable(false);
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        JPanel rightTopPanel = new JPanel(new BorderLayout());

        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);
        JMenuItem loadSong = new JMenuItem("Add Song");

        loadSong.addActionListener(e -> addSongToLibrary());
        songMenu.add(loadSong);

        JMenu queueMenu = new JMenu("Queue");
        menuBar.add(queueMenu);
        JMenuItem viewQueue = new JMenuItem("View Queue");
        viewQueue.addActionListener(e -> showQueueViewer());
        queueMenu.add(viewQueue);


        // Add playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        // Add current playlist
        JMenuItem currentPlaylist = new JMenuItem("View Current Playlist");
        playlistMenu.add(currentPlaylist);
        currentPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LinkedList<Song> playlist = musicPlayer.getPlaylist();

                if (playlist == null || playlist.isEmpty()) {
                    JOptionPane.showMessageDialog(MusicLibraryWindow.this,
                            "No playlist loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String playlistName = currentPlaylistName;

                new PlaylistViewerWindow(playlistName, playlist, musicPlayerGUI.musicPlayer, musicPlayerGUI, MusicLibraryWindow.this).setVisible(true);
                setVisible(false);
            }
        });

        // Add new playlist
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist dialog
                new MusicPlaylistDialog(musicPlayerGUI).setVisible(true);
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

                int result = jFileChooser.showOpenDialog(musicPlayerGUI);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null && musicPlayer.loadPlaylist(selectedFile)){
                    // load playlist
                    musicPlayer.loadPlaylist(selectedFile);

                    LinkedList<Song> playlist = musicPlayer.getPlaylist();
                    String playlistName = selectedFile.getName().replace(".txt", "");
                    currentPlaylistName = playlistName;
                    new PlaylistViewerWindow(playlistName, playlist, musicPlayerGUI.musicPlayer ,musicPlayerGUI, MusicLibraryWindow.this);
                    setVisible(false);
                } else{
                    JOptionPane.showMessageDialog(MusicLibraryWindow.this,
                            "Playlist file cannot be loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        playlistMenu.add(loadPlaylist);

        topPanel.add(toolBar, BorderLayout.NORTH);

        // Search Bar
        searchBar = new JTextField();
        searchBar.addActionListener(e -> performClosestSearch());

        // Button to clear the search bar
        JButton clearSearchBtn = new JButton("❌");
        clearSearchBtn.setToolTipText("Clear Search Bar");
        clearSearchBtn.setMargin(new Insets(0, 5, 0, 5));
        clearSearchBtn.addActionListener(e -> {
            searchBar.setText("");
            displayedSongs.clear();
            displayedSongs.addAll(allSongs);
            renderSongList();
            searchBar.requestFocusInWindow();
        });

        searchFieldPanel.add(searchBar, BorderLayout.CENTER);
        searchFieldPanel.add(clearSearchBtn, BorderLayout.EAST);

        topPanel.add(searchFieldPanel, BorderLayout.CENTER);

        // Search mechanism
        JButton searchClosestBtn = new JButton("Search");
        searchClosestBtn.addActionListener(e -> performClosestSearch());
        rightTopPanel.add(searchClosestBtn, BorderLayout.NORTH);
        topPanel.add(rightTopPanel, BorderLayout.EAST);




        // Sort panel above the songs
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.setBackground(Color.WHITE);

        JLabel sortLabel = new JLabel("Sort:");
        JButton sortToggleButton = new JButton("↓ A → Z");

        sortToggleButton.setBorderPainted(false);   // No border
        sortToggleButton.setContentAreaFilled(false); // No background fill
        sortToggleButton.setFocusPainted(false);    // No focus border when clicked
        sortToggleButton.setOpaque(false);

        sortToggleButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        sortToggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sortToggleButton.addActionListener(e -> {
            isAscending = !isAscending;
            Collections.reverse(displayedSongs);
            sortToggleButton.setText(isAscending ? "↓ A → Z" : "↑ Z → A");
            renderSongList();  // Re-render song list based on new order
        });

        sortPanel.add(sortLabel);
        sortPanel.add(sortToggleButton);

        topPanel.add(sortPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);




        // --- Center Song List ---
        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(songListPanel);
        // Increase vertical scroll speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- Currently Playing ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.setBackground(new Color(245, 245, 245));

        nowPlayingLabel = new JLabel("🎵 Now Playing: ");
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


        loadSongsFromLibraryFolder();
        renderSongList();

        checkCurrentSong();
    }

    public void checkCurrentSong(){
        // Timer to monitor current playing song
        Timer songChecker = new Timer(1000, e -> {
            Song current = musicPlayer.getCurrentSong();
//            queueViewerWindow = new QueueViewerWindow(musicPlayer);
//            queueViewerWindow.setVisible(true);

            if (queueViewerWindow != null && queueViewerWindow.isDisplayable()) {
                queueViewerWindow.updateQueueUI(musicPlayer.getQueue());
            }
            updateNowPlayingSong(current);
        });
        songChecker.start();
    }

    private void loadSongsFromLibraryFolder() {
        File libraryDir = new File("./src/Library");

        File[] lists = libraryDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));


        allSongs.clear();
        songMap.clear();

        if (lists != null) {
            for (File file : lists) {
                String path = file.getAbsolutePath();

                // Basic Song object (artist unknown here)
                Song song = new Song(path);
                songMap.put(song.getSongTitle(), path);
                songTreeNew.insert(song);
            }
        }
        allSongs.addAll(songTreeNew.getSortedSongList());
        displayedSongs.clear();
        displayedSongs.addAll(allSongs);
    }

    private void renderSongList() {
        songListPanel.removeAll();

        for (Song song : displayedSongs){
            SongPanel panel = new LibrarySongPanel(song, this, musicPlayer, queueViewerWindow){
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
                    musicPlayer.loadSong(song);
                    musicPlayerGUI.updateGUI(song);
                    updateNowPlayingSong(song);
                }
                @Override
                public void onRemove(Song song) {
                    File songFile = new File(song.getFilePath());
                    if (songFile.exists()) songFile.delete();
                    allSongs.remove(song);
                    displayedSongs.remove(song);
                    songMap.remove(song.getSongTitle());
                    songTreeNew.delete(song);
                    renderSongList();
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
                String text = String.format("🎵 Now Playing from %s: ", currentPlaylistName);
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

    private void performClosestSearch(){
        String keyword = searchBar.getText();
        displayedSongs.clear();

        if (keyword.isEmpty()) {
            displayedSongs.addAll(allSongs);
        } else {
            ArrayList<Song> matches = songTreeNew.searchClosestSongs(keyword, 3);
            displayedSongs.addAll(matches);
        }
        renderSongList();

    }

    private void addSongToLibrary() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            File libraryDir = new File("./Library");
            if (!libraryDir.exists()) libraryDir.mkdir();

            // Copy file to Library folder
            File destination = new File(libraryDir, selectedFile.getName());
            try {
                Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error copying file: " + e.getMessage());
                return;
            }

            // Add song to map and list
            Song newSong = new Song(destination.getAbsolutePath());
            if (!songMap.containsKey(newSong.getSongTitle())) {
                allSongs.add(newSong);
                displayedSongs.add(newSong);
                songMap.put(newSong.getSongTitle(), newSong.getFilePath());
                songTreeNew.insert(newSong);
            }

            renderSongList();
        }
    }

    public void showQueueViewer() {
        if (queueViewerWindow == null || !queueViewerWindow.isDisplayable()) {
            queueViewerWindow = new QueueViewerWindow(musicPlayer);
        } else {
            queueViewerWindow.updateQueueUI(musicPlayer.getQueue());
        }
        queueViewerWindow.setVisible(true);
    }

    public void setGUIVisible(boolean b){
        setVisible(b);
        checkCurrentSong();
    }

    public QueueViewerWindow getQueueViewerWindow(){
        return queueViewerWindow;
    }

}
