package tojoy.it.tvcontrol.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @ClassName: AudioRecoderManager
 * @Description: 录音的工具类
 * @Author: songdaren
 * @CreateDate: 2020/4/27 3:22 PM
 */
public class RecoderUtils {
    private String TAG = this.getClass().getSimpleName();
    private static final int sampleRateInHz = 16000;
    private static final int bufferSizeInBytes = 1024 * 2;
    private static final int audioSource = MediaRecorder.AudioSource.MIC;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static RecoderUtils instance;
    private AudioRecord audioRecord;
    private volatile boolean isRecorder = false;

    public static synchronized RecoderUtils newInstance() {
        if (instance == null) {
            instance = new RecoderUtils();
        }
        return instance;
    }

    private AudioRecord initAudioRecoder() {
        return new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    public void startRecoder() {
        if (!isRecorder) {
            try {
                isRecorder = true;
                audioRecord = initAudioRecoder();
                audioRecord.startRecording();
            } catch (Exception e) {
                Log.d(TAG, "startRecoder: " + e);
                audioRecord = null;
                isRecorder = false;
            }
        }
    }

    public int read(@NonNull byte[] bytes, int offsetInBytes, int sizeInBytes) {
        try {
            if (audioRecord != null && isRecorder) {
                return audioRecord.read(bytes, offsetInBytes, sizeInBytes);
            }
            return -1;
        } catch (Exception e) {
            Log.d(TAG, "read: " + e);
        }

        return -1;

    }


    /**
     * 停止录音
     */
    public void stopRecoder() {
        Log.d(TAG, "stopRecoder: ");
        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                isRecorder = false;
                audioRecord = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "stopRecoder: " + e);
        }


    }

}
