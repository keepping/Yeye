package com.angelatech.yeyelive1.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.angelatech.yeyelive1.CommonUrlConfig;
import com.angelatech.yeyelive1.Constant;
import com.angelatech.yeyelive1.R;
import com.angelatech.yeyelive1.TransactionValues;
import com.angelatech.yeyelive1.activity.base.WithBroadCastActivity;
import com.angelatech.yeyelive1.activity.function.SearchUser;
import com.angelatech.yeyelive1.adapter.CommonAdapter;
import com.angelatech.yeyelive1.adapter.ViewHolder;
import com.angelatech.yeyelive1.db.model.BasicUserInfoDBModel;
import com.angelatech.yeyelive1.model.BasicUserInfoModel;
import com.angelatech.yeyelive1.model.CommonListResult;
import com.angelatech.yeyelive1.model.CommonModel;
import com.angelatech.yeyelive1.model.SearchItemModel;
import com.angelatech.yeyelive1.util.CacheDataManager;
import com.angelatech.yeyelive1.util.JsonUtil;
import com.angelatech.yeyelive1.util.StartActivityHelper;
import com.angelatech.yeyelive1.util.Utility;
import com.angelatech.yeyelive1.web.HttpFunction;
import com.facebook.AccessToken;
import com.google.gson.reflect.TypeToken;
import com.will.common.log.DebugLogs;
import com.will.common.string.Encryption;
import com.will.web.handle.HttpBusinessCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends WithBroadCastActivity {

    private final int MSG_UPDATE_SEARCH_RESULT = 1;
    private final int MSG_NO_DATA = 2;
    private final int MSG_ADAPTER_NOTIFY = 3;
    private final int MSG_SET_FOLLOW = 4;
    private final int MSG_CLEAR_DATA = 5;

    private final int MSG_UPDATE_FRIEND_RESULT = 11;

    private ListView searchListView;
    private EditText searchEditText;
    private CommonAdapter<SearchItemModel> adapter;
    private volatile List<SearchItemModel> datas = new ArrayList<>();

    private TextView searchCancel,txt_title;

    private SearchUser searchUser;
    private BasicUserInfoDBModel model;

    private volatile String searchKey;

    private RelativeLayout noDataLayout;

    private String before = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search);
        initView();
        setView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.closeKeybord(searchEditText, this);
    }

    private void initView() {
        model = CacheDataManager.getInstance().loadUser();
        searchUser = new SearchUser(this);
        searchEditText = (EditText) findViewById(R.id.search_input);
        searchListView = (ListView) findViewById(R.id.search_list);
        searchCancel = (TextView) findViewById(R.id.search_cancel);
        txt_title = (TextView) findViewById(R.id.txt_title);
        noDataLayout = (RelativeLayout)findViewById(R.id.no_data_layout);

        adapter = new CommonAdapter<SearchItemModel>(this, datas, R.layout.item_search) {
            @Override
            public void convert(ViewHolder helper, SearchItemModel item, final int position) {
                helper.setText(R.id.user_nick, item.nickname);
                helper.setImageViewByImageLoader(R.id.user_face, item.headurl);

                if (Constant.SEX_MALE.equals(item.sex)) {
                    helper.setImageResource(R.id.user_sex, R.drawable.icon_information_boy);
                } else {
                    helper.setImageResource(R.id.user_sex, R.drawable.icon_information_girl);
                }
                if (SearchItemModel.HAVE_FOLLOW.equals(item.isfollow)) {
                    helper.setImageResource(R.id.attention_btn, R.drawable.btn_focus_cancel);
                } else {
                    helper.setImageResource(R.id.attention_btn, R.drawable.btn_focus);
                }
                //0 无 1 v 2 金v 9官
                if (item.isv!=null){
                    switch (item.isv){
                        case "1":
                            helper.setImageResource(R.id.iv_vip,R.drawable.icon_identity_vip_white);
                            helper.showView(R.id.iv_vip);
                            break;
                        case "2":
                            helper.setImageResource(R.id.iv_vip,R.drawable.icon_identity_vip_gold);
                            helper.showView(R.id.iv_vip);
                            break;
                        case "9":
                            helper.setImageResource(R.id.iv_vip,R.drawable.icon_identity_official);
                            helper.showView(R.id.iv_vip);
                            break;
                        default:
                            helper.hideView(R.id.iv_vip);
                            break;
                    }
                }
                helper.setOnClick(R.id.attention_btn, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doFocus(datas.get(position), position);
                    }
                });

                if(model.userid.equals(item.userid)){
                    helper.hideView(R.id.attention_btn);
                }else{
                    helper.showView(R.id.attention_btn);
                }
            }
        };
    }

    private void setView() {
        searchListView.setAdapter(adapter);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchTask != null) {
                    uiHandler.removeCallbacks(searchTask);
                    noDataLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    searchKey = s.toString();
                    uiHandler.postDelayed(searchTask, 1000);
                } else {
                    uiHandler.obtainMessage(MSG_CLEAR_DATA, 0, 0).sendToTarget();
                }
            }
        });

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //关闭搜素输入框
                Utility.closeKeybord(searchEditText,SearchActivity.this);
                SearchItemModel searchItemModel = datas.get(position);
                BasicUserInfoModel userInfoModel = new BasicUserInfoModel();
                userInfoModel.Userid = searchItemModel.userid;
                StartActivityHelper.jumpActivity(SearchActivity.this, FriendUserInfoActivity.class, userInfoModel);
            }
        });

        searchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.closeKeybord(searchEditText,SearchActivity.this);
                finish();
            }
        });

        Utility.openKeybord(searchEditText, this);
        noDataLayout.setVisibility(View.GONE);
        if(AccessToken.getCurrentAccessToken() != null &&AccessToken.getCurrentAccessToken().getToken() != null )
        {
            Responsefbfriends();
        }
    }

    public Runnable searchTask = new Runnable() {
        @Override
        public void run() {
            ResponseRelationSearch();
        }
    };

    private void ResponseRelationSearch() {
        final HttpBusinessCallback callback = new HttpBusinessCallback() {
            @Override
            public void onFailure(Map<String, ?> errorMap) {}

            @Override
            public void onSuccess(String response) {
                CommonListResult<SearchItemModel> results = JsonUtil.fromJson(response, new TypeToken<CommonListResult<SearchItemModel>>() {
                }.getType());
                if (results != null && HttpFunction.isSuc(results.code)) {
                    if (results.hasData()) {
                        uiHandler.obtainMessage(MSG_UPDATE_SEARCH_RESULT, 0, 0, results.data).sendToTarget();
                    } else {
                        uiHandler.obtainMessage(MSG_NO_DATA, 0, 0).sendToTarget();
                    }
                }
            }
        };
        if (searchKey == null || "".equals(searchKey)) {
            return;
        }
        searchUser.searchUser(model.userid, model.token, Encryption.utf8ToUnicode(searchKey), callback);
    }

    private void Responsefbfriends() {
        final HttpBusinessCallback callback = new HttpBusinessCallback() {
            @Override
            public void onFailure(Map<String, ?> errorMap) {}

            @Override
            public void onSuccess(String response) {
                CommonListResult<SearchItemModel> results = JsonUtil.fromJson(response, new TypeToken<CommonListResult<SearchItemModel>>() {
                }.getType());
                if (results != null && HttpFunction.isSuc(results.code)) {
                    //before = results.before;
                    if (results.hasData()) {
                        uiHandler.obtainMessage(MSG_UPDATE_FRIEND_RESULT, 0, 0, results.data).sendToTarget();
                    } else {
                        uiHandler.obtainMessage(MSG_NO_DATA, 0, 0).sendToTarget();
                    }
                }
            }
        };

        searchUser.getfbfriends(model.userid, model.token, before, AccessToken.getCurrentAccessToken().getToken(), callback);
    }

    @Override
    public void doHandler(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SEARCH_RESULT:
                txt_title.setVisibility(View.GONE);
                if (searchKey == null || "".equals(searchKey)) {
                    return;
                }
                datas = (List<SearchItemModel>) msg.obj;
                adapter.setData(datas);
                adapter.notifyDataSetChanged();
                break;
            case MSG_UPDATE_FRIEND_RESULT:
                txt_title.setVisibility(View.VISIBLE);
                datas = (List<SearchItemModel>) msg.obj;
                adapter.setData(datas);
                adapter.notifyDataSetChanged();
                break;
            case MSG_NO_DATA:
                txt_title.setVisibility(View.GONE);
                showNoDataLayout();
                datas = new ArrayList<>();
                adapter.setData(datas);
                adapter.notifyDataSetChanged();
                break;
            case MSG_CLEAR_DATA:
                noDataLayout.setVisibility(View.GONE);
                datas = new ArrayList<>();
                adapter.setData(datas);
                adapter.notifyDataSetChanged();
                break;
            case MSG_ADAPTER_NOTIFY:
                adapter.notifyDataSetChanged();
                break;
            case MSG_SET_FOLLOW:
                adapter.notifyDataSetChanged();
//                ToastUtils.showToast(SearchActivity.this, getString(R.string.success));
                break;
        }
    }

    @Override
    protected void doReceive(String action, Intent intent) {
        if(WithBroadCastActivity.ACTION_WITH_BROADCAST_ACTIVITY.equals(action)){
            SearchItemModel searchItemModel = (SearchItemModel) intent.getSerializableExtra(TransactionValues.UI_2_UI_KEY_OBJECT);
            if(searchItemModel != null){
                for(SearchItemModel data:datas){
                    if(data.userid.equals(searchItemModel.userid)){
                        data.isfollow = searchItemModel.isfollow;
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void doFocus(SearchItemModel data, final int position) {
        HttpBusinessCallback callback = new HttpBusinessCallback() {
            @Override
            public void onFailure(Map<String, ?> errorMap) {
                DebugLogs.e("===============失败了");
            }
            @Override
            public void onSuccess(String response) {
                CommonModel results = JsonUtil.fromJson(response, CommonModel.class);
                if (results != null) {
                    if(HttpFunction.isSuc(results.code)){
                        if (datas.get(position).isfollow.equals("1")) {
                            datas.get(position).isfollow = "0";
                        } else {
                            datas.get(position).isfollow = "1";
                        }
                        uiHandler.obtainMessage(MSG_SET_FOLLOW).sendToTarget();
                    }
                    else{
                        onBusinessFaild(results.code);
                    }
                }

            }
        };
        BasicUserInfoDBModel model = CacheDataManager.getInstance().loadUser();
        searchUser.UserFollow(CommonUrlConfig.UserFollow, model.token, model.userid, data.userid, Integer.valueOf(data.isfollow), callback);
    }

    private void showNoDataLayout(){
        noDataLayout.setVisibility(View.VISIBLE);
        noDataLayout.findViewById(R.id.hint_textview2).setVisibility(View.GONE);
        ((TextView)noDataLayout.findViewById(R.id.hint_textview1)).setText(getString(R.string.no_data_not_match_people));
    }
}