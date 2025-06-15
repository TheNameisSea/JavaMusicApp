import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

public class Song {

    private String songTitle;
    private String songArtist;
    private String songLength;
    private String filePath;
    private BufferedImage coverImage;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;

    public Song(String filePath) {
        this.filePath = filePath;
        try {
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            songLength = convertToSongLengthFormat();

            // Create audio file obj
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // Get metadata
            Tag tag = audioFile.getTag();
            if (tag != null) {
                songTitle = tag.getFirst(FieldKey.TITLE);
                songArtist = tag.getFirst(FieldKey.ARTIST);

                // Get artwork (cover image)
                List<Artwork> artworkList = tag.getArtworkList();
                if (artworkList != null && !artworkList.isEmpty()) {
                    byte[] imageData = artworkList.getFirst().getBinaryData();
                    coverImage = ImageIO.read(new ByteArrayInputStream(imageData));
                }

            } else {
                songTitle = "N/A";
                songArtist = "N/A";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertToSongLengthFormat(){
        long minutes = mp3File.getLengthInSeconds()/60;
        long seconds = mp3File.getLengthInSeconds()%60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        return formattedTime;
    }

    // Getters

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongLength() {
        return songLength;
    }

    public String getFilePath() {
        return filePath;
    }

    public BufferedImage getCoverImage() {
        return coverImage;
    }

    public Mp3File getMp3File() {
        return mp3File;
    }

    public double getFrameRatePerMilliseconds() {
        return frameRatePerMilliseconds;
    }


    public String toString(){
        return this.songTitle;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Song other = (Song) obj;
        return filePath.equals(other.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

}
