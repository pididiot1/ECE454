package com.tonegenerator;

import java.io.FileWriter;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private static int nDuration = 1;
	private static double dFreqOfTone = 20000;
	private static int nSampleRate = 44100;
	private static int nNumSamples = nDuration * nSampleRate;
	private double[] sample = new double[nNumSamples];
	private byte[] generatedSnd = new byte[nNumSamples*2];
	private Thread recordingThread;
	private short[] readData = new short[nNumSamples];

	
	public AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
			nSampleRate, 
			AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, 
			AudioRecord.getMinBufferSize(nSampleRate, AudioFormat.CHANNEL_IN_MONO, 
					AudioFormat.ENCODING_PCM_16BIT));
	


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
		record();
	}

	public void record() {
		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				recorder.startRecording();
				recorder.read(readData, 0, readData.length);
				recorder.stop();
				recorder.release();
				
				WriteFile("RecordedAudio.txt", readData);
			}
		},"AudioRecorder Thread");

		recordingThread.start();
	}
	
	private void WriteFile(String fileName, short[] data) {
		try {
			FileWriter writer = new FileWriter (Environment.getExternalStorageDirectory().getPath()+"/Download/"+fileName);
			for(int i = 0; i < data.length; i++) {
				writer.write(data[i] + "\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	private void writeAudioDataToFile(){
		byte data[] = new byte[nBufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int read = 0;

		if(null != os){
			while(isRecording){
				read = recorder.read(data, 0, nBufferSize);

				if(AudioRecord.ERROR_INVALID_OPERATION != read){
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void copyWaveFile(String inFilename,String outFilename){
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = nSampleRate;
		int channels = 2;
		long byteRate = RECORDER_BPP * nSampleRate * channels/8;

		byte[] data = new byte[nBufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

//			AppLog.logString("File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while(in.read(data) != -1){
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String getTempFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER);

		if(!file.exists()){
			file.mkdirs();
		}

		File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

		if(tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}
	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}
	
	private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        
        if(!file.exists()){
                file.mkdirs();
        }
        
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
}

	public class TimerTick extends TimerTask {

		public TimerTick(Activity mainActivity) {
		}
		@Override
		public void run() {
			recorder.stop();
			copyWaveFile(getTempFilename(),getFilename());
		}    	
	}
	*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
