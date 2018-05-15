package com.example.az.myeasemobdemo;

import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

/**
 * Created by wp on 2018/5/15.
 */

public class APP extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// initEasemob();
		// 调用初始化方法初始化sdk
		EMOptions options = new EMOptions();
		EMClient.getInstance().init(this, options);
		// 设置开启debug模式
		EMClient.getInstance().setDebugMode(true);
	}
	
	private void initEasemob() {
		// // 获取当前进程 id 并取得进程名
		// int pid = android.os.Process.myPid();
		// String processAppName = getAppName(pid);
		// /**
		//  * 如果app启用了远程的service，此application:onCreate会被调用2次
		//  * 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
		//  * 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
		//  */
		// if (processAppName == null || !processAppName.equalsIgnoreCase(mContext.getPackageName())) {
		// 	// 则此application的onCreate 是被service 调用的，直接返回
		// 	return;
		// }
		// if (isInit) {
		// 	return;
		// }
		/**
		 * SDK初始化的一些配置
		 * 关于 EMOptions 可以参考官方的 API 文档
		 * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1chat_1_1_e_m_options.html
		 */
		EMOptions options = new EMOptions();
		// 设置Appkey，如果配置文件已经配置，这里可以不用设置
		// options.setAppKey("guaju");
		// 设置自动登录
		options.setAutoLogin(true);
		// 设置是否需要发送已读回执
		options.setRequireAck(true);
		// 设置是否需要发送回执，TODO 这个暂时有bug，上层收不到发送回执
		options.setRequireDeliveryAck(true);
		// 设置是否需要服务器收到消息确认
		// options.setRequireServerAck(true);
		// 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
		options.setAcceptInvitationAlways(false);
		// 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
		options.setAutoAcceptGroupInvitation(false);
		// 设置（主动或被动）退出群组时，是否删除群聊聊天记录
		options.setDeleteMessagesAsExitGroup(false);
		// 设置是否允许聊天室的Owner 离开并删除聊天室的会话
		options.allowChatroomOwnerLeave(true);
		// 设置google GCM推送id，国内可以不用设置
		// options.setGCMNumber(MLConstants.ML_GCM_NUMBER);
		// 设置集成小米推送的appid和appkey
		// options.setMipushConfig(MLConstants.ML_MI_APP_ID, MLConstants.ML_MI_APP_KEY);
		
		// 调用初始化方法初始化sdk
		EMClient.getInstance().init(this, options);
		
		// 设置开启debug模式
		EMClient.getInstance().setDebugMode(true);
		
		// 设置初始化已经完成
		// isInit = true;
	}
}
