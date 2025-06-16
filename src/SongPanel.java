import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

interface SongClickHandler {
    void onClick(Song song);         // single click
    void onDoubleClick(Song song);                 // double click
    void onAddToQueue(Song song);
    void onAddToPlaylist(Song song);
    void onRemove(Song song);
}


public abstract class SongPanel extends JPanel implements SongClickHandler{
    private long lastClickTime = 0;
    private final Song song;

    public SongPanel(Song song) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        setBackground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.song = song;

        // --- Image ---
        JLabel imageLabel = new JLabel();
        BufferedImage cover = song.getCoverImage();
        if (cover != null) {
            imageLabel.setIcon(new ImageIcon(cover.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        } else {
            imageLabel.setIcon(new ImageIcon(
                    new ImageIcon("src/assets/record.png").getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        add(imageLabel, BorderLayout.WEST);

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
        add(infoPanel, BorderLayout.CENTER);

        JButton rightButton =getRightButton();

        add(rightButton, BorderLayout.EAST);

        // --- Click listener ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < 400) {
                    onDoubleClick(song);
                } else {
                    onClick(song);
                }
                lastClickTime = currentTime;
            }
        });
    }

    public JButton getRightButton(){
        // --- 3-dot menu ---
        JButton menuButton = new JButton("⋮");
        menuButton.setFont(new Font("Dialog", Font.BOLD, 16));
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setOpaque(false);
        menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem queueItem = new JMenuItem("Add to Queue");
        queueItem.addActionListener(e -> onAddToQueue(song));
        popupMenu.add(queueItem);

        JMenuItem playlistItem = new JMenuItem("Add to Playlist");
        playlistItem.addActionListener(e -> onAddToPlaylist(song));
        popupMenu.add(playlistItem);

        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(e -> onRemove(song));
        popupMenu.add(removeItem);


        menuButton.addActionListener(e -> popupMenu.show(menuButton, 0, menuButton.getHeight()));

        return menuButton;
    }
}
