package com.girfa.apps.wifitalkie.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.girfa.apps.wifitalkie.R;
import com.girfa.apps.wifitalkie.db.TalkieDB;
import com.girfa.apps.wifitalkie.helper.Broadcast;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Talkie;
import com.girfa.apps.wifitalkie.service.MainService;
import com.girfa.view.DataAdapter;

public class TalkiesFragment extends Fragment implements
		OnCheckedChangeListener, OnTouchListener {
	public static final String TAG = TalkiesFragment.class.getSimpleName();
	private Connector con;
	private TalkieDB tdb;
	private TalkieDB.TalkieResult result;
	private ListAdapter adapter;
	private SparseArray<View> views = new SparseArray<View>();
	private CheckBox vLock;
	private Button vPtt;

	public static TalkiesFragment create() {
		return new TalkiesFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		con = new Connector();
		tdb = new TalkieDB(getActivity());
		adapter = new ListAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.talkies_frg, container, false);
		vLock = (CheckBox) view.findViewById(R.id.talkies_lock);
		vPtt = (Button) view.findViewById(R.id.talkies_ptt);
		ListView list = (ListView) view.findViewById(R.id.talkies_list);
		vLock.setOnCheckedChangeListener(this);
		vPtt.setOnTouchListener(this);
		list.setAdapter(adapter);
		pttState();
		return view;
	}

	@Override
	public void onResume() {
		con.register();
		tdb.openRead();
		refresh();
		super.onResume();
	}

	@Override
	public void onPause() {
		con.unregister();
		result.close();
		tdb.close();
		super.onPause();
	}

	private void refresh() {
		views.clear();
		if (result != null)
			result.close();
		result = tdb.gets(Config.read(getActivity()).showOffline());
		adapter.notifyDataSetChanged();
	}

	private void pttState() {
		boolean on = Config.read(getActivity()).toggle();
		vLock.setEnabled(on);
		vPtt.setEnabled(on);
		boolean ptt = Config.read(getActivity()).ptt();
		vLock.setChecked(on && !ptt);
		vPtt.setPressed(on && !ptt);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!Config.read(getActivity()).ptt())
			return true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			con.send(Command.AUDIO_ON, null);
			break;
		case MotionEvent.ACTION_UP:
			con.send(Command.AUDIO_OFF, null);
			break;
		}
		return false;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		vPtt.setPressed(isChecked);
		Config.write(getActivity()).ptt(!isChecked);
		con.send(isChecked ? Command.AUDIO_ON : Command.AUDIO_OFF, null);
	}

	private class ListAdapter extends DataAdapter {

		public ListAdapter() {
			super(getActivity());
		}

		@Override
		public int getCount() {
			if (result == null)
				return 0;
			return result.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Talkie talkie = result.get(position);
			convertView = getInflater().inflate(R.layout.talkies_item, null);
			String name = talkie.getName();
			String status = talkie.getStatus();
			String device = talkie.getDevice() + " " + talkie.getModel();
			byte[] photo = talkie.getPhoto();
			if (name == null)
				name = getString(R.string.def_name);
			if (talkie.isOnline()) {
				if (status == null)
					status = getString(R.string.def_status);
			} else {
				status = getString(R.string.offline);
			}
			if (device == null)
				device = getString(R.string.def_device);
			if (photo != null) {
				ImageView vPhoto = (ImageView) convertView
						.findViewById(R.id.ti_photo);
				vPhoto.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0,
						photo.length));
			}
			((TextView) convertView.findViewById(R.id.ti_name)).setText(name);
			((TextView) convertView.findViewById(R.id.ti_status))
					.setText(status);
			((TextView) convertView.findViewById(R.id.ti_device))
					.setText(device);
			views.put(position, convertView);
			convertView.setBackgroundColor(getResources().getColor(
					talkie.isOnline() ? R.color.online : R.color.offline));
			return convertView;
		}

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
				refresh();
				break;
			case _STOP:
				refresh();
			case _START:
				pttState();
				break;
			case AUDIO:
			case AUDIO_OFF:
			case AUDIO_ON:
			default:
			}
			Log.e(TAG, cmd + "");
		}
	}
}