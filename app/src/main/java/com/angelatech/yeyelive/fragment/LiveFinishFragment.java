package com.angelatech.yeyelive.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angelatech.yeyelive.R;
import com.angelatech.yeyelive.TransactionValues;
import com.angelatech.yeyelive.activity.function.ChatRoom;
import com.angelatech.yeyelive.model.RoomModel;
import com.angelatech.yeyelive.util.ScreenUtils;
import com.angelatech.yeyelive.view.FrescoBitmapUtils;
import com.angelatech.yeyelive.view.GaussAmbiguity;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * 直播结束页面
 * 观看者结束界面
 *
 */
public class LiveFinishFragment extends DialogFragment implements View.OnClickListener {
    private Button btn_close;
    public RoomModel roomModel;
    private SimpleDraweeView img_head;
    private TextView tv_userName, tv_likeNum;
    private LinearLayout ly_live;
    private ImageView face;
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        view = inflater.inflate(R.layout.activity_live_finish, container, false);
        initView();
        setView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ScreenUtils.getScreenWidth(getActivity()),
                getDialog().getWindow().getAttributes().height);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void initView() {
        btn_close = (Button) view.findViewById(R.id.btn_close);
        img_head = (SimpleDraweeView) view.findViewById(R.id.img_head);
        tv_userName = (TextView) view.findViewById(R.id.txt_barname);
        tv_likeNum = (TextView) view.findViewById(R.id.txt_likenum);
        face = (ImageView) view.findViewById(R.id.face);
        ly_live = (LinearLayout) view.findViewById(R.id.ly_live);
    }

    public void setView() {
        roomModel = (RoomModel) getActivity().getIntent().getSerializableExtra(TransactionValues.UI_2_UI_KEY_OBJECT);
        img_head.setImageURI(Uri.parse(roomModel.getUserInfoDBModel().headurl));
        tv_userName.setText(roomModel.getUserInfoDBModel().nickname);
        tv_likeNum.setText(String.valueOf(roomModel.getLikenum()));
        ly_live.setVisibility(View.INVISIBLE);
        FrescoBitmapUtils.getImageBitmap(getActivity(), roomModel.getUserInfoDBModel().headurl, new FrescoBitmapUtils.BitCallBack() {
            @Override
            public void onNewResultImpl(Bitmap bitmap) {
                final Drawable drawable = GaussAmbiguity.BlurImages(bitmap, getActivity());
                face.setImageDrawable(drawable);
                face.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });

        btn_close.setOnClickListener(this);
    }

    public void setRoomModel(RoomModel model) {
        this.roomModel = model;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                dismiss();
                ChatRoom.closeChatRoom();
                break;
        }
    }
}
