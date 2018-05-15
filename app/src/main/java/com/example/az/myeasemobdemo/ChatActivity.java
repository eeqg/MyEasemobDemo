package com.example.az.myeasemobdemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ChatActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		//use EaseChatFratFragment
		MyChatRoomFragment chatFragment = new MyChatRoomFragment();
		//pass parameters to chat fragment
		chatFragment.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction().add(R.id.contentView, chatFragment).commit();
	}
}
