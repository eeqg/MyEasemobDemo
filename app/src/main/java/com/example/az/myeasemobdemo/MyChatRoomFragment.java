package com.example.az.myeasemobdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.az.myeasemobdemo.chat.EaseAtMessageHelper;
import com.example.az.myeasemobdemo.chat.EaseChatMessageList;
import com.example.az.myeasemobdemo.chat.EaseChatRoomListener;
import com.example.az.myeasemobdemo.chat.EaseConstant;
import com.example.az.myeasemobdemo.chat.EaseUI;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.EMLog;

import java.util.List;

/**
 * Created by wp on 2018/5/15.
 */

public class MyChatRoomFragment extends Fragment implements EMMessageListener {
	
	private String TAG = "MyChatRoomFragment";
	
	protected int pagesize = 20;
	private String chatRoomId;
	private EaseChatMessageList messageList;
	private ListView listView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private ChatRoomListener chatRoomListener;
	private EMConversation conversation;
	private InputMethodManager inputManager;
	
	private boolean isMessageListInited;
	private View rootView;
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_my_chat, container, false);
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		Bundle fragmentArgs = getArguments();
		// userId you are chat with or group id
		this.chatRoomId = fragmentArgs.getString("roomId");
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		observeContent();
	}
	
	private void observeContent() {
		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		// clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		this.messageList = (EaseChatMessageList) getView().findViewById(R.id.message_list);
		this.swipeRefreshLayout = messageList.getSwipeRefreshLayout();
		this.listView = messageList.getListView();
		
		this.chatRoomListener = new ChatRoomListener();
		EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomListener);
		onChatRoomViewCreation();
		
		final EditText etMessage = this.rootView.findViewById(R.id.etMessage);
		this.rootView.findViewById(R.id.tvSend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String message = etMessage.getText().toString();
				if (TextUtils.isEmpty(message)) {
					return;
				}
				sendTextMessage(message);
			}
		});
	}
	
	/**
	 * 加入聊天室
	 */
	protected void onChatRoomViewCreation() {
		final ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Joining......");
		EMClient.getInstance().chatroomManager().joinChatRoom(chatRoomId, new EMValueCallBack<EMChatRoom>() {
			
			@Override
			public void onSuccess(final EMChatRoom value) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (getActivity().isFinishing() || !chatRoomId.equals(value.getId()))
							return;
						pd.dismiss();
						EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
						if (room != null) {
							// titleBar.setTitle(room.getName());
							EMLog.d(TAG, "join room success : " + room.getName());
						} else {
							// titleBar.setTitle(toChatUsername);
						}
						onConversationInit();
						onMessageListInit();
					}
				});
			}
			
			@Override
			public void onError(final int error, String errorMsg) {
				// TODO Auto-generated method stub
				EMLog.d(TAG, "join room failure : " + error);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pd.dismiss();
					}
				});
				getActivity().finish();
			}
		});
	}
	
	protected void onConversationInit() {
		this.conversation = EMClient.getInstance().chatManager().getConversation(chatRoomId, EMConversation.EMConversationType.ChatRoom, true);
		conversation.markAllMessagesAsRead();
		// the number of messages loaded into conversation is getChatOptions().getNumberOfMessagesLoaded
		// you can change this number
		
		// if (!isRoaming) {
		final List<EMMessage> msgs = conversation.getAllMessages();
		int msgCount = msgs != null ? msgs.size() : 0;
		if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
			String msgId = null;
			if (msgs != null && msgs.size() > 0) {
				msgId = msgs.get(0).getMsgId();
			}
			conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
		}
		// } else {
		// 	fetchQueue.execute(new Runnable() {
		// 		@Override
		// 		public void run() {
		// 			try {
		// 				EMClient.getInstance().chatManager().fetchHistoryMessages(
		// 						toChatUsername, EaseCommonUtils.getConversationType(chatType), pagesize, "");
		// 				final List<EMMessage> msgs = conversation.getAllMessages();
		// 				int msgCount = msgs != null ? msgs.size() : 0;
		// 				if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
		// 					String msgId = null;
		// 					if (msgs != null && msgs.size() > 0) {
		// 						msgId = msgs.get(0).getMsgId();
		// 					}
		// 					conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
		// 				}
		// 				messageList.refreshSelectLast();
		// 			} catch (HyphenateException e) {
		// 				e.printStackTrace();
		// 			}
		// 		}
		// 	});
		// }
	}
	
	protected void onMessageListInit() {
		messageList.init(chatRoomId, 3, null);
		// setListItemClickListener();
		
		messageList.getListView().setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				// inputMenu.hideExtendMenuContainer();
				return false;
			}
		});
		
		isMessageListInited = true;
	}
	
	/**
	 * hide
	 */
	protected void hideKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getActivity().getCurrentFocus() != null)
				inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	/**
	 * listen chat room event
	 */
	class ChatRoomListener extends EaseChatRoomListener {
		
		@Override
		public void onChatRoomDestroyed(final String roomId, final String roomName) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					if (roomId.equals(chatRoomId)) {
						Toast.makeText(getActivity(), R.string.the_current_chat_room_destroyed, Toast.LENGTH_LONG).show();
						Activity activity = getActivity();
						if (activity != null && !activity.isFinishing()) {
							activity.finish();
						}
					}
				}
			});
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (isMessageListInited) {
			messageList.refresh();
		}
		EaseUI.getInstance().pushActivity(getActivity());
		// register the event listener when enter the foreground
		EMClient.getInstance().chatManager().addMessageListener(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// unregister this event listener when this activity enters the
		// background
		EMClient.getInstance().chatManager().removeMessageListener(this);
		
		// remove activity from foreground activity list
		EaseUI.getInstance().popActivity(getActivity());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (chatRoomListener != null) {
			EMClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomListener);
		}
		//离开聊天室
		EMClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId);
	}
	
	public void onBackPressed() {
		EMClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId);
	}
	
	//send message
	protected void sendTextMessage(String content) {
		// if (EaseAtMessageHelper.get().containsAtUsername(content)) {
		// 	Log.d("test_wp", "-1-sendTextMessage()--content=" + content);
		// 	sendAtMessage(content);
		// } else {
		// 	Log.d("test_wp", "-2-sendTextMessage()--content=" + content);
			EMMessage message = EMMessage.createTxtSendMessage(content, chatRoomId);
			sendMessage(message);
		// }
	}
	
	/**
	 * send @ message, only support group chat message
	 *
	 * @param content
	 */
	@SuppressWarnings("ConstantConditions")
	private void sendAtMessage(String content) {
		EMMessage message = EMMessage.createTxtSendMessage(content, chatRoomId);
		EMGroup group = EMClient.getInstance().groupManager().getGroup(chatRoomId);
		if (EMClient.getInstance().getCurrentUser().equals(group.getOwner()) && EaseAtMessageHelper.get().containsAtAll(content)) {
			message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL);
		} else {
			message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG,
					EaseAtMessageHelper.get().atListToJsonArray(EaseAtMessageHelper.get().getAtMessageUsernames(content)));
		}
		sendMessage(message);
		
	}
	
	private void sendMessage(EMMessage message) {
		if (message == null) {
			return;
		}
		// if (chatFragmentHelper != null) {
		// 	//set extension
		// 	chatFragmentHelper.onSetMessageAttributes(message);
		// }
		
		message.setChatType(EMMessage.ChatType.ChatRoom);
		//Add to conversation
		EMClient.getInstance().chatManager().saveMessage(message);
		//refresh ui
		if (isMessageListInited) {
			messageList.refreshSelectLast();
		}
	}
	
	// implement methods in EMMessageListener
	@Override
	public void onMessageReceived(List<EMMessage> messages) {
		for (EMMessage message : messages) {
			Log.d("test_wp", "onMessageReceived()--" + message.getBody().toString());
			String username = null;
			// group message
			if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
				username = message.getTo();
			} else {
				// single chat message
				username = message.getFrom();
			}
			
			// if the message is for current conversation
			if (username.equals(chatRoomId) || message.getTo().equals(chatRoomId) || message.conversationId().equals(chatRoomId)) {
				messageList.refreshSelectLast();
				EaseUI.getInstance().getNotifier().vibrateAndPlayTone(message);
				conversation.markMessageAsRead(message.getMsgId());
			} else {
				EaseUI.getInstance().getNotifier().onNewMsg(message);
			}
		}
	}
	
	@Override
	public void onCmdMessageReceived(List<EMMessage> messages) {
	
	}
	
	@Override
	public void onMessageRead(List<EMMessage> messages) {
		if (isMessageListInited) {
			messageList.refresh();
		}
	}
	
	@Override
	public void onMessageDelivered(List<EMMessage> messages) {
		if (isMessageListInited) {
			messageList.refresh();
		}
	}
	
	@Override
	public void onMessageRecalled(List<EMMessage> messages) {
		if (isMessageListInited) {
			messageList.refresh();
		}
	}
	
	@Override
	public void onMessageChanged(EMMessage emMessage, Object change) {
		Log.d("test_wp", "onMessageChanged()--" + emMessage.getBody().toString());
		if (isMessageListInited) {
			messageList.refresh();
		}
	}
}
