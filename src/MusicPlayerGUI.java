import javax.swing.*;

public class MusicPlayerGUI extends JFrame {
    public MusicPlayerGUI(){
        // Use JFrame constructor to configure the GUI
        super ("Music Player");

        // Set w and h
        setSize(400, 600);

        // End process if app is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Launch app at screen center
        setLocationRelativeTo(null);

        // prevent the app from being resized
        setResizable(false);

        setLayout(null);


    }
}
