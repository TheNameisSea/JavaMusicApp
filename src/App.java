import javax.swing.*;

public class App {
    public static void main(String[] args) {

        /* Use the invokeLater method to ensure that our GUI
        is executed on the Event Dispatch Thread in Swing*/
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MusicPlayerGUI().setVisible(true);
            }
        });
    }
}
