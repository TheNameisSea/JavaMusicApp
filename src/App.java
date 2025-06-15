import javax.swing.*;

public class App {
    public static void main(String[] args) {

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
