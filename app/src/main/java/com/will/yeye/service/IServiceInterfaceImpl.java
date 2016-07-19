package com.will.yeye.service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

import com.facebook.AccessToken;
import com.framework.socket.factory.SocketModuleManager;
import com.framework.socket.factory.SocketModuleManagerImpl;
import com.framework.socket.model.SocketConfig;
import com.framework.socket.model.TcpSocketConnectorConfig;
import com.framework.socket.protocol.Protocol;
import com.will.common.log.Logger;
import com.will.yeye.CommonUrlConfig;
import com.will.yeye.TransactionValues;
import com.will.yeye.activity.LoginActivity;
import com.will.yeye.application.App;
import com.will.yeye.db.model.BasicUserInfoDBModel;
import com.will.yeye.model.AccountTModel;
import com.will.yeye.model.LoginServerModel;
import com.will.yeye.socket.WillProtocol;
import com.will.yeye.socket.im.ImLogin;
import com.will.yeye.util.CacheDataManager;
import com.will.yeye.util.NotificationUtil;
import com.will.yeye.util.StartActivityHelper;
import com.will.yeye.R;
import com.will.view.ToastUtils;
import com.will.web.HttpManager;
import com.will.web.okhttp3.OkHttpManager;

import java.util.HashMap;
import java.util.Map;

/**
 * service 业务实现
 */
public class IServiceInterfaceImpl implements IServiceInterface {

    private final int DELAY = 100;
    private final int RETRYTIME = 5;
    private final int PERIOD = 10000;

    private Context mContext;
    private boolean run = false;
    private SocketModuleManager mImSocketModuleManager = null;//im socket 管理
    private HttpManager httpManager = new OkHttpManager();

    public IServiceInterfaceImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public void handleAction(String action, Intent intent) {
        if (IServiceValues.ACTION_CMD_WAY.equals(action)) {

            int cmd = intent.getIntExtra(IServiceValues.KEY_BROADCAST_CMD_VALUE, -1);
            String sign = intent.getStringExtra(IServiceValues.KEY_BROADCAST_SIGN);

            Logger.e("======" + cmd + "----" + sign + "-----");

            if (cmd == -1 || sign == null) {
                return;
            }
            switch (cmd) {
                case IServiceValues.CMD_LOGIN:
                    if (mImSocketModuleManager != null) {
                        mImSocketModuleManager.stopSocket();
                    }
                    final LoginServerModel loginServerModel = intent.getParcelableExtra(TransactionValues.UI_2_SERVICE_KEY1);
                    if (loginServerModel != null) {
                        //连接
                        Protocol protocol = new WillProtocol();
                        TcpSocketConnectorConfig connectorConfig = new TcpSocketConnectorConfig();
                        connectorConfig.setLaucherDelay(DELAY);
                        connectorConfig.setPeriod(PERIOD);
                        connectorConfig.setMaxRetrayTime(RETRYTIME);
                        mImSocketModuleManager = new SocketModuleManagerImpl(connectorConfig, protocol);

                        SocketConfig loginSocketConfig = new SocketConfig();
                        loginSocketConfig.setHost(CommonUrlConfig.OUT_IP);
                        loginSocketConfig.setPort(CommonUrlConfig.OUT_PORT);
                        loginSocketConfig.setTimeout(6000);
                        final ImLogin imLogin = new ImLogin(mContext, loginSocketConfig, mImSocketModuleManager);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                imLogin.performLogin(loginServerModel);
                            }
                        }).start();
                    }
                    break;
                case IServiceValues.CMD_ACCOUNT:
                    final AccountTModel accountTModel = (AccountTModel) intent.getSerializableExtra(TransactionValues.UI_2_SERVICE_KEY1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> params = new HashMap<>();
                            params.put("device", accountTModel.device);
                            params.put("userid", accountTModel.userid);

                            httpManager.getRequest(CommonUrlConfig.PlatformIntoLogIns, params);
                        }
                    }).start();
                    break;
                case IServiceValues.CMD_EXIT_LOGIN:
                    if (mImSocketModuleManager != null) {
                        mImSocketModuleManager.stopSocket();
                    }
                    BasicUserInfoDBModel userInfoDBModel = CacheDataManager.getInstance().loadUser();
                    if(userInfoDBModel != null && userInfoDBModel.userid != null){
                        CacheDataManager.getInstance().deleteMessageRecord(userInfoDBModel.userid);
                    }
                    App.isLogin = false;
                    AccessToken.setCurrentAccessToken(null);
                    Logger.e("退出=========================");
                    break;
                case IServiceValues.CMD_KICK_OUT:
                    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
//                    b.setMessage(R.string.other_place_login_kick_out);
                    b.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    if (mImSocketModuleManager != null) {
                                        mImSocketModuleManager.stopSocket();
                                    }
                                    BasicUserInfoDBModel userInfoDBModel = CacheDataManager.getInstance().loadUser();
                                    if (userInfoDBModel != null && userInfoDBModel.userid != null) {
                                        CacheDataManager.getInstance().deleteMessageRecord(userInfoDBModel.userid);
                                    }
                                    App.isLogin = false;
                                    //AccessToken.setCurrentAccessToken(null);
                                    NotificationUtil.clearAllNotify(mContext);//清理所有的通知
                                    StartActivityHelper.jumpActivity(mContext, Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK, null, LoginActivity.class, null);

                                }
                            }
                    );
                    b.setCancelable(false);
                    AlertDialog d = b.create();
                    d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    d.show();
                    break;
            }


        }
        if (IServiceValues.ACTION_CMD_TEST.equals(action)) {

        }

    }

    @Override
    public void handleNetworkInactive() {
        ToastUtils.showToast(mContext, "没有网络");
    }

    @Override
    public void handleNetworkActivie(int networkType) {

    }

}
