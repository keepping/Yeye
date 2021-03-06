package com.angelatech.yeyelive1.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.angelatech.yeyelive1.R;
import com.angelatech.yeyelive1.model.OnlineListModel;
import com.angelatech.yeyelive1.util.VerificationUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

public class HorizontalListViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<OnlineListModel> mData;

    public HorizontalListViewAdapter(Context context) {
        this.mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
    }

    public void setData(List<OnlineListModel> data){
        if(data!=null){
            mData = data;
            notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_chatroom_gallery, parent, false);
            holder.mImage = (SimpleDraweeView) convertView.findViewById(R.id.item_chatRoom_gallery_image);
            holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //0 无 1 v 2 金v 9官
        switch (mData.get(position).isv){
            case "0":
                holder.iv_vip.setVisibility(View.GONE);
                break;
            case "1":
                holder.iv_vip.setImageResource(R.drawable.icon_identity_vip_white);
                holder.iv_vip.setVisibility(View.VISIBLE);
                break;
            case "2":
                holder.iv_vip.setImageResource(R.drawable.icon_identity_vip_gold);
                holder.iv_vip.setVisibility(View.VISIBLE);
                break;
            case "9":
                holder.iv_vip.setImageResource(R.drawable.icon_identity_official);
                holder.iv_vip.setVisibility(View.VISIBLE);
                break;
            default:
                holder.iv_vip.setVisibility(View.GONE);
                break;
        }
        holder.mImage.setBackgroundResource(R.drawable.default_face_icon);
        holder.mImage.setImageURI(Uri.parse(VerificationUtil.getImageUrl(mData.get(position).headphoto)));
        return convertView;
    }

    private static class ViewHolder {
        private SimpleDraweeView mImage;
        private ImageView iv_vip;
    }
}