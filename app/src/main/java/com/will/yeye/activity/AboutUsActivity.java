package com.will.yeye.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.will.yeye.CommonUrlConfig;
import com.will.yeye.activity.base.HeaderBaseActivity;
import com.will.yeye.model.WebTransportModel;
import com.will.yeye.util.StartActivityHelper;
import com.will.yeye.util.Utility;
import com.will.yeye.R;

/**
 * Created by:      xujian
 * Version          ${version}
 * Date:            16/4/15
 * Description(描述):
 * Modification  History(历史修改): 关于我们
 * Date              Author          Version
 * ---------------------------------------------------------
 * 16/4/15          xujian         ${version}
 * Why & What is modified(修改原因):
 * 关于我们
 */
public class AboutUsActivity extends HeaderBaseActivity implements View.OnClickListener{
    private LinearLayout btn_agreement;
    private TextView versionCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        headerLayout.showTitle(getString(R.string.about_title));
        headerLayout.showLeftBackButton(R.id.backBtn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        versionCode = (TextView)findViewById(R.id.versionCode);
        versionCode.setText(String.format("%s%s ", getString(R.string.versionCode), Utility.getVersionName(AboutUsActivity.this)));
        btn_agreement = (LinearLayout)findViewById(R.id.btn_agreement);
        btn_agreement.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_agreement:
                WebTransportModel webTransportModel = new WebTransportModel();
                webTransportModel.url = CommonUrlConfig.Agreement;
                webTransportModel.title = getString(R.string.lisence_title);
                StartActivityHelper.jumpActivity(AboutUsActivity.this,WebActivity.class,webTransportModel);
                break;
        }
    }
}
