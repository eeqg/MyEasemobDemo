package com.example.az.myeasemobdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMPageResult;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

public class MainActivity extends AppCompatActivity {
	
	private String TAG = "test_wp";
	private ListView listView;
	private ChatRoomAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		login();
		
		initView();
		// loadData();
	}
	
	private void initView() {
		this.listView = findViewById(R.id.listView);
		this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				startActivity(new Intent(MainActivity.this, ChatActivity.class).
						putExtra("roomId", adapter.getItem(position).getId()));
			}
		});
	}
	
	private void loadData() {
		new Thread(new Runnable() {
			
			public void run() {
				try {
					// pageSize: 此次获取的条目
					// cursor: 后台需要的cursor ID，根据此ID再次获取pageSize的条目，首次传null即可
					final EMPageResult<EMChatRoom> result = EMClient.getInstance().chatroomManager()
							.fetchPublicChatRoomsFromServer(1, 20);
					//get chat room list
					final List<EMChatRoom> chatRooms = result.getData();
					runOnUiThread(new Runnable() {
						public void run() {
							adapter = new ChatRoomAdapter(MainActivity.this, 1, chatRooms);
							listView.setAdapter(adapter);
						}
					});
				} catch (HyphenateException e) {
					e.printStackTrace();
					Log.d(TAG, "" + e);
				}
			}
		}).start();
	}
	
	private void login() {
		if (isLoggedIn()) {
			EMClient.getInstance().chatManager().loadAllConversations();
			EMClient.getInstance().groupManager().loadAllGroups();
			return;
		}
		EMClient.getInstance().login("asdfg", "asdfg", new EMCallBack() {
			
			@Override
			public void onSuccess() {
				Log.d(TAG, "login: onSuccess");
				
				// ** manually load all local groups and conversation
				// 加载所有会话到内存
				EMClient.getInstance().chatManager().loadAllConversations();
				// 加载所有群组到内存，如果使用了群组的话
				EMClient.getInstance().groupManager().loadAllGroups();
				
				loadData();
			}
			
			@Override
			public void onProgress(int progress, String status) {
				Log.d(TAG, "login: onProgress");
			}
			
			@Override
			public void onError(final int code, final String message) {
				Log.d(TAG, "login: onError: " + code);
				/**
				 * 关于错误码可以参考官方api详细说明
				 * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1_e_m_error.html
				 */
				switch (code) {
					// 网络异常 2
					case EMError.NETWORK_ERROR:
						Toast.makeText(MainActivity.this, "网络错误 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 无效的用户名 101
					case EMError.INVALID_USER_NAME:
						Toast.makeText(MainActivity.this, "无效的用户名 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 无效的密码 102
					case EMError.INVALID_PASSWORD:
						Toast.makeText(MainActivity.this, "无效的密码 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 用户认证失败，用户名或密码错误 202
					case EMError.USER_AUTHENTICATION_FAILED:
						Toast.makeText(MainActivity.this, "用户认证失败，用户名或密码错误 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 用户不存在 204
					case EMError.USER_NOT_FOUND:
						Toast.makeText(MainActivity.this, "用户不存在 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 无法访问到服务器 300
					case EMError.SERVER_NOT_REACHABLE:
						Toast.makeText(MainActivity.this, "无法访问到服务器 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 等待服务器响应超时 301
					case EMError.SERVER_TIMEOUT:
						Toast.makeText(MainActivity.this, "等待服务器响应超时 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 服务器繁忙 302
					case EMError.SERVER_BUSY:
						Toast.makeText(MainActivity.this, "服务器繁忙 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					// 未知 Server 异常 303 一般断网会出现这个错误
					case EMError.SERVER_UNKNOWN_ERROR:
						Toast.makeText(MainActivity.this, "未知的服务器异常 code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(MainActivity.this, "ml_sign_in_failed code: " + code + ", message:" + message, Toast.LENGTH_LONG).show();
						break;
				}
			}
		});
	}
	
	/**
	 * if ever logged in
	 *
	 * @return
	 */
	public boolean isLoggedIn() {
		return EMClient.getInstance().isLoggedInBefore();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		loginOut();
	}
	
	/**
	 * 退出登录
	 */
	private void loginOut() {
		// 调用sdk的退出登录方法，第一个参数表示是否解绑推送的token，没有使用推送或者被踢都要传false
		EMClient.getInstance().logout(false, new EMCallBack() {
			@Override
			public void onSuccess() {
				Log.i(TAG, "logout success");
				// 调用退出成功，结束app
				finish();
			}
			
			@Override
			public void onError(int i, String s) {
				Log.i(TAG, "logout error " + i + " - " + s);
			}
			
			@Override
			public void onProgress(int i, String s) {
			
			}
		});
	}
}
