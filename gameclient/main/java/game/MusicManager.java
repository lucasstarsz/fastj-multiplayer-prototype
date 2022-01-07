package game;

import tech.fastj.systems.audio.AudioManager;
import tech.fastj.systems.audio.StreamedAudio;
import tech.fastj.systems.audio.state.PlaybackState;

import java.util.ArrayList;
import java.util.List;

import util.FilePaths;

public class MusicManager {

    public static final float InitialAudioLevel = 0.35f;

    private final List<StreamedAudio> music;
    private final StreamedAudio danceHype;

    public MusicManager(float initialAudioLevel) {
        music = new ArrayList<>();
        danceHype = AudioManager.loadStreamedAudio(FilePaths.DanceHype);
        danceHype.gainControl().setValue(20f * (float) Math.log10(initialAudioLevel));

        music.add(danceHype);
    }

    public void playMainMusic() {
        danceHype.play();
    }

    public void pauseMainMusic() {
        danceHype.pause();
    }

    public void setMusicLevel(float audioLevel) {
        for (StreamedAudio streamedAudio : music) {
            if (streamedAudio.getCurrentPlaybackState() != PlaybackState.Playing) {
                return;
            }

            streamedAudio.pause();
            streamedAudio.gainControl().setValue(20f * (float) Math.log10(audioLevel));
            streamedAudio.resume();
        }
    }

    public void unloadAll() {
        for (StreamedAudio streamedAudio : music) {
            AudioManager.unloadStreamedAudio(streamedAudio.getID());
        }
    }
}
