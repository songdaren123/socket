package tojoy.it.tvcontrol;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @ClassName: AudioTrackUtils
 * @Description: 语音转文字的播放器
 * @Author: songdaren
 * @CreateDate: 2020/4/28 11:36 AM
 */
public class AudioTrackUtils {
    private AudioTrack track;

    public AudioTrackUtils() {
        int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
    }

    public synchronized void play() {
        track.play();
    }

    public synchronized void stop() {
        try {
            track.stop();
            track.release();
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

    public synchronized void write(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        track.write(audioData, offsetInBytes, sizeInBytes);
        track.flush();
    }
}
