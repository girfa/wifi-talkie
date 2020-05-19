package com.girfa.apps.wifitalkie.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.R;
import com.girfa.apps.wifitalkie.db.ChatDB;
import com.girfa.apps.wifitalkie.helper.Broadcast;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.model.Chat;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Talkie;
import com.girfa.apps.wifitalkie.service.MainService;
import com.girfa.view.DataAdapter;

public class ChatFragment extends Fragment implements OnClickListener {
	public static final String TAG = ChatFragment.class.getSimpleName();
	private Connector con;
	private ChatDB cdb;
	private ChatDB.ChatResult result;
	private ListAdapter adapter;
	private ListView vList;
	private EditText vInput;
	private Button vSend;

	public static ChatFragment create() {
		return new ChatFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		con = new Connector();
		cdb = new ChatDB(getActivity());
		adapter = new ListAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chat_frg, container, false);
		vList = (ListView) view.findViewById(R.id.chat_list);
		vInput = (EditText) view.findViewById(R.id.chat_input);
		vSend = (Button) view.findViewById(R.id.chat_send);
		vList.setAdapter(adapter);
		vSend.setOnClickListener(this);
		if (Config.read(getActivity()).toggle()) {
			vInput.setEnabled(true);
			vSend.setEnabled(true);
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		con.register();
		cdb.openWrite();
		refresh();
	}

	@Override
	public void onPause() {
		if (result != null)
			result.close();
		cdb.close();
		con.unregister();
		super.onPause();
	}

	private void refresh() {
		if (result != null)
			result.close();
		result = cdb.gets(Config.read(getActivity()).showOffline());
		adapter.notifyDataSetChanged();
		vList.setSelection(vList.getCount() - 1);
	}

	@Override
	public void onClick(View v) {
		Talkie talkie = Config.read(getActivity()).me();
		String message = vInput.getText().toString();
		if (talkie.getMAC() == null || Utils.isEmpty(message))
			return;
		Chat chat = new Chat();
		chat.setFrom(talkie);
		chat.setMessage(message);
		chat.setWhen(System.currentTimeMillis());
		Intent intent = new Intent();
		intent.putExtra(Chat.Column._id.toString(), (int) cdb.add(chat));
		con.send(Command.CHAT, intent);
		vInput.setText(null);
		refresh();
	}

	private class Connector extends Broadcast {

		public Connector() {
			super(getActivity(), MainService.class);
		}

		@Override
		public void onReceive(Command cmd, Intent intent) {
			switch (cmd) {
			case _START:
				vInput.setEnabled(true);
				vSend.setEnabled(true);
				refresh();
				break;
			case _STOP:
				vInput.setEnabled(false);
				vSend.setEnabled(false);
				refresh();
				break;
			case ACK:
			case END:
			case PING:
			case CHAT:
				refresh();
				break;
			default:
			}
			Log.e(TAG, cmd + "");
		}
	}

	private class ListAdapter extends DataAdapter {

		public ListAdapter() {
			super(getActivity());
		}

		@Override
		public int getCount() {
			if (result != null)
				return result.size();
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			Chat chat = result.get(position);
			boolean isMe = chat.getFrom().equals(Config.me());
			Talkie from = chat.getFrom();
			view = getInflater().inflate(
					isMe ? R.layout.chat_item_left : R.layout.chat_item_right,
					null);
			ImageView vPhoto = (ImageView) view
					.findViewById(isMe ? R.id.chat_photo_left
							: R.id.chat_photo_right);
			TextView vName = (TextView) view
					.findViewById(isMe ? R.id.chat_name_left
							: R.id.chat_name_right);
			TextView vWhen = (TextView) view
					.findViewById(isMe ? R.id.chat_when_left
							: R.id.chat_when_right);
			TextView vMessage = (TextView) view
					.findViewById(isMe ? R.id.chat_message_left
							: R.id.chat_message_right);
			byte[] photo = from.getPhoto();
			if (photo != null)
				vPhoto.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0,
						photo.length));
			vName.setText(from.getName());
			String when = DateUtils.getRelativeTimeSpanString(chat.getWhen())
					.toString();
			String device = from.getDevice() + " " + from.getModel();
			vWhen.setText(String.format(getString(R.string.chat_when), when,
					device));
			vMessage.setText(chat.getMessage());
			view.setBackgroundColor(getResources().getColor(
					from.isOnline() ? R.color.online : R.color.offline));
			return view;
		}
	}
}