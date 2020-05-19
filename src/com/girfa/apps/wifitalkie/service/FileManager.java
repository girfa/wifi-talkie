package com.girfa.apps.wifitalkie.service;

import android.content.Context;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Header;
import java.io.InputStream;

class FileManager implements Networker {
	private Context ctx;
	private MainService.Connector mCon;
	
	FileManager(Context context, MainService.Connector con) {
		ctx = context;
		mCon = con;
	}

	void acceptFILE(Header header, byte[] data) {
	}

	void saveFILE(Header header, InputStream data) {
	}

	void sendFILE(Header header, byte[] data) {
	}

	public void start(boolean main) {
	}

	public void stop(boolean main) {
	}
}