package tojoy.it.tvcontrol;

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
    private static RecoderUtils instance;
    private AudioRecord audioRecord;
    //采样率的修改会影响语音识别的效果
    private int sampleRateInHz = 16000;
    private int bufferSizeInBytes = 1024 * 2;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private volatile boolean isRecoder = false;


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
        if (!isRecoder) {
            try {
                isRecoder = true;
                audioRecord = initAudioRecoder();
                audioRecord.startRecording();
            } catch (Exception e) {
                Log.d(TAG, "startRecoder: " + e);
                audioRecord = null;
                isRecoder = false;
            }
        }
    }

    public int read(@NonNull byte[] bytes, int offsetInBytes, int sizeInBytes) {
        if (audioRecord != null && isRecoder) {
            return audioRecord.read(bytes, offsetInBytes, sizeInBytes);
        }
        return -1;

    }


    public boolean isRecoder() {
        return isRecoder;
    }

    /**
     * 停止录音
     */
    public void stopRecoder() {
        Log.d(TAG, "stopRecoder: ");

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            isRecoder = false;
            audioRecord = null;
        }


    }

}
