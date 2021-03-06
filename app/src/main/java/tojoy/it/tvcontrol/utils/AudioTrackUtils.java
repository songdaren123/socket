package tojoy.it.tvcontrol.utils;

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
    private String TAG = this.getClass().getSimpleName();
    private AudioTrack mAudioTrack;
    private static AudioTrackInterface mAudioTrackInterface;
    private volatile int len = 0;

    public AudioTrackUtils() {
        int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
    }

    public synchronized void play() {
        mAudioTrack.play();
    }

    public synchronized void stop() {
        try {
            mAudioTrack.stop();
            mAudioTrack.release();
            len = -1;
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

    public synchronized void write(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
        mAudioTrack.flush();
    }


    public synchronized int write() {
        LogUtil.logd(TAG, "write: 无参数");
        LogUtil.logd(TAG, "write: audioTrackInterface:" + mAudioTrackInterface);
        if (mAudioTrackInterface != null) {
            byte[] bt = new byte[1024 * 2];
            len = 0;
            len = mAudioTrackInterface.getAudioData(bt, 0, bt.length);
            mAudioTrack.write(bt, 0, len);
            mAudioTrack.flush();

        }
        return len;
    }

    public interface AudioTrackInterface {
        int getAudioData(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes);
    }

    public void setAudioTrackInterface(AudioTrackInterface audioTrackInterface) {
        mAudioTrackInterface = audioTrackInterface;
    }

}
