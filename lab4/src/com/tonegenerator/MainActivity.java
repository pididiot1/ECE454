package com.tonegenerator;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private static int nDuration = 3;
	private static double dFreqOfTone = 2000;
	private static int nSampleRate = 44100;
	private static int nNumSamples = nDuration * nSampleRate;
	private double[] sample = new double[nNumSamples];
	private byte[] generatedSnd = new byte[nNumSamples*2];
	
	public AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
			nSampleRate, 
			AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, 
			generatedSnd.length/3);
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		for (int i = 0; i < nNumSamples; ++i) {
			sample[i] = Math.sin(2 * Math.PI * i * (dFreqOfTone/nSampleRate));
		}

		int idx = 0;
		for (final double dVal : sample) {
			short val = (short) (dVal * 32767);
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}

	}

	public void play(View v) {
		AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				nSampleRate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, nNumSamples,
				AudioTrack.MODE_STREAM);
		
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
		audioTrack.play();
	}
	
	public void record(View v) {
		
		recorder.startRecording();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
