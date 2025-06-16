import javax.swing.*;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class App {
    public static void main(String[] args) {

        FlatLightLaf.setup();
        /* Use the invokeLater method to ensure that our GUI
        is executed on the Event Dispatch Thread in Swing*/
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

////                musicPlayerGUI.setVisible(true);
                MusicLibraryWindow musicLibraryWindow = new MusicLibraryWindow(new MusicPlayerGUI());
                musicLibraryWindow.setVisible(true);

                

//                Song song = new Song("src/assets/Tetoris-_-テトリス-_-重音テトSV.mp3");
//                System.out.println(song.getSongTitle());
//                System.out.println(song.getSongArtist());
            }
        });
    }
}
