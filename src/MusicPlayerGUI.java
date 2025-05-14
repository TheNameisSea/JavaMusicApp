import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MusicPlayerGUI extends JFrame {

    // Color constant
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;



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

        // Change color
        getContentPane().setBackground(FRAME_COLOR);


        addGuiComponents();

    }

    private void addGuiComponents(){
        // Add toolbar
        addToolbar();

        // Load music image
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth()-20, 225);
        add(songImage);

    }

    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        // Prevent toolbar from being moved
        toolBar.setFloatable(false);

        // Add drop down menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // Add song menu
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        // Add option to load song
        JMenuItem loadSong = new JMenuItem("Load Song");
        songMenu.add(loadSong);

        // Add playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        playlistMenu.add(loadPlaylist);

        add(toolBar);



    }

    private ImageIcon loadImage(String imagePath){
        try {
            // Read image file from path
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);

        }catch (Exception e){
            e.printStackTrace();
        }

        // Could not load
        return null;
    }
}
