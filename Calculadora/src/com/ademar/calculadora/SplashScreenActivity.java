package com.ademar.calculadora;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {

	boolean activityVisible;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		activityVisible = true;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (activityVisible) {
					abrirCalculadora();
				}
			}
		}, 2000);
	} // Fim do método onCreate

	@Override
	protected void onResume() {
		super.onResume();
		if (!activityVisible) {
			abrirCalculadora();
		}
	} // Fim do método onResume

	@Override
	protected void onPause() {
		super.onPause();
		activityVisible = false;
	} // Fim do método onPause

	@Override
	public void onBackPressed() {
		this.moveTaskToBack(true);
	} // Fim do método onBackPressed

	private void abrirCalculadora() {
		startActivity(new Intent(this, FullscreenActivity.class));
		finish();
	} // Fim do método abrirCalculadora

} // Fim da classe