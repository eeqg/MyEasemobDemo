package com.example.az.myeasemobdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMChatRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * adapter
 */
class ChatRoomAdapter extends ArrayAdapter<EMChatRoom> {
	
	private final List<EMChatRoom> chatRoomList;
	private LayoutInflater inflater;
	private RoomFilter filter;
	
	ChatRoomAdapter(Context context, int res, List<EMChatRoom> chatRoomList) {
		super(context, res, chatRoomList);
		this.chatRoomList = chatRoomList;
		this.inflater = LayoutInflater.from(context);
	}
	
	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.em_row_group, parent, false);
		}
		((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.mipmap.em_group_icon);
		((TextView) convertView.findViewById(R.id.name)).setText(getItem(position).getName());
		
		return convertView;
	}
	
	@NonNull
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new RoomFilter();
		}
		return filter;
	}
	
	private class RoomFilter extends Filter {
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			
			if (constraint == null || constraint.length() == 0) {
				results.values = chatRoomList;
				results.count = chatRoomList.size();
			} else {
				List<EMChatRoom> roomss = new ArrayList<EMChatRoom>();
				for (EMChatRoom chatRoom : chatRoomList) {
					if (chatRoom.getName().contains(constraint)) {
						roomss.add(chatRoom);
					}
				}
				results.values = roomss;
				results.count = roomss.size();
			}
			return results;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			chatRoomList.clear();
			chatRoomList.addAll((List<EMChatRoom>) results.values);
			notifyDataSetChanged();
		}
	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}
}