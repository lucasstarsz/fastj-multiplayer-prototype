package game;

import tech.fastj.systems.audio.AudioManager;
import tech.fastj.systems.audio.StreamedAudio;

import javax.sound.sampled.FloatControl;
import java.util.ArrayList;
import java.util.List;

import util.FilePaths;

public class MusicManager {

    public static final float InitialAudioLevel = 0.7f;

    private final List<StreamedAudio> audio;
    private final StreamedAudio trailblaze;

    public MusicManager(float initialAudioLevel) {
        audio = new ArrayList<>();
        trailblaze = AudioManager.loadStreamedAudio(FilePaths.Trailblaze);
        trailblaze.getAudioEventListener().setAudioStopAction(lineEvent -> trailblaze.play());
        audio.add(trailblaze);

        FloatControl gain = trailblaze.gainControl();
        float gainValue = 20f * (float) Math.log10(initialAudioLevel);
        gain.setValue(gainValue);
    }

    public void playMainMusic() {
        trailblaze.play();
    }

    public void pauseMainMusic() {
        trailblaze.pause();
    }

    public void setMusicLevel(float audioLevel) {
        for (StreamedAudio streamedAudio : audio) {
            streamedAudio.pause();
            streamedAudio.gainControl().setValue(20f * (float) Math.log10(audioLevel));
            streamedAudio.resume();
        }
    }

    public void unloadAll() {
        for (StreamedAudio streamedAudio : audio) {
            AudioManager.unloadStreamedAudio(streamedAudio.getID());
        }
    }
}
