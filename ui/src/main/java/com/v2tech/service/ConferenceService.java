package com.v2tech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;

import com.V2.jni.AudioRequest;
import com.V2.jni.ChatRequest;
import com.V2.jni.ChatRequestCallbackAdapter;
import com.V2.jni.ConfRequest;
import com.V2.jni.ConfRequestCallbackAdapter;
import com.V2.jni.GroupRequest;
import com.V2.jni.GroupRequestCallbackAdapter;
import com.V2.jni.ImRequest;
import com.V2.jni.VideoMixerRequest;
import com.V2.jni.VideoRequest;
import com.V2.jni.VideoRequestCallbackAdapter;
import com.V2.jni.callback.VideoMixerRequestCallback;
import com.V2.jni.ind.V2User;
import com.V2.jni.util.V2Log;
import com.v2tech.net.DeamonWorker;
import com.v2tech.net.lv.LiveWatchingReqPacket;
import com.v2tech.net.pkt.PacketProxy;
import com.v2tech.service.jni.JNIIndication;
import com.v2tech.service.jni.JNIResponse;
import com.v2tech.service.jni.MessageInd;
import com.v2tech.service.jni.PermissionUpdateIndication;
import com.v2tech.service.jni.RequestConfCreateResponse;
import com.v2tech.service.jni.RequestEnterConfResponse;
import com.v2tech.service.jni.RequestExitedConfResponse;
import com.v2tech.service.jni.RequestPermissionResponse;
import com.v2tech.service.jni.RequestUpdateCameraParametersResponse;
import com.v2tech.vo.AttendDeviceIndication;
import com.v2tech.vo.CameraConfiguration;
import com.v2tech.vo.Live;
import com.v2tech.vo.MixVideo;
import com.v2tech.vo.User;
import com.v2tech.vo.UserDeviceConfig;
import com.v2tech.vo.conference.Conference;
import com.v2tech.vo.conference.ConferencePermission;
import com.v2tech.vo.group.Group;
import com.v2tech.vo.group.Group.GroupType;
import com.v2tech.vo.msg.VMessage;


public class ConferenceService extends DeviceService {

	private static final int JNI_REQUEST_ENTER_CONF = 1;
	private static final int JNI_REQUEST_EXIT_CONF = 2;
	private static final int JNI_REQUEST_SPEAK = 5;
	private static final int JNI_REQUEST_RELEASE_SPEAK = 6;
	private static final int JNI_REQUEST_CREATE_CONFERENCE = 7;
	private static final int JNI_REQUEST_QUIT_CONFERENCE = 8;
	private static final int JNI_REQUEST_INVITE_ATTENDEES = 9;

	private static final int JNI_UPDATE_CAMERA_PAR = 75;

	private static final int KEY_KICKED_LISTNER = 100;
	private static final int KEY_ATTENDEE_DEVICE_LISTNER = 101;
	private static final int KEY_ATTENDEE_STATUS_LISTNER = 102;
	private static final int KEY_SYNC_LISTNER = 103;
	private static final int KEY_PERMISSION_CHANGED_LISTNER = 104;
	private static final int KEY_MIXED_VIDEO_LISTNER = 105;
	private static final int KEY_MESSAGE_LISTENER = 106;

	private VideoRequestCB videoCallback;
	private ConfRequestCB confCallback;
	private GroupRequestCB groupCallback;
	private MixerRequestCB mrCallback;
	private ChatRequestCB mcrCallback;

	private boolean mFlag = false;

	public ConferenceService() {
		this(false);
	}

	public ConferenceService(boolean flag) {
		super();
		videoCallback = new VideoRequestCB(this);
		VideoRequest.getInstance().addCallback(videoCallback);
		confCallback = new ConfRequestCB(this);
		ConfRequest.getInstance().addCallback(confCallback);
		groupCallback = new GroupRequestCB(this);
		GroupRequest.getInstance().addCallback(groupCallback);
		mrCallback = new MixerRequestCB(this);
		VideoMixerRequest.getInstance().addCallbacks(mrCallback);
		mcrCallback = new ChatRequestCB();
		ChatRequest.getInstance().addChatRequestCallback(mcrCallback);
		mFlag = flag;
	}

	/**
	 * User request to enter conference.<br>
	 * 
	 * @param l
	 *            {@link Live l} object which user wants to enter
	 * @param caller
	 *            if input is null, ignore response. Message.object is
	 *            {@link com.v2tech.service.jni.RequestEnterConfResponse}
	 * 
	 * @see com.v2tech.service.jni.RequestEnterConfResponse
	 */
	public void requestEnterConference(Live l, MessageListener caller) {
		DeamonWorker.getInstance().request(
				new PacketProxy(new LiveWatchingReqPacket(l.getLid(), GlobalHolder.getInstance().getCurrentUser().nId, l.getNid(),
						LiveWatchingReqPacket.WATCHING), null));
		V2Log.i("===  request join meeting  : "+  l.getLid());
		initTimeoutMessage(JNI_REQUEST_ENTER_CONF, DEFAULT_TIME_OUT_SECS,
				caller);
		ConfRequest.getInstance().ConfEnter(l.getLid());
	//	ConfRequest.getInstance().ConfQuickEnter(2, "test", GlobalHolder.getInstance().getCurrentUserId(), l.getLid(), 1);
		
	}
	
	
	

	/**
	 * User request to quit conference. This API just use to for quit conference
	 * this time.<br>
	 * User will receive this conference when log in next time.
	 * 
	 * @param l
	 *             {@link Live}  object which user wants to enter
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is
	 *            {@link com.v2tech.service.jni.RequestExitedConfResponse}
	 */
	public void requestExitConference(Live l, MessageListener caller) {
		if (l == null) {
			if (caller != null && caller.getHandler() != null) {
				JNIResponse jniRes = new RequestConfCreateResponse(0, 0,
						RequestConfCreateResponse.Result.INCORRECT_PAR);
				sendResult(caller, jniRes);
			}
			return;
		}
		DeamonWorker.getInstance().request(
				new PacketProxy(new LiveWatchingReqPacket(l.getLid(),GlobalHolder.getInstance().getCurrentUser().nId, l.getNid(),
						LiveWatchingReqPacket.CANCEL), null));
		
		initTimeoutMessage(JNI_REQUEST_EXIT_CONF, DEFAULT_TIME_OUT_SECS, caller);
		ConfRequest.getInstance().ConfExit(l.getLid());
		// send response to caller because exitConf no call back from JNI
		JNIResponse jniRes = new RequestExitedConfResponse(l.getLid(),
				System.currentTimeMillis() / 1000, JNIResponse.Result.SUCCESS);
		Message res = Message.obtain(this, JNI_REQUEST_EXIT_CONF, jniRes);
		// send delayed message for that make sure send response after JNI
		// request
		this.sendMessageDelayed(res, 300);
	}

	
	private static Random ran = new Random();
	/**
	 * Create conference.
	 * <ul>
	 * </ul>
	 * 
	 * @param l
	 *            {@link Live} object.
	 * @param caller
	 *            if input is null, ignore response Message. Response Message
	 *            object is
	 *            {@link com.v2tech.service.jni.RequestConfCreateResponse}
	 */
	public void createConference(Live l, MessageListener caller) {
		if (l == null) {
			if (caller != null && caller.getHandler() != null) {
				JNIResponse jniRes = new RequestConfCreateResponse(0, 0,
						RequestConfCreateResponse.Result.FAILED);
				sendResult(caller, jniRes);
			}
			return;
		}
//		long gid = 15010000000L;
//		int seed = ran.nextInt();
//		if (seed < 0) {
//			seed = ~seed;
//		}
//		gid |= seed;
		//ConfRequest.getInstance().ConfQuickEnter(2, "test", GlobalHolder.getInstance().getCurrentUserId(), gid, 1);
		
		initTimeoutMessage(JNI_REQUEST_CREATE_CONFERENCE,
				DEFAULT_TIME_OUT_SECS, caller);
		GroupRequest.getInstance().GroupCreate(
				Group.GroupType.CONFERENCE.intValue(),
				l.getConferenceConfigXml(), l.getInvitedAttendeesXml());
	}

	/**
	 * User request to quit this conference for ever.<br>
	 * User never receive this conference information any more.
	 * 
	 * @param l
	 * @param caller
	 */
	public void quitConference(Live l, MessageListener caller) {
		if (l == null) {
			if (caller != null) {
				JNIResponse jniRes = new RequestConfCreateResponse(0, 0,
						RequestConfCreateResponse.Result.INCORRECT_PAR);
				sendResult(caller, jniRes);
			}
			return;
		}
		
		DeamonWorker.getInstance().request(
				new LiveWatchingReqPacket(l.getLid(),GlobalHolder.getInstance()
						.getCurrentUser().nId, l.getNid(),
						LiveWatchingReqPacket.CLOSE)); 
		
		initTimeoutMessage(JNI_REQUEST_QUIT_CONFERENCE, DEFAULT_TIME_OUT_SECS,
				caller);
		// If conference owner is self, then delete group
		if (l.getPublisher().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
			GroupRequest.getInstance().GroupDestroy(
					Group.GroupType.CONFERENCE.intValue(), l.getLid());
			// If conference owner isn't self, just leave group
		} else {
			GroupRequest.getInstance().GroupLeave(
					Group.GroupType.CONFERENCE.intValue(), l.getLid());
		}
	}

	/**
	 * Chair man invite extra attendee to join current conference.<br>
	 * 
	 * @param conf
	 *            conference which user current joined
	 * @param list
	 *            additional attendee
	 * @param caller
	 *            caller
	 */
	public void inviteAttendee(Conference conf, List<User> list,
			MessageListener caller) {
		if (list == null || conf == null || list.isEmpty()) {
			if (caller != null) {
				JNIResponse jniRes = new JNIResponse(
						JNIResponse.Result.INCORRECT_PAR);
				sendResult(caller, jniRes);
			}
			return;
		}
		StringBuffer attendees = new StringBuffer();
		attendees.append("<userlist> ");
		for (User at : list) {
			attendees.append(" <user id='" + at.getmUserId() + "' />");
		}
		attendees.append("</userlist>");
		GroupRequest.getInstance().GroupInviteUsers(
				GroupType.CONFERENCE.intValue(), conf.getConferenceConfigXml(),
				attendees.toString(), "");

		// send response to caller because invite attendee no call back from JNI
		JNIResponse jniRes = new JNIResponse(JNIResponse.Result.SUCCESS);
		Message res = Message
				.obtain(this, JNI_REQUEST_INVITE_ATTENDEES, jniRes);
		// send delayed message for that make sure send response after JNI
		// request
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * User request speak permission on the conference.
	 * 
	 * @param type
	 *            speak type should be {@link ConferencePermission#SPEAKING}
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.v2tech.service.jni.RequestPermissionResponse}
	 * 
	 * @see ConferencePermission
	 */
	public void applyForControlPermission(ConferencePermission type,
			MessageListener caller) {
		initTimeoutMessage(JNI_REQUEST_SPEAK, DEFAULT_TIME_OUT_SECS, caller);

		ConfRequest.getInstance().ConfApplyPermission(type.intValue());

		JNIResponse jniRes = new RequestPermissionResponse(
				RequestPermissionResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_SPEAK, jniRes);
		this.sendMessageDelayed(res, 300);
	}

	/**
	 * Request release permission on the conference.
	 * 
	 * @param type
	 *            speak type should be {@link ConferencePermission#SPEAKING}
	 * @param caller
	 *            if input is null, ignore response Message.object is
	 *            {@link com.v2tech.service.jni.RequestPermissionResponse}
	 * 
	 * @see ConferencePermission
	 */
	public void applyForReleasePermission(ConferencePermission type,
			MessageListener caller) {

		initTimeoutMessage(JNI_REQUEST_RELEASE_SPEAK, DEFAULT_TIME_OUT_SECS,
				caller);

		ConfRequest.getInstance().ConfReleasePermission(type.intValue());

		JNIResponse jniRes = new RequestPermissionResponse(
				RequestPermissionResponse.Result.SUCCESS);

		// send delayed message for that make sure send response after JNI
		Message res = Message.obtain(this, JNI_REQUEST_RELEASE_SPEAK, jniRes);
		this.sendMessageDelayed(res, 300);
	}
	
	
	public void queryList(int type, MessageListener caller) {
//		if (type == 1) {
//			ConfRequest.getInstance().getMyFans();
//		} else if (type == 2) {
//			ConfRequest.getInstance().getMyConcerns();
//		}
	}

	/**
	 * Pause or resume audio.
	 * 
	 * @param flag
	 *            true for resume false for suspend
	 */
	public void updateAudio(boolean flag) {
		if (flag) {
			AudioRequest.getInstance().ResumePlayout();
		} else {
			AudioRequest.getInstance().PausePlayout();
		}

	}

	/**
	 * Register listener for out conference by kick.
	 * 
	 */
	public void registerKickedConfListener(Handler h, int what, Object obj) {
		registerListener(KEY_KICKED_LISTNER, h, what, obj);
	}

	public void removeRegisterOfKickedConfListener(Handler h, int what,
			Object obj) {
		unRegisterListener(KEY_KICKED_LISTNER, h, what, obj);

	}
	
	
	
	
	public void sendMessage(VMessage msg) {
		String xml = msg.toXml();
		V2Log.e(xml);
		byte[] bytes = xml.getBytes();
		ChatRequest.getInstance().ChatSendTextMessage(msg.getMsgCode(), msg.getGroupId(),
				msg.getToUser() == null ? 0 : msg.getToUser().getmUserId(), msg.getUUID(), bytes, bytes.length);
	}

	// =============================
	/**
	 * Register listener for out conference by kick.
	 * 
	 */
	public void registerAttendeeDeviceListener(Handler h, int what, Object obj) {
		registerListener(KEY_ATTENDEE_DEVICE_LISTNER, h, what, obj);
	}

	public void removeAttendeeDeviceListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_ATTENDEE_DEVICE_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for out conference by kick.
	 * 
	 */
	public void registerAttendeeListener(Handler h, int what, Object obj) {
		registerListener(KEY_ATTENDEE_STATUS_LISTNER, h, what, obj);
	}

	public void removeAttendeeListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_ATTENDEE_STATUS_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for chairman control or release desktop
	 * 
	 */
	public void registerSyncDesktopListener(Handler h, int what, Object obj) {
		registerListener(KEY_SYNC_LISTNER, h, what, obj);
	}

	public void removeSyncDesktopListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_SYNC_LISTNER, h, what, obj);
	}

	/**
	 * Register listener for permission changed
	 * 
	 */
	public void registerPermissionUpdateListener(Handler h, int what, Object obj) {
		registerListener(KEY_PERMISSION_CHANGED_LISTNER, h, what, obj);
	}

	public void unRegisterPermissionUpdateListener(Handler h, int what,
			Object obj) {
		unRegisterListener(KEY_PERMISSION_CHANGED_LISTNER, h, what, obj);
	}

	public void registerVideoMixerListener(Handler h, int what, Object obj) {
		registerListener(KEY_MIXED_VIDEO_LISTNER, h, what, obj);
	}

	public void unRegisterVideoMixerListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_MIXED_VIDEO_LISTNER, h, what, obj);
	}
	
	
	public void registerMessageListener(Handler h, int what, Object obj) {
		registerListener(KEY_MESSAGE_LISTENER, h, what, obj);
	}
	
	public void unRgisterMessageListener(Handler h, int what, Object obj) {
		unRegisterListener(KEY_MESSAGE_LISTENER, h, what, obj);
	}

	@Override
	public void clearCalledBack() {
		super.clearCalledBack();
		VideoRequest.getInstance().removeCallback(videoCallback);
		ConfRequest.getInstance().removeCallback(confCallback);
		GroupRequest.getInstance().removeCallback(groupCallback);
		VideoMixerRequest.getInstance().removeCallback(mrCallback);
		ChatRequest.getInstance().removeChatRequestCallback(mcrCallback);
	}

	@Override
	protected void notifyListenerWithPending(int key, int arg1, int arg2,
			Object obj) {
		if (mFlag) {
			super.notifyListenerWithPending(key, arg1, arg2, obj);
		} else {
			super.notifyListener(key, arg1, arg2, obj);
		}
	}

	class ConfRequestCB extends ConfRequestCallbackAdapter {

		private Handler mCallbackHandler;

		public ConfRequestCB(Handler mCallbackHandler) {
			this.mCallbackHandler = mCallbackHandler;
		}

		@Override
		public void OnEnterConfCallback(long nConfID, long nTime,
				String szConfData, int nJoinResult) {
			V2Log.i("OnEnterConfCallback====>config ID : "+nConfID+"  data==>" +szConfData+"   " +nJoinResult+"   =>");

			JNIResponse jniConfCreateRes = new RequestConfCreateResponse(
					nConfID, 0, RequestConfCreateResponse.Result.SUCCESS);
			Message.obtain(mCallbackHandler, JNI_REQUEST_CREATE_CONFERENCE,
					jniConfCreateRes).sendToTarget();
			
			JNIResponse jniRes = new RequestEnterConfResponse(
					nConfID,
					nTime,
					szConfData,
					JNIResponse.Result.SUCCESS);
			Message.obtain(mCallbackHandler, JNI_REQUEST_ENTER_CONF, jniRes)
					.sendToTarget();
//			
			ConfRequest.getInstance().NotifyConfAllMessage(nConfID);
//			//
//			DeamonWorker.getInstance().request(
//					new PacketProxy(new LiveWatchingReqPacket(GlobalHolder.getInstance().getCurrentUser().nId, nConfID,
//							LiveWatchingReqPacket.WATCHING), null));
//			

		}
		
		
		


		@Override
		public void OnConfMemberEnter(long nConfID, long nUserID, long nTime,
				String szUserInfos) {
			User u = GlobalHolder.getInstance().getUser(nUserID);
			// For quick logged in User.
			if (u == null) {
				u = new User(nUserID);
			}
			notifyListenerWithPending(KEY_ATTENDEE_STATUS_LISTNER, 0, 0, u);
		}

		@Override
		public void OnConfNotify(String confXml, String creatorXml) {
			V2Log.i("====>" +confXml+"   " +creatorXml+"   =>");
			// TODO Auto-generated method stub
			super.OnConfNotify(confXml, creatorXml);
		}

		@Override
		public void OnConfNotify(long nSrcUserID, String srcNickName,
				long nConfID, String subject, long nTime) {
			// TODO Auto-generated method stub
			super.OnConfNotify(nSrcUserID, srcNickName, nConfID, subject, nTime);
		}

		@Override
		public void OnConfNotifyEnd(long nConfID) {
			// TODO Auto-generated method stub
			super.OnConfNotifyEnd(nConfID);
		}

		@Override
		public void OnConfMemberExitCallback(long nConfID, long nTime,
				long nUserID) {

			User u = GlobalHolder.getInstance().getUser(nUserID);
			// For quick logged in User.
			if (u == null) {
				u = new User(nUserID);
			}
			notifyListenerWithPending(KEY_ATTENDEE_STATUS_LISTNER, 0, 0, u);

		}

		@Override
		public void OnKickConfCallback(int nReason) {
			notifyListenerWithPending(KEY_KICKED_LISTNER, nReason, 0, null);
		}

		@Override
		public void OnGrantPermissionCallback(long userid, int type, int status) {
			JNIIndication jniInd = new PermissionUpdateIndication(userid, type,
					status);
			notifyListenerWithPending(KEY_PERMISSION_CHANGED_LISTNER, 0, 0,
					jniInd);
		}

		@Override
		public void onUserListNotify(int type, List<V2User> list) {
			List<User> userList = new ArrayList<User>(list.size());
			for (V2User vu : list) {
				ImRequest.getInstance().ImGetUserBaseInfo(vu.mUserId);
				User u = new User(vu.mUserId, vu.name);
				userList.add(u);
			}
		}
		
		
		

	}

	class VideoRequestCB extends VideoRequestCallbackAdapter {

		private Handler mCallbackHandler;

		public VideoRequestCB(Handler mCallbackHandler) {
			this.mCallbackHandler = mCallbackHandler;
		}

	//	04-26 17:08:47.564: E/V2TECH(32261): [V2-TECH-ERROR]OnRemoteUserVideoDevice===>2011990   <xml defaultid='2011990:Camera'><video bps='128' camtype='0' comm='0' desc='Camera' fps='15' h='1080' id='2011990:Camera' inuse='1' videotype='1' w='1920'/></xml>

		@Override
		public void OnRemoteUserVideoDevice(long uid, String szXmlData) {
			V2Log.e("OnRemoteUserVideoDevice===>"+uid+"   "+szXmlData);
			if (szXmlData == null) {
				V2Log.e(" No avaiable user device configuration");
				return;
			}
			List<UserDeviceConfig> ll = UserDeviceConfig.parseFromXml(uid,
					szXmlData);
			
			AttendDeviceIndication  ind = new AttendDeviceIndication(JNIResponse.Result.SUCCESS);
			ind.uid = uid;
			ind.ll = ll;
			
			User u = GlobalHolder.getInstance().getUser(uid);
			// For quick logged in User.
			if (u == null) {
				u = new User(uid);
			}
			u.ll = ll;
			notifyListenerWithPending(KEY_ATTENDEE_DEVICE_LISTNER, 0, 0, ind);

		}

		@Override
		public void OnSetCapParamDone(String szDevID, int nSizeIndex,
				int nFrameRate, int nBitRate) {
			JNIResponse jniRes = new RequestUpdateCameraParametersResponse(
					new CameraConfiguration(szDevID, 1, nFrameRate, nBitRate),
					RequestUpdateCameraParametersResponse.Result.SUCCESS);
			Message.obtain(mCallbackHandler, JNI_UPDATE_CAMERA_PAR, jniRes)
					.sendToTarget();

		}

	}

	class GroupRequestCB extends GroupRequestCallbackAdapter {

		public GroupRequestCB(Handler mCallbackHandler) {
		}

//		@Override
//		public void OnModifyGroupInfoCallback(V2Group group) {
//			if (group == null) {
//				return;
//			}
//			if (group.type == Group.GroupType.CONFERENCE.intValue()) {
//				ConferenceGroup cache = (ConferenceGroup) GlobalHolder
//						.getInstance().findGroupById(group.id);
//
//				// if doesn't find matched group, mean this is new group
//				if (cache == null) {
//
//				} else {
//					cache.setSyn(group.isSync);
//						notifyListenerWithPending(KEY_SYNC_LISTNER,
//								(cache.isSyn() ? 1 : 0), 0, null);
//
//				}
//
//			}
//		}

	}
	
	
	class ChatRequestCB extends ChatRequestCallbackAdapter {

		@Override
		public void OnRecvChatTextCallback(int eGroupType, long nGroupID,
				long nFromUserID, long nToUserID, long nTime, String szSeqID,
				String szXmlText) {
			V2Log.i(nGroupID+"   "+nFromUserID+"   "+nToUserID+szXmlText);
			
			Pattern p = Pattern.compile("(Text=\")(.+)(\"/)");
			Matcher m = p.matcher(szXmlText);
			if (m.find()) {
				MessageInd ind = new MessageInd(MessageInd.Result.SUCCESS);
				ind.uid = nFromUserID;
				ind.lid = nGroupID;
				String group = m.group();
				ind.content = group.substring(6, group.length() - 2);
				notifyListener(KEY_MESSAGE_LISTENER, 0, 0, ind);
			} else {
				V2Log.e("====no match");
			}
 			 

		}
		
	}

	class MixerRequestCB implements VideoMixerRequestCallback {

		public MixerRequestCB(Handler mCallbackHandler) {
		}

		@Override
		public void OnCreateVideoMixerCallback(String sMediaId, int layout,
				int width, int height) {
			if (sMediaId == null || sMediaId.isEmpty()) {
				V2Log.e(" OnCreateVideoMixerCallback -- > unlmatform parameter sMediaId is null ");
				return;
			}
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 1, 0,
					new MixVideo(sMediaId, MixVideo.LayoutType.fromInt(layout),
							width, height));
		}

		@Override
		public void OnDestroyVideoMixerCallback(String sMediaId) {
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 2, 0,
					new MixVideo(sMediaId, MixVideo.LayoutType.UNKOWN));
		}

		@Override
		public void OnAddVideoMixerCallback(String sMediaId, long nDstUserId,
				String sDstDevId, int pos) {
			UserDeviceConfig udc = new UserDeviceConfig(0, 0, nDstUserId,
					sDstDevId, null);
			MixVideo mix = new MixVideo(sMediaId);
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 3, 0,
					mix.createMixVideoDevice(pos, sMediaId, udc));
		}

		@Override
		public void OnDelVideoMixerCallback(String sMediaId, long nDstUserId,
				String sDstDevId) {
			UserDeviceConfig udc = new UserDeviceConfig(0, 0, nDstUserId,
					sDstDevId, null);
			MixVideo mix = new MixVideo(sMediaId);
			notifyListenerWithPending(KEY_MIXED_VIDEO_LISTNER, 4, 0,
					mix.createMixVideoDevice(-1, sMediaId, udc));

		}

	}

}
