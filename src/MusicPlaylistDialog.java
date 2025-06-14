import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicPlaylistDialog extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;

    // store all of the song's name to be written to a txt file (when we load a playlist)
    private ArrayList<String> songNames;

    public MusicPlaylistDialog(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
        songNames = new ArrayList<>();


        // configure dialog
        setTitle("Create Playlist");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGUI.FRAME_COLOR);
        setLayout(null);
        setModal(true); // this property makes it so that the dialog has to be closed to give focus
        setLocationRelativeTo(musicPlayerGUI);

        addDialogComponents();
    }

    private void addDialogComponents(){
        // container to hold each song path
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int)(getWidth() * 0.025), 10, (int)(getWidth() * 0.90), (int) (getHeight() * 0.75));
        add(songContainer);

        // add song button
        JButton addSongButton = new JButton("Add");
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 14));
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open file explorer
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
                jFileChooser.setCurrentDirectory(new File("src/Library"));
                int result = jFileChooser.showOpenDialog(MusicPlaylistDialog.this);

                File selectedFile = jFileChooser.getSelectedFile();
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    String name = selectedFile.getName().replace(".mp3", "");
                    JLabel filePathLabel = new JLabel(name);
                    filePathLabel.setFont(new Font("Dialog", Font.BOLD, 15));
                    filePathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    filePathLabel.setMaximumSize(new Dimension(songContainer.getWidth(), 40));
                    filePathLabel.setVerticalAlignment(SwingConstants.CENTER);
                    filePathLabel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK), // underline only
                            BorderFactory.createEmptyBorder(5, 10, 5, 10) // inner padding
                    ));

                    // add to the list

                    songNames.add(filePathLabel.getText());

                    // add to container
                    songContainer.add(filePathLabel);

                    // refreshes dialog to show newly added JLabel
                    songContainer.revalidate();
                }
            }
        });
        add(addSongButton);

        // save playlist button
        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setCurrentDirectory(new File("src/playlist"));
                    int result = jFileChooser.showSaveDialog(MusicPlaylistDialog.this);

                    if(result == JFileChooser.APPROVE_OPTION){
                        // we use getSelectedFile() to get reference to the file that we are about to save
                        File selectedFile = jFileChooser.getSelectedFile();

                        // convert to .txt file if not done so already
                        // this will check to see if the file does not have the ".txt" file extension
                        if(!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")){
                            selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                        }

                        // create the new file at the destinated directory
                        selectedFile.createNewFile();

                        // now we will write all of the song paths into this file
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        // iterate through our song paths list and write each string into the file
                        // each song will be written in their own row
                        for(String songPath : songNames){
                            bufferedWriter.write(songPath + "\n");
                        }
                        bufferedWriter.close();

                        // display success dialog
                        JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "Successfully Created Playlist!");

                        // close this dialog
                        MusicPlaylistDialog.this.dispose();
                    }
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }
        });
        add(savePlaylistButton);
    }
}
