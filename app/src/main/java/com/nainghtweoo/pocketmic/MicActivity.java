package com.nainghtweoo.pocketmic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static android.Manifest.permission.RECORD_AUDIO;

public class MicActivity extends AppCompatActivity {

    final int MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE = 226;
    AudioManager am = null;
    AudioRecord record = null;
    AudioTrack track = null;
    ImageView iv_mic;
    Button on_btn, off_btn;
    boolean isPlaying = false;
    Handler handler = new Handler();
    Runnable refreshList = new Runnable() {
        @Override
        public void run() {
            //what you want to do here
            //------------------------
            handler.postDelayed(refreshList, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_mic_latest);
        setVolumeControlStream(AudioManager.MODE_CURRENT);
        init();
        if (mayRequestRecordAudio()) {
            new RecordAndPlay().execute();
        }
    }

    /**
     * App Permissions for Audio
     **/
    private boolean mayRequestRecordAudio() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{RECORD_AUDIO}, MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE);
        return false;
    }

    private boolean isGrantedAudioRecord() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage("You need to grant Audio Permission for use!");
        builder.setPositiveButton("Request Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mayRequestRecordAudio();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new RecordAndPlay().execute();
            } else {
                showWarningDialog();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void createAudioRecord() {
        int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);
    }

    private void init() {
        TextView tvInfo = findViewById(R.id.info_msg);
        tvInfo.setText(getString(R.string.info_msg).concat("\n").concat(Utility.getAppVersion()));
        iv_mic = findViewById(R.id.iv_mic);
        iv_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.showInfoDialog(MicActivity.this);
            }
        });
        int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.MODE_CURRENT, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
        off_btn = findViewById(R.id.off_btn);
        on_btn = findViewById(R.id.on_btn);

        am = (AudioManager) MicActivity.this.getSystemService(Context.AUDIO_SERVICE);
        Log.d("Speaker", "Speaker is " + am.isSpeakerphoneOn());
        Log.d("Speaker", "isBluetoothScoOn " + am.isBluetoothScoOn());
        Log.d("Speaker", "isBluetoothA2dpOn " + am.isBluetoothA2dpOn());
        Log.d("Speaker", "isWiredHeadsetOn " + am.isWiredHeadsetOn());
        Log.d("Speaker", "Mode " + am.getMode());
        Log.d("Speaker", "RingMode " + am.getRingerMode());
        off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    isPlaying = false;
                    setUIControl();
                }
            }
        });
        on_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying && isConnectAnySpeaker(am)) {
                    isPlaying = true;
                    setUIControl();
                } else {
                    Utility.showInfoDialog(MicActivity.this);
                }
            }
        });
    }

    private boolean isConnectAnySpeaker(AudioManager am) {
        return am != null && (am.isSpeakerphoneOn() || am.isBluetoothA2dpOn() || Utility.isWifiConnected(this));
    }

    private void setUIControl() {
        int defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        int selectedColor = ContextCompat.getColor(this, android.R.color.white);
        on_btn.setTextColor(defaultColor);
        off_btn.setTextColor(defaultColor);
        on_btn.setSelected(false);
        off_btn.setSelected(false);
        if (isPlaying) {
            iv_mic.setImageResource(R.drawable.mic_open);
            on_btn.setTextColor(selectedColor);
            on_btn.setSelected(true);
        } else {
            iv_mic.setImageResource(R.drawable.mic_close);
            off_btn.setTextColor(selectedColor);
            off_btn.setSelected(true);
        }
        if (isGrantedAudioRecord()) {
            if (isPlaying) {
                record.startRecording();
                track.play();
                isPlaying = true;
            } else {
                record.stop();
                track.pause();
                isPlaying = false;
            }
        }
    }

    private void recordAndPlay() {

        int num = 0;
        short[] lin = new short[1024];
        setUIControl();
        while (true) {
            num = record.read(lin, 0, 1024);
            track.write(lin, 0, num);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.sample_mic_latest);
        //------
        handler.postDelayed(refreshList, 1000); // end of line your onCreate method
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    class RecordAndPlay extends AsyncTask<Void, Void, Void> {
        short[] lin = new short[1024];
        int num = 0;

        @Override
        protected void onPreExecute() {
            if (am != null) {
                am.setMode(AudioManager.MODE_CURRENT);
                am.setSpeakerphoneOn(false);
//                int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                Log.d("AudioManager", "Max Volume is " + maxVolume);
                float percent = 0.9f;
                int seventyVolume = (int) (maxVolume * percent);
                Log.d("AudioManager", "Set Volume is " + seventyVolume);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                createAudioRecord();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                num = record.read(lin, 0, 1024);
                track.write(lin, 0, num);
            }
        }
    }
}
