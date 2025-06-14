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

    private MusicPlayerGUI musicPlayerGUI;
    private JPanel selectedPanel = null;
    private MusicPlayer musicPlayer;
    private ArrayList<Song> allSongs;
    private ArrayList<Song> displayedSongs;
    private JPanel songListPanel;
    private JTextField searchBar;

    // HashMap to store song title -> file path
    private HashMap<String, String> songMap = new HashMap<>();
    private AVLTree songTree = new AVLTree();

    public MusicLibraryWindow(MusicPlayerGUI musicPlayerGUI, MusicPlayer musicPlayer, ArrayList<Song> songList) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.musicPlayer = musicPlayer;
        this.allSongs = new ArrayList<>(songList);
        this.displayedSongs = new ArrayList<>(songList);

        for (Song s : songList) {
            songMap.put(s.getSongTitle(), s.getFilePath());
        }

        setTitle("Music Library");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Top Bar (Search + Add) ---
        JPanel topPanel = new JPanel(new BorderLayout());

        searchBar = new JTextField();
        topPanel.add(searchBar, BorderLayout.CENTER);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> performSearch());
        topPanel.add(searchBtn, BorderLayout.EAST);

        JButton addSongBtn = new JButton("＋");
        addSongBtn.setToolTipText("Add Song");
        addSongBtn.addActionListener(e -> addSongToLibrary());
        topPanel.add(addSongBtn, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        // --- Center Song List ---
        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(songListPanel);
        add(scrollPane, BorderLayout.CENTER);


        setVisible(true);
        loadSongsFromLibraryFolder();
        renderSongList();
    }

    private void loadSongsFromLibraryFolder() {
        File libraryDir = new File("./Library");
        if (!libraryDir.exists()) {
            libraryDir.mkdir();
        }

        File[] files = libraryDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files == null) return;

        allSongs.clear();
        songMap.clear();

        for (File file : files) {
            String title = file.getName().replace(".mp3", "");  // strip extension
            String path = file.getAbsolutePath();

            // Basic Song object (artist unknown here)
            Song song = new Song(path);
            allSongs.add(song);
            songMap.put(title, path);
            songTree.insert(title);
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
                if (songFile.exists()) songFile.delete();

                allSongs.remove(song);
                displayedSongs.remove(song);
                songMap.remove(song.getSongTitle());
                renderSongList();
            });
            songPanel.add(deleteBtn, BorderLayout.EAST);

            songListPanel.add(songPanel);
        }

        songListPanel.revalidate();
        songListPanel.repaint();
    }

    private void performSearch() {
        String keyword = searchBar.getText().trim().toLowerCase();
        displayedSongs.clear();

        if (keyword.isEmpty()) {
            displayedSongs.addAll(allSongs);
        } else {
            ArrayList<String> matches = songTree.searchContains(keyword);
            for (String title : matches) {
                String path = songMap.get(title);
                if (path != null) {
                    displayedSongs.add(new Song(path));
                }
            }
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
                songTree.insert(newSong.getSongTitle());
            }

            renderSongList();
        }
    }
}
