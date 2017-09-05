package com.ademar.calculadora;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		Handler handle = new Handler();
		handle.postDelayed(new Runnable() {
		     @Override
		     public void run() {
		          mostrarLogin();
		     }
		}, 2000);
	}

	private void mostrarLogin() {
		try {
			Intent intent = new Intent(this, FullscreenActivity.class);
			startActivity(intent);
			finish();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}

}