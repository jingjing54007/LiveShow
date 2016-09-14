package com.v2tech.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.V2.jni.util.V2Log;
import com.v2tech.net.DeamonWorker;
import com.v2tech.net.lv.FansQueryReqPacket;
import com.v2tech.net.lv.FansQueryRespPacket;
import com.v2tech.net.lv.FollowsQueryReqPacket;
import com.v2tech.net.lv.FollowsQueryRespPacket;
import com.v2tech.net.pkt.ResponsePacket;
import com.v2tech.service.GlobalHolder;
import com.v2tech.service.MessageListener;
import com.v2tech.service.UserService;
import com.v2tech.service.jni.JNIResponse;
import com.v2tech.vo.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginPresenter extends BasePresenter {

    private static final int INIT = 1;
    private static final int LOGIN_ACTION = 2;
    private static final int GET_CODE_ACTION = 3;
    private static final int LOGIN_CALLBACK = 4;
    private static final int LOGOUT_AS_CALLBACK = 5;

    private LocalState localState = LocalState.IDLE;

    public interface LoginPresenterUI {

        String getUserNameText();

        String getCodeText();

        CharSequence getOriginUserNameText();

        void updateStartButton(boolean enable);

        void appendBlankSpace();

        void showLogingInProgress();

        void doLoggedIn();

        void doReturned();

        void setPhoneNumberError();

        void showKeyboard();

        void doLogonFailed();

        void showIncorrectMsgIncorrectUsername();

        void showNotificationView(boolean flag);
    }

    private LoginPresenterUI ui;
    private UserService us;

    public LoginPresenter(LoginPresenterUI ui) {
        super();
        this.ui = ui;
    }

    @Override
    public void onUICreated() {
        super.onUICreated();
        h = new LocalHandler(backendThread.getLooper());
        Message.obtain(h, INIT).sendToTarget();
    }


    @Override
    public void onUIStarted() {
        super.onUIStarted();
        ui.showKeyboard();
        ui.showNotificationView(false);
    }

    @Override
    public void onUIDestroyed() {
        if (localState == LocalState.IDLE) {
            super.onUIDestroyed();
            us.clearCalledBack();
        }
    }

    public void verificationCodeButtonClicked() {
        Message.obtain(h, GET_CODE_ACTION, null).sendToTarget();
    }

    public void startButtonClicked() {
        ui.showNotificationView(false);
        synchronized (localState) {
            localState = LocalState.LOGGINGIN;
        }
        ui.showLogingInProgress();
        Message.obtain(h, LOGIN_ACTION, null).sendToTarget();
    }

    public void returnButtonClicked() {
        ui.doReturned();
    }

    public void userNameTextChanged() {
        CharSequence cs = ui.getOriginUserNameText();
        int len = cs.length();
        if ((len == 4 || len == 9) && cs.charAt(len - 1) != ' ') {
            ui.appendBlankSpace();
        }
    }

    public void codeTextChanged() {
        if (!TextUtils.isEmpty(ui.getUserNameText())
                && !TextUtils.isEmpty(ui.getCodeText())) {
            ui.updateStartButton(true);
        } else {
            ui.updateStartButton(false);
        }
    }

    private void doLoginInBack() {
        User u = GlobalHolder.getInstance().getCurrentUser();
        if (u != null && u.isNY) {
            us.logout(new MessageListener(h, LOGOUT_AS_CALLBACK, null), true);
        }
        String username = ui.getUserNameText();
        String code = ui.getCodeText();
        us.login(username, code, new MessageListener(h, LOGIN_CALLBACK, null));

    }

    public void doGetCodeInBack() {
        String number = ui.getUserNameText();
        if (TextUtils.isEmpty(number)) {
            ui.setPhoneNumberError();
            return;
        }
        us.sendVaidationCode(number);
    }

    private void handleLoginCallback(JNIResponse resp) {
        synchronized (localState) {
            localState = LocalState.IDLE;
        }
        if (resp.getResult() == JNIResponse.Result.SUCCESS) {
            GlobalHolder.getInstance().getCurrentUser().isNY = false;
            GlobalHolder.getInstance().getCurrentUser()
                    .setMobile(ui.getUserNameText());
            queryFollowersList();
            queryFansList();
            ui.doLoggedIn();
            V2Log.i("=== log in success ful");
        } else {
            ui.showIncorrectMsgIncorrectUsername();
            ui.showNotificationView(true);
            ui.doLogonFailed();
        }
    }

    private void queryFollowersList() {
        FollowsQueryReqPacket req = new FollowsQueryReqPacket();
        ResponsePacket rep = DeamonWorker
                .getInstance().request(req);

        if (rep.getHeader().isError()) {
            V2Log.e(" query followers error " + rep);
            return;
        }

        FollowsQueryRespPacket resp = (FollowsQueryRespPacket) rep;

        if (!resp.getHeader().isError()) {
            List<User> tmp;
            if (resp.follows == null || resp.follows.size() <= 0) {
                tmp = new ArrayList<User>(0);
                return;
            }
            List<User> fans = new ArrayList<User>(resp.follows.size());
            User u = null;
            for (Map<String, String> m : resp.follows) {
                long id = Long.parseLong(m.get("id"));
                String v2idStr = m.get("v2id");
                long v2id = -1;
                if (v2idStr != null && !v2idStr.isEmpty()) {
                    v2id = Long.parseLong(v2idStr);
                }
                u = new User(v2id);
                u.nId = id;

                String strfansC = m.get("fansCount");
                if (!TextUtils.isEmpty(strfansC)) {
                    u.fansCount = Integer.parseInt(strfansC);
                }

                String strFollowCount = m.get("followCount");
                if (!TextUtils.isEmpty(strFollowCount)) {
                    u.followerCount = Integer.parseInt(strFollowCount);
                }

                String strvideoCount = m.get("videoCount");
                if (!TextUtils.isEmpty(strvideoCount)) {
                    u.videoCount = Integer.parseInt(strvideoCount);
                }
                fans.add(u);
            }

            tmp = fans;
            GlobalHolder.getInstance().mMyFollowers = tmp;
        } else {
            // TODO query error
        }
    }

    private void queryFansList() {
        FansQueryReqPacket req = new FansQueryReqPacket();
        ResponsePacket rep = DeamonWorker
                .getInstance().request(req);

        if (rep.getHeader().isError()) {
            V2Log.e(" query queryFansList error " + rep);
            return;
        }

        FansQueryRespPacket resp = (FansQueryRespPacket) rep;

        if (!resp.getHeader().isError()) {
            List<User> tmp;
            if (resp.fansList == null || resp.fansList.size() <= 0) {
                tmp = new ArrayList<User>(0);
            }
            List<User> fans = new ArrayList<User>(resp.fansList.size());
            User u = null;
            for (Map<String, String> m : resp.fansList) {
                long id = Long.parseLong(m.get("id"));
                String v2idStr = m.get("v2id");
                long v2id = -1;
                if (v2idStr != null && !v2idStr.isEmpty()) {
                    v2id = Long.parseLong(v2idStr);
                }
                u = new User(v2id);
                u.nId = id;
                String strfansC = m.get("fansCount");
                if (!TextUtils.isEmpty(strfansC)) {
                    u.fansCount = Integer.parseInt(strfansC);
                }

                String strFollowCount = m.get("followCount");
                if (!TextUtils.isEmpty(strFollowCount)) {
                    u.followerCount = Integer.parseInt(strFollowCount);
                }

                String strvideoCount = m.get("videoCount");
                if (!TextUtils.isEmpty(strvideoCount)) {
                    u.videoCount = Integer.parseInt(strvideoCount);
                }
                fans.add(u);
            }

            tmp = fans;
            GlobalHolder.getInstance().mMyFans = tmp;
        } else {
            // TODO query error
        }
    }

    private Handler h;

    class LocalHandler extends Handler {

        public LocalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int w = msg.what;
            switch (w) {
                case INIT:
                    us = new UserService();
                    break;
                case LOGIN_ACTION:
                    doLoginInBack();
                    break;
                case GET_CODE_ACTION:
                    doGetCodeInBack();
                    break;
                case LOGIN_CALLBACK:
                    handleLoginCallback((JNIResponse) msg.obj);
                    break;
            }

            if (uiState == UIState.DESTROYED) {
                onUIDestroyed();
                us.clearCalledBack();
            }
        }
    }


    enum LocalState {
        IDLE,
        GETING_CODE,
        LOGGINGIN;
    }
}
