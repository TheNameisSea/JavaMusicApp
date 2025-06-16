import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class LibrarySongPanel extends SongPanel {
    private final MusicPlayer musicPlayer;
    private final QueueViewerWindow queueViewerWindow;
    private final JFrame parent;

    public LibrarySongPanel(Song song, JFrame parent, MusicPlayer musicPlayer, QueueViewerWindow queueViewerWindow){
        super(song);
        this.musicPlayer = musicPlayer;
        this.queueViewerWindow = queueViewerWindow;
        this.parent = parent;
    }


    @Override
    public void onAddToPlaylist(Song song) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("playlist", "txt"));
        jFileChooser.setCurrentDirectory(new File("src/playlist"));

        int result = jFileChooser.showOpenDialog(parent);
        File selectedFile = jFileChooser.getSelectedFile();

        if (result == JFileChooser.APPROVE_OPTION && selectedFile != null && musicPlayer.loadPlaylist(selectedFile)) {
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

        } else {
            JOptionPane.showMessageDialog(parent,
                    "Playlist file cannot be loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
        }

    }

    @Override
    public void onAddToQueue(Song song) {
        musicPlayer.addToQueue(song);
        if (queueViewerWindow != null && queueViewerWindow.isDisplayable()) {
            queueViewerWindow.updateQueueUI(musicPlayer.getQueue());
        }
    }
}