package com.monitor;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

public class BasicActivity extends Activity {

	// private PowerManager.WakeLock mWakeLock = null;
	private AudioManager mAudioManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		if (null == mAudioManager)
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mAudioManager = null;
	}
	
}
