package com.v2tech.view;

import v2av.VideoPlayer;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.V2.jni.util.V2Log;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapView;
import com.v2tech.map.MapAPI;
import com.v2tech.map.baidu.BaiduMapImpl;
import com.v2tech.v2liveshow.R;
import com.v2tech.video.VideoController;
import com.v2tech.video.VideoShareSufaceViewCallback;
import com.v2tech.vo.Live;
import com.v2tech.vo.User;
import com.v2tech.vo.Watcher;
import com.v2tech.widget.BountyMarkerWidget;
import com.v2tech.widget.LiveInformationLayout;
import com.v2tech.widget.LiveInformationLayout.LiveInformationLayoutListener;
import com.v2tech.widget.LiverInteractionLayout;
import com.v2tech.widget.LiverInteractionLayout.InterfactionBtnClickListener;
import com.v2tech.widget.MessageMarqueeLinearLayout;
import com.v2tech.widget.MessageMarqueeLinearLayout.MessageMarqueeLayoutListener;
import com.v2tech.widget.P2PAudioWatcherLayout;
import com.v2tech.widget.P2PAudioWatcherLayout.P2PAudioWatcherLayoutListener;
import com.v2tech.widget.P2PVideoMainLayout;
import com.v2tech.widget.P2PVideoMainLayout.P2PVideoMainLayoutListener;
import com.v2tech.widget.RequestConnectLayout;
import com.v2tech.widget.RequestConnectLayout.RequestConnectLayoutListener;
import com.v2tech.widget.TouchSurfaceView;
import com.v2tech.widget.VideoShareBtnLayout;
import com.v2tech.widget.VideoShowFragment;
import com.v2tech.widget.VideoWatcherListLayout;
import com.v2tech.widget.VideoWatcherListLayout.VideoWatcherListLayoutListener;

public class MapVideoLayout extends FrameLayout implements VideoControllerAPI{
	
	private static int VIDEO_SURFACE_HEIGHT = 684;
	
	private static final int ANIMATION_TYPE_IN = 1;
	private static final int ANIMATION_TYPE_OUT = 2;
	//from down to up  for in and from up to down for out
	private static final int ANIMATION_TYPE_CATEGORY = 1;
	
	private static final int ANIMATION_DURATION = 1000;
	

	private static final boolean DEBUG = true;
	private static final String TAG = "MapVideoLayout";
	
	private int mTouchSlop;
	private int mTouchTapTimeout;
	

	private VideoPlayer videoController;
	
	private MapView mMapView;
	private TouchSurfaceView tsv;
	private TouchSurfaceView shareSurfaceView;
	private VideoShareBtnLayout videoShareBtnLayout;
	private MessageMarqueeLinearLayout mMsgLayout;
	private LiverInteractionLayout lierInteractionLayout;
	private RequestConnectLayout   requestConnectLayout;
	private P2PVideoMainLayout p2pVideoLayout;
	private P2PAudioWatcherLayout p2pAudioWatcherLayout;
	private LiveInformationLayout  liveInformationLayout;
	private VideoWatcherListLayout liveWatcherLayout;
	
	private BountyMarkerWidget bountyMarker;
	
	private LayoutPositionChangedListener mPosInterface;
	
	
	private ScreenType st = ScreenType.VIDEO_MAP;
	private PostState ps = PostState.IDLE;
	private Flying fly = new Flying();


	public MapVideoLayout(Context context) {
		super(context);
		init();
	}

	public MapVideoLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapVideoLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		videoController = new VideoPlayer(6);
		tsv =  new TouchSurfaceView(getContext()); 
		tsv.setZOrderOnTop(true);
		tsv.setZOrderMediaOverlay(true);
		tsv.getHolder().setFormat(PixelFormat.TRANSPARENT);
		tsv.getHolder().addCallback(videoController);
		
		shareSurfaceView = new TouchSurfaceView(getContext()); 
		shareSurfaceView.getHolder().addCallback(new VideoShareSufaceViewCallback());

		BaiduMapOptions mapOptions = new BaiduMapOptions();
		mapOptions.compassEnabled(true);
		mapOptions.scaleControlEnabled(true);
		mapOptions.zoomControlsEnabled(false);
		mapOptions.rotateGesturesEnabled(true);
		mMapView = new MapView(getContext(), mapOptions);
		
		mMsgLayout = (MessageMarqueeLinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.message_marquee_layout, (ViewGroup)null);
		videoShareBtnLayout = (VideoShareBtnLayout)LayoutInflater.from(getContext()).inflate(R.layout.video_share_btn_layout, (ViewGroup)null);
		
		
		lierInteractionLayout = (LiverInteractionLayout)LayoutInflater.from(getContext()).inflate(R.layout.liver_interaction_layout, (ViewGroup)null);
		lierInteractionLayout.showInnerBox(false);
		lierInteractionLayout.setVisibility(View.GONE);
		
		
		requestConnectLayout = (RequestConnectLayout)LayoutInflater.from(getContext()).inflate(R.layout.requesting_connect_layout, (ViewGroup)null);
		requestConnectLayout.setVisibility(View.GONE);
		
		p2pVideoLayout= (P2PVideoMainLayout)LayoutInflater.from(getContext()).inflate(R.layout.p2p_video_main_layout, (ViewGroup)null);
		p2pVideoLayout.setVisibility(View.GONE);
		
		p2pAudioWatcherLayout= (P2PAudioWatcherLayout)LayoutInflater.from(getContext()).inflate(R.layout.p2p_audio_watcher_layout, (ViewGroup)null);
		p2pAudioWatcherLayout.setVisibility(View.GONE);
		
		
		liveInformationLayout = (LiveInformationLayout)LayoutInflater.from(getContext()).inflate(R.layout.video_right_border_layout, (ViewGroup)null);
		liveWatcherLayout	 = (VideoWatcherListLayout)LayoutInflater.from(getContext()).inflate(R.layout.video_layout_bottom_layout, (ViewGroup)null);
		
		bountyMarker = (BountyMarkerWidget)LayoutInflater.from(getContext()).inflate(R.layout.bounty_marker_layout, (ViewGroup)null);
		
		this.addView(shareSurfaceView, -1, new LayoutParams(LayoutParams.MATCH_PARENT, VIDEO_SURFACE_HEIGHT));
		this.addView(videoShareBtnLayout, -1, generateDefaultLayoutParams());
		
		this.addView(tsv, -1, new LayoutParams(LayoutParams.MATCH_PARENT, VIDEO_SURFACE_HEIGHT));
		this.addView(mMapView, -1, generateDefaultLayoutParams());
		this.addView(lierInteractionLayout, -1, generateDefaultLayoutParams());
		this.addView(p2pVideoLayout, -1, generateDefaultLayoutParams());
		this.addView(p2pAudioWatcherLayout, -1, generateDefaultLayoutParams());
		
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.topMargin = 10;
		this.addView(mMsgLayout, -1, lp);
		this.addView(liveInformationLayout, -1,  new LayoutParams(LayoutParams.WRAP_CONTENT, VIDEO_SURFACE_HEIGHT ));
		this.addView(liveWatcherLayout, -1,  new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.addView(requestConnectLayout, -1, generateDefaultLayoutParams());
		this.addView(bountyMarker, -1,  new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mTouchTapTimeout = ViewConfiguration.getTapTimeout();
	}
	
	
	
	
	


	public MapAPI getMap() {
		return new BaiduMapImpl(mMapView.getMap(), mMapView);
	}

	public MapView getMapView() {
		return this.mMapView;
	}

	
	public void addNewMessage(CharSequence msg) {
		mMsgLayout.addMessageString(msg);
	}
	
	
	
	public VideoOpt addNewVideoWindow(final Live l) {
		return null;
	}
	
	
	public int getVideoWindowNums() {
		//return mViewPagerAdapter.getCount();
		return 1;
	}
	


	public void setPosInterface(LayoutPositionChangedListener posInterface) {
		this.mPosInterface = posInterface;
	}


	public VideoController getCurrentVideoController() {
		return new VideoController() {

			@Override
			public View getVideoView() {
				//return (SurfaceView)((SurfaceViewAdapter)mViewPagerAdapter).getItem(mVideoShowPager.getCurrentItem());
				return tsv;
			}
			
		};
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public interface OnVideoFragmentChangedListener {
		public void onChanged(VideoShowFragment videoFrag);
	}

	public interface LayoutPositionChangedListener {
		public void onPreparedFlyingIn();
		
		public void onFlyingIn();

		public void onPreparedFlyingOut();
		
		public void onFlyingOut();
		
		public void onDrag();
		
		public void onVideoScreenClick();
	}

	


	
	public void translateTsvAndMap(int offset) {
		tsv.offsetTopAndBottom(offset);
		mMapView.offsetTopAndBottom(offset);
	}
	
	public void updateRendNum(int num) {
		liveInformationLayout.updateRecommands(num+"");
	}
	
	public void updateWatcherNum(int num) {
		
	}
	
	public void updateBalanceSum(float num) {
		liveInformationLayout.updateTips(num + "");
	}
	
    public void showRedBtm(boolean flag) {
		//TODO add implments
	}
	
	public void showIncharBtm(boolean flag) {
		//TODO add implments
	}
	
	public void showVideoBtnLy(boolean flag) {
		liveInformationLayout.setVisibility(flag? View.VISIBLE:View.GONE);
	}
	public void showVideoWatcherListLy(boolean flag) {
		liveWatcherLayout.setVisibility(flag? View.VISIBLE:View.GONE);
	}
	
	
	
	public void showLiverInteractionLy(boolean flag) {
		showOrHidenViewAnimation(lierInteractionLayout, flag);
	}
	
	
	public void showRequestingConnectionLy(boolean flag) {
		showOrHidenViewAnimation(requestConnectLayout, flag);
	}
	
	
	public void showP2PAudioWatcherLy(boolean flag) {
		showOrHidenViewAnimation(p2pAudioWatcherLayout, flag);
	}
	
	public void showP2PVideoLayout(boolean flag) {
		showOrHidenViewAnimation(p2pVideoLayout, flag);
		p2pVideoLayout.bringToFront();
	}
	
	
	private void showOrHidenViewAnimation(View view, boolean flag) {
		if (flag && view.getVisibility() == View.GONE) {
			this.mMapView.onPause();
			view.setVisibility(View.VISIBLE);
			view.startAnimation(getBoxAnimation(
					ANIMATION_TYPE_CATEGORY, ANIMATION_TYPE_IN,
					ANIMATION_DURATION, true));
		} else if (!flag  && view.getVisibility() == View.VISIBLE)  {
			view.startAnimation(getBoxAnimation(
					ANIMATION_TYPE_CATEGORY, ANIMATION_TYPE_OUT,
					ANIMATION_DURATION, true));
			view.setVisibility(View.GONE);
			this.mMapView.onResume();
		}
	}
	
	private Animation getBoxAnimation(int cate, int type, int duration, boolean fillAfter) {
		Animation tabBlockHolderAnimation = null;
		
		if (type == ANIMATION_TYPE_OUT) {
			tabBlockHolderAnimation = AnimationUtils.loadAnimation(getContext(),
					R.animator.liver_interaction_from_up_to_down_out);
		} else if (type == ANIMATION_TYPE_IN) {
			tabBlockHolderAnimation =  AnimationUtils.loadAnimation(getContext(),
					R.animator.liver_interaction_from_down_to_up_in);
		}
		tabBlockHolderAnimation.setDuration(duration);
		tabBlockHolderAnimation.setFillAfter(fillAfter);
		tabBlockHolderAnimation.setZAdjustment(Animation.ZORDER_TOP);

		return tabBlockHolderAnimation;
		
	}
	
	
	public void updateFollowBtnImageResource(int res) {
		lierInteractionLayout.updateFollowBtnImageResource(res);
	}

	public void updateFollowBtnTextResource(int res) {
		lierInteractionLayout.updateFollowBtnTextResource(res);
	}

	
	
	
	public SurfaceView getP2PWatcherSurfaceView() {
		return p2pVideoLayout.getSurfaceView();
	}
	
	
	public void setRequestConnectLayoutListener(RequestConnectLayoutListener listener)  {
		this.requestConnectLayout.setListener(listener);
	}
	
	
	public void setP2PAudioWatcherLayoutListener(P2PAudioWatcherLayoutListener listener)  {
		this.p2pAudioWatcherLayout.setOutListener(listener);
	}
	
	public void setInterfactionBtnClickListener(InterfactionBtnClickListener listener)  {
		this.lierInteractionLayout.setOutListener(listener);
	}
	
	public void setP2PVideoMainLayoutListener(P2PVideoMainLayoutListener listener) {
		this.p2pVideoLayout.setListener(listener);
	}
	
	public void setMessageMarqueeLayoutListener(MessageMarqueeLayoutListener listener) {
		this.mMsgLayout.setListener(listener);
	}
	
	public void showMarqueeMessageLayout(boolean flag) {
		mMsgLayout.setVisibility(flag? View.VISIBLE : View.GONE);
	}
	public void showMarqueeMessage(boolean flag) {
		mMsgLayout.updateMessageShow(flag);
	}
	
	
	public void setLiveInformationLayoutListener(LiveInformationLayoutListener listener) {
		this.liveInformationLayout.setListener(listener);
	}
	
	public void setVideoWatcherListLayoutListener(VideoWatcherListLayoutListener listener) {
		this.liveWatcherLayout.setListener(listener);
	}
	
	
	public void addWatcher(Watcher watcher) {
		liveWatcherLayout.addWatcher(watcher);
	}
	
	public void removeWatcher(Watcher watcher) {
		liveWatcherLayout.removeWatcher(watcher);
	}
	
	
	public VideoPlayer getVideoPlayer() {
		return videoController;
	}
	
	
	
	
	private int mInitY;
	private int mInitX;
	private int mLastY;
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean flag = false;
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		int disX = Math.abs(x - mInitX);
		int disY = Math.abs(y - mInitY);
		
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			doTouchDown(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			int yDiff = Math.abs((int)ev.getY() - mInitY);
			flag = checkTouchRectEvent(ev) && yDiff > disX;
			break;
		case MotionEvent.ACTION_UP:
			flag = false;
			break;
		}
		return flag;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int y = (int)ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!checkTouchRectEvent(ev)) {
				return false;
			}
			doTouchDown(ev);
			
			break;
		case MotionEvent.ACTION_MOVE:
			doTouchMove(ev);
			break;
		case MotionEvent.ACTION_UP:
			doTouchUp(ev);
			break;
		}

		mLastY = y;
		
		return true;
	}
	
	
	private boolean checkTouchRectEvent(MotionEvent ev) {
		boolean ret = false;
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		int disX = Math.abs(x - mInitX);
		int disY = Math.abs(y - mInitY);

		switch (st) {
		case VIDEO_MAP:
			V2Log.i("==check event x:" + x + "   txv x :" + tsv.getLeft()
					+ "   rigth:" + tsv.getRight() + "  top:" + tsv.getTop()
					+ "  bottom:" + tsv.getBottom() +"  disY:" + disY+"  disX:"+ disX+"");
			ret = (x >= (int) tsv.getLeft()
					&& tsv.getRight() >= x
					&& (int) tsv.getTop() <= y && tsv
					.getBottom() >= y);
			break;
		case VIDEO_SHARE:
			ret = true;
			break;
		case VIDEO_SHARE_CONNECTION_REQUESTING:
			break;
		case VIDEO_SHARE_MAP:
			break;
		case VIDEO_SHARE_P2P:
			break;
		default:
			break;
		}
		V2Log.i("==check event ret:" + ret+"   st:"+ st);

		return ret;
	}
	
	
	private void doTouchDown(MotionEvent ev) {
		layoutOffsetY = 0;

		mInitY = (int)ev.getY();
		mLastY = mInitY;
		mInitX = (int)ev.getX();
		
	}
	
	int layoutOffsetY = 0;
	
	private void doTouchMove(MotionEvent ev) {
		int dy =  (int)ev.getY() - mLastY;
		switch (st) {
		case VIDEO_MAP:
			translateTsvAndMap(dy);
			break;
		case VIDEO_SHARE:
			translateTsvAndMap(dy);
			break;
		case VIDEO_SHARE_CONNECTION_REQUESTING:
			break;
		case VIDEO_SHARE_MAP:
			break;
		case VIDEO_SHARE_P2P:
			break;
		default:
			break;
		}
	}
	
	private void doTouchUp(MotionEvent ev) {
		int disY = Math.abs((int)ev.getY() - mInitY);
		switch (st) {
		case VIDEO_MAP:
			if (disY > 100) {
				ps = PostState.GO_NEXT;
				fly.startFlying(getBottom() - disY , ScreenType.VIDEO_SHARE);
			} else {
				ps = PostState.RESTORE;
				fly.startFlying(disY , ScreenType.VIDEO_MAP);
			}
			break;
		case VIDEO_SHARE:
			if (disY > 100) {
				ps = PostState.GO_NEXT;
				fly.startFlying(getBottom() - disY , ScreenType.VIDEO_MAP);
			} else {
				ps = PostState.RESTORE;
				fly.startFlying(disY , ScreenType.VIDEO_SHARE);
			}
			break;
		case VIDEO_SHARE_CONNECTION_REQUESTING:
			break;
		case VIDEO_SHARE_MAP:
			break;
		case VIDEO_SHARE_P2P:
			break;
		default:
			break;
		}
		
	}
	
	
	private void doVideoScreenTap() {
		if (mPosInterface != null) {
			mPosInterface.onVideoScreenClick();
		}
	}
	
	private void postTranslation(int offset) {
		switch (st) {
		case VIDEO_MAP:
			if (ps == PostState.GO_NEXT) {
				translateTsvAndMap(offset);
			} else {
				translateTsvAndMap(-offset);
			}
			break;
		case VIDEO_SHARE:
			if (ps == PostState.GO_NEXT) {
				translateTsvAndMap(-offset);
			} else {
				translateTsvAndMap(offset);
			}
			break;
		case VIDEO_SHARE_CONNECTION_REQUESTING:
			break;
		case VIDEO_SHARE_MAP:
			break;
		case VIDEO_SHARE_P2P:
			break;
		default:
			break;
		}
	}
	
	
	
	class Flying implements Runnable {
		
		int distance;
		ScreenType nextType;
		int velocity;
		public void startFlying(int distance, ScreenType nextType) {
			this.distance = distance;
			this.nextType = nextType;
			velocity = 95;
			postOnAnimation(this);
		}

		@Override
		public void run() {
			V2Log.i("=== remain distance:" +distance +"  velocity:"+ velocity+"   st:"+ st);
			if (distance > 0) {
				if (distance - velocity <= 0) {
					velocity = distance;
				}
				postTranslation(velocity);
				distance -= velocity;
				postOnAnimationDelayed(this, 15);
			} else {
				ps = PostState.IDLE;
				if (nextType != st) {
					st = nextType;
					//TODO Post UI type changed
				}
				requestLayout();
			}
		}
		
	}

	
	
	class FlyingX implements Runnable {

		int initVelocity = 95;
		int distance = 0;
		float cent;
		int type = 1;
		float offset;
		float limition;

		public void startFlying(int offset, int limition) {
			this.offset = offset;
			this.limition = limition;
			this.cent = (float)offset / (float)limition;
			if (this.cent > 0.2F) {
				type = 2;
				distance = Math.abs(tsv.getMeasuredWidth() - offset);
			} else if(cent > 0.0F &&  cent < 0.2F) {
				distance = offset;
				type = 1;
			} else if (this.cent < - 0.2F) {
				type = 1;
				distance =  tsv.getMeasuredWidth() + offset;
			} else if (this.cent > - 0.2F && this.cent < 0.0F) {
				type = 2;
				distance =  -offset;
			}
			postOnAnimation(this);
		}

		@Override
		public void run() {
			if (distance > 0) {
				if (distance - initVelocity < 0) {
					initVelocity = distance;
				}
				
				if (type == 1) {
						offset -= initVelocity;
				} else {
						offset += initVelocity;
				}

				videoController.translate(
						 offset / limition, 0F);
				distance -= initVelocity;

				postOnAnimationDelayed(this, 15);
			} else {
				videoController.finishTranslate();
			}
		}

	};
	
	


	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		
		int bottomChildTop = top + tsv.getMeasuredHeight();

		if (st == ScreenType.VIDEO_MAP) {
			tsv.layout(left, top, right, bottomChildTop);
			shareSurfaceView.layout(left, top, right, bottomChildTop);
			videoShareBtnLayout.layout(left, bottomChildTop, right, bottom);
			mMapView.layout(left, bottomChildTop, right, bottom);
		} else if (st == ScreenType.VIDEO_SHARE) {
			tsv.layout(left, bottom, right, bottom + tsv.getMeasuredHeight());
			shareSurfaceView.layout(left, top, right, bottomChildTop);
			videoShareBtnLayout.layout(left, bottomChildTop, right, bottom);
			mMapView.layout(left, bottom + tsv.getMeasuredHeight(), right, bottom + tsv.getMeasuredHeight() + (bottom - tsv.getMeasuredHeight()) );
		}
		
		LayoutParams lp = (LayoutParams)mMsgLayout.getLayoutParams();
		mMsgLayout.layout(left, top + lp.topMargin, right, top + mMsgLayout.getMeasuredHeight()+ lp.topMargin);
		
//		int bw = bountyMarker.getMeasuredWidth();
//		int bh = bountyMarker.getMeasuredHeight();
//		int bl = left + (right - left - bw) / 2;
//		int br = bl + bw;
//		int bto = mMapView.getTop() + (mMapView.getBottom() - mMapView.getTop() ) / 2 - bh;
//		int btm = bto + bh;
//	//	bountyMarker.layout(bl, bto, br, btm);
		
		
		
		if (liveInformationLayout.getVisibility() == View.VISIBLE) {
			liveInformationLayout.layout(right - liveInformationLayout.getMeasuredWidth(), top, right, bottomChildTop);
		}
		if (liveWatcherLayout.getVisibility() == View.VISIBLE) {
			liveWatcherLayout.layout(left, bottomChildTop - liveWatcherLayout.getMeasuredHeight() , right - liveInformationLayout.getMeasuredWidth(), bottomChildTop);
		}
		if (lierInteractionLayout.getVisibility() == View.VISIBLE) {
			lierInteractionLayout.layout(left,bottomChildTop, right, bottom );
		}
		if (requestConnectLayout.getVisibility() == View.VISIBLE) {
			requestConnectLayout.layout(left, bottomChildTop, right, bottom);
		}
		if (p2pVideoLayout.getVisibility() == View.VISIBLE) {
			p2pVideoLayout.layout(left,bottomChildTop , right, bottom);
		}
		if (p2pAudioWatcherLayout.getVisibility() == View.VISIBLE) {
			p2pAudioWatcherLayout.layout(left, bottomChildTop, right, bottom);
		}
		
	}

	
	
	class NotificationWrapper {
		Live live;
		View v;
		User u;
	}

	
	enum PostState {
		IDLE, RESTORE, GO_NEXT;
	}
	
	enum ScreenType {
		VIDEO_MAP, VIDEO_SHARE, VIDEO_SHARE_CONNECTION_REQUESTING, VIDEO_SHARE_MAP, VIDEO_SHARE_P2P;
	}
	
}
