package com.girfa.apps.wifitalkie;

import android.content.Intent;

import com.girfa.apps.SplashScreen;
import com.girfa.apps.wifitalkie.service.MainService;

public class WiFiTalkie extends SplashScreen {
	public static final String TAG = WiFiTalkie.class.getSimpleName();
	
	public void init() {
		setAppName(getResources().getString(R.string.app_name));
		showSplashEvery(7, 3000);
	}

	public void start() {
		startActivity(new Intent(this, MainActivity.class));
		startService(new Intent(this, MainService.class));
	}
}