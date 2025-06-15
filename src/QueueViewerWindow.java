import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class QueueViewerWindow extends JFrame {
    private JPanel queuePanel;
    private MusicPlayer musicPlayer;
    private LinkedList<Song> songQueue;

    public QueueViewerWindow(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;

        setTitle("Now Playing Queue");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Now Playing Queue", SwingConstants.CENTER);
        header.setFont(new Font("Dialog", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        queuePanel = new JPanel();
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(queuePanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        renderQueue();
    }

    private void renderQueue() {
        queuePanel.removeAll();
        List<Song> queue = musicPlayer.getQueue();
        Song playingSong = musicPlayer.getCurrentSong();

        for (Song song : queue) {
            JPanel songPanel = new JPanel(new BorderLayout());
            songPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            songPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            songPanel.setBackground(Color.WHITE);
            songPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Cover image
            JLabel imageLabel = new JLabel();
            BufferedImage cover = song.getCoverImage();
            if (cover != null) {
                imageLabel.setIcon(new ImageIcon(cover.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            } else {
                imageLabel.setIcon(new ImageIcon(new ImageIcon("src/assets/record.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
            }
            imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
            songPanel.add(imageLabel, BorderLayout.WEST);

            // Title and artist
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

            // Remove button
            JButton removeButton = new JButton("❌");
            removeButton.setFocusPainted(false);
            removeButton.setBorderPainted(false);
            removeButton.setContentAreaFilled(false);
            removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            removeButton.addActionListener((ActionEvent e) -> {
                musicPlayer.removeFromQueue(song);
                renderQueue();
            });
            songPanel.add(removeButton, BorderLayout.EAST);

            // Highlight selection
            songPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Optional: single click highlight
                    for (Component comp : queuePanel.getComponents()) {
                        comp.setBackground(Color.WHITE);
                    }
                    songPanel.setBackground(new Color(220, 220, 255));
                }
            });



            queuePanel.add(songPanel);
        }

        queuePanel.revalidate();
        queuePanel.repaint();
    }

    public void updateQueueUI(List<Song> updatedQueue) {
        this.songQueue = new LinkedList<>(updatedQueue);  // Update local copy
        renderQueue();  // Re-render song list panel


    }
}
