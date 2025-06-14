import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicLibraryWindow extends JFrame {

    private final MusicPlayerGUI musicPlayerGUI;
    private JPanel selectedPanel = null;
    private final MusicPlayer musicPlayer;
    private final ArrayList<Song> allSongs;
    private final ArrayList<Song> displayedSongs;
    private final JPanel songListPanel;
    private final JTextField searchBar;

    // HashMap to store song title -> file path
    public static final HashMap<String, String> songMap = new HashMap<>();
    private final SongTree songTreeNew = new SongTree();

    public MusicLibraryWindow(MusicPlayerGUI musicPlayerGUI, MusicPlayer musicPlayer) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.musicPlayer = musicPlayer;
        this.allSongs = new ArrayList<>();
        this.displayedSongs = new ArrayList<>();

        setTitle("Music Library");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Top Bar (Search + Add) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        JPanel rightTopPanel = new JPanel(new BorderLayout());

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

        JButton searchBtn = new JButton("Search");
        JButton searchClosestBtn = new JButton("Search Closest");
        searchBtn.addActionListener(e -> performSearch());
        searchClosestBtn.addActionListener(e -> performClosestSearch());
        rightTopPanel.add(searchBtn, BorderLayout.SOUTH);
        rightTopPanel.add(searchClosestBtn, BorderLayout.NORTH);
        topPanel.add(rightTopPanel, BorderLayout.EAST);


        JButton addSongBtn = new JButton("＋");
        addSongBtn.setToolTipText("Add Song");
        addSongBtn.setMargin(new Insets(0, 5, 0, 5));
        addSongBtn.addActionListener(e -> addSongToLibrary());
        topPanel.add(addSongBtn, BorderLayout.WEST);



        add(topPanel, BorderLayout.NORTH);

        // --- Center Song List ---
        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(songListPanel);
        // Increase vertical scroll speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);


//        setVisible(true);
        loadSongsFromLibraryFolder();
        renderSongList();
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
                allSongs.add(song);
                songMap.put(song.getSongTitle(), path);
                songTreeNew.insert(song);
            }
        }

        displayedSongs.clear();
        displayedSongs.addAll(allSongs);
    }

    private void renderSongList() {
        songListPanel.removeAll();

        for (Song song : displayedSongs) {
            JPanel songPanel = new JPanel(new BorderLayout());
            songPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            songPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            songPanel.setBackground(Color.WHITE);
            songPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Mouse listener for selection and playback
            songPanel.addMouseListener(new MouseAdapter() {
                private long lastClickTime = 0;

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Double click = play song
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime < 400) {  // Double click threshold
                        musicPlayer.loadSong(song);
                        musicPlayerGUI.updateSongTitleAndArtist(song);
                        musicPlayerGUI.updatePlaybackSlider(song);
                        musicPlayerGUI.updateCoverImage(song);
                        musicPlayerGUI.enablePauseButtonDisablePlayButton();
                    } else {
                        // Single click = select and highlight
                        if (selectedPanel != null) {
                            selectedPanel.setBackground(Color.WHITE);  // Deselect previous
                        }
                        songPanel.setBackground(new Color(220, 220, 255));  // Light blue highlight
                        selectedPanel = songPanel;
                    }
                    lastClickTime = currentTime;
                }
            });

            // Image
            JLabel imageLabel = new JLabel();
            BufferedImage cover = song.getCoverImage();
            if (cover != null) {
                imageLabel.setIcon(new ImageIcon(cover.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            } else {
                imageLabel.setIcon(new ImageIcon(new ImageIcon("src/assets/record.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            }
            imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            songPanel.add(imageLabel, BorderLayout.WEST);

            // Title + Artist
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);  // Don't override panel bg

            JLabel titleLabel = new JLabel(song.getSongTitle());
            titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));

            JLabel artistLabel = new JLabel(song.getSongArtist());
            artistLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            artistLabel.setForeground(Color.GRAY);

            infoPanel.add(titleLabel);
            infoPanel.add(artistLabel);
            songPanel.add(infoPanel, BorderLayout.CENTER);

            // Delete button
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.addActionListener(e -> {
                File songFile = new File(song.getFilePath());
//                if (songFile.exists()) songFile.delete();

                allSongs.remove(song);
                displayedSongs.remove(song);
                songMap.remove(song.getSongTitle());
                songTreeNew.delete(song);
                renderSongList();
            });
            songPanel.add(deleteBtn, BorderLayout.EAST);

            songListPanel.add(songPanel);
        }

        songListPanel.revalidate();
        songListPanel.repaint();
    }

    private void performSearch() {
        String keyword = searchBar.getText();
        displayedSongs.clear();

        if (keyword.isEmpty()) {
            displayedSongs.addAll(allSongs);
        } else {
            ArrayList<Song> matches = songTreeNew.searchNearestLexico(keyword, 3);
            displayedSongs.addAll(matches);
        }
        renderSongList();
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

    // --- Add Song Function ---
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
}
