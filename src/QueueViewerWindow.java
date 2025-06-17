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
    private JPanel selectedPanel = null;

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
            QueueSongPanel queueSongPanel = new QueueSongPanel(song, musicPlayer, this) {
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

                }

                @Override
                public void onRemove(Song song) {
                    musicPlayer.removeFromQueue(song);
                    renderQueue();
                }
            };

            queuePanel.add(queueSongPanel);
        }

        queuePanel.revalidate();
        queuePanel.repaint();
    }

    public void updateQueueUI(List<Song> updatedQueue) {
        this.songQueue = new LinkedList<>(updatedQueue);  // Update local copy
        renderQueue();  // Re-render song list panel


    }

    public LinkedList<Song> getSongQueue() {
        return songQueue;
    }
}
