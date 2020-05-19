package com.girfa.apps.wifitalkie.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.girfa.apps.wifitalkie.R;
import com.girfa.apps.wifitalkie.helper.Broadcast;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.service.MainService;

public class FilesFragment extends Fragment {
	public static final String TAG = FilesFragment.class.getSimpleName();
	private Connector con;
	
	public static FilesFragment create() {
		return new FilesFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.files_frg, container, false);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		con = new Connector();
	}
	
	@Override
	public void onResume() {
		con.register();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		con.unregister();
		super.onPause();
	}
	
	private class Connector extends Broadcast {

		public Connector() {
			super(getActivity(), MainService.class);
		}

		@Override
		public void onReceive(Command cmd, Intent intent) {
			switch (cmd) {
			case ACK:
			case END:
			case PING:
			case FILE:
			case FILE_OK:
			default:
			}
		}
	}
}