import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class QueueSongPanel extends SongPanel{
    private final MusicPlayer musicPlayer;
    private final QueueViewerWindow queueViewerWindow;
    private final Song song;

    public QueueSongPanel(Song song, MusicPlayer musicPlayer, QueueViewerWindow queueViewerWindow){
        super(song);
        this.song = song;
        this.musicPlayer = musicPlayer;
        this.queueViewerWindow = queueViewerWindow;
    }

    @Override
    public JButton getRightButton(){
        // Remove button
        JButton removeButton = new JButton("❌");
        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener((ActionEvent e) -> {
            onRemove(song);
        });
        return removeButton;
    }

    @Override
    public void onAddToPlaylist(Song song) {

    }

    @Override
    public void onAddToQueue(Song song) {

    }
}
