package com.angelatech.yeyelive.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.angelatech.yeyelive.R;
import com.angelatech.yeyelive.TransactionValues;
import com.angelatech.yeyelive.activity.base.BaseActivity;
import com.angelatech.yeyelive.db.BaseKey;
import com.angelatech.yeyelive.db.model.BasicUserInfoDBModel;
import com.angelatech.yeyelive.fragment.payPwdDialogFragment;
import com.angelatech.yeyelive.util.CacheDataManager;
import com.angelatech.yeyelive.util.JsonUtil;
import com.angelatech.yeyelive.util.StartActivityHelper;
import com.will.view.ToastUtils;
import com.xj.frescolib.View.FrescoRoundView;

import java.util.Map;


//转账页面
public class TransferActivity extends BaseActivity {

    private ImageView btn_back;
    private FrescoRoundView user_face;
    private TextView txt_username, txt_userid;
    private BasicUserInfoDBModel userInfo;
    private BasicUserInfoDBModel baseInfo;

    private Button btn_submit_pay;
    private EditText txt_money, txt_remark;
    private final int MSG_FIND_TRANSFER_ERROR = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        initView();
        findView();
        initData();
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            baseInfo = (BasicUserInfoDBModel) getIntent().getSerializableExtra(TransactionValues.UI_2_UI_KEY_OBJECT);
        }

        txt_username.setText(baseInfo.nickname);
        txt_userid.setText("ID:" + baseInfo.idx);
        user_face.setImageURI(baseInfo.headurl);
    }

    private void findView() {
        userInfo = CacheDataManager.getInstance().loadUser();
        btn_back.setOnClickListener(this);
        btn_submit_pay.setOnClickListener(this);
    }

    private void initView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        user_face = (FrescoRoundView) findViewById(R.id.user_face);
        txt_userid = (TextView) findViewById(R.id.txt_userid);
        txt_username = (TextView) findViewById(R.id.txt_username);
        btn_submit_pay = (Button) findViewById(R.id.btn_submit_pay);
        txt_money = (EditText) findViewById(R.id.txt_money);
        txt_remark = (EditText) findViewById(R.id.txt_remark);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_submit_pay:
                if (txt_money.getText() != null) {
                    pay();
                }
                break;
        }
    }

    private void pay() {
        final float money = Float.parseFloat(txt_money.getText().toString());
        final String remark = txt_remark.getText().toString();

        payPwdDialogFragment.Callback callback = new payPwdDialogFragment.Callback() {
            @Override
            public void onCancel(String code) {
                //收起键盘
            }

            @Override
            public void onEnter(String response) {
                Map result = JsonUtil.fromJson(response, Map.class);
                Intent intent = new Intent(TransferActivity.this, TransferCompleteActivity.class);
                int code = Integer.parseInt(result.get("code").toString());
                String msg = "";
                intent.putExtra("type", code);
                if (code == 1000) {
                    userInfo.voucher = result.get("data").toString();
                    CacheDataManager.getInstance().update(BaseKey.USER_VOUCHER,  userInfo.voucher,  userInfo.userid);
                    msg = baseInfo.nickname + "已成功收到您的转账";
                } else {
                    switch (code) {
                        case 6005:
                            msg = "余额不足";
                            break;
                        default:
                            msg = "转账失败";
                            break;
                    }
                }
                intent.putExtra("msg", msg);
                startActivity(intent);
                finish();
            }
        };
        payPwdDialogFragment paypwdDialogFragment = new payPwdDialogFragment(TransferActivity.this, callback, baseInfo.userid, remark, money);
        paypwdDialogFragment.show(getFragmentManager(), "");
    }

    @Override
    public void doHandler(Message msg) {
        switch (msg.what) {
            case MSG_FIND_TRANSFER_ERROR:
                if (msg.obj.toString().equals("6003")) {
                    ToastUtils.showToast(this, "交易密码错误！");
                    pay();
                } else {
                    StartActivityHelper.jumpActivityDefault(TransferActivity.this, TransferCompleteActivity.class);
                }
                break;
        }
    }

}
