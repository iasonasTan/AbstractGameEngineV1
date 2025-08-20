package com.engine.sound;

import com.engine.data.UniqueInsertMap;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Stores sounds with IDs so they can be played at any time.
 * When playing clip, previous playing clip stops immediately.
 */
public class SoundMap {
    /**
     * If set to {@code true} no sound will not play but no exception will be thrown.<br>
     * Adding and removing sounds is still possible.
     */
    public static boolean sMuted=false;

    /**
     * {@link UniqueInsertMap} contains audio input streams and their IDs.
     */
    private UniqueInsertMap<String, URL> mURlsMap =new UniqueInsertMap<>();

    /**
     * Constructor
     */
    public SoundMap() {
    }

    /**
     * Plays sound with given ID if {@link #sMuted} is {@code false}.
     * @param soundID ID of sound to play
     * @return {@code false} if given ID sound is not found, false otherwise.
     */
    public boolean playSound(String soundID) {
        if(!mURlsMap.containsKey(soundID)||sMuted)
            return false;
        try {
            Clip clip=AudioSystem.getClip();
            AudioInputStream audioInputStream= AudioSystem.getAudioInputStream(mURlsMap.get(soundID));
            clip.open(audioInputStream);
            clip.start();
            clip.addLineListener(event -> {
                if(event.getType()== LineEvent.Type.STOP)
                    clip.close();
            });
            return true;
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes sound with given ID from sounds.
     * @param id id of sound to remove.
     */
    public void removeSound(String id) {
        mURlsMap.remove(id);
    }

    /**
     * Adds audio input stream from taken url, and it's ID to streams map.
     * Use {@link #playSound(String)} with an ID to play sound.
     * Each ID can be used only once.
     * @see UniqueInsertMap#putPair(Object, Object).
     * @param id id of the sound, used to play sound later.
     * @param urlToSounds url to file.
     * @throws IllegalArgumentException If SoundMap is closed.
     * @throws UnsupportedAudioFileException If audio file is unsupported.
     * @throws IOException When I/O error occurs.
     */
    public void addSound(String id, URL urlToSounds) throws IllegalArgumentException, UnsupportedAudioFileException, IOException {
        try {
            mURlsMap.putPair(id, urlToSounds);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("ID already in use by "+ mURlsMap.get(id), iae);
        }
    }

}
