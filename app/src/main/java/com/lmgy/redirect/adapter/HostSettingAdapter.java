package com.lmgy.redirect.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lmgy.redirect.R;
import com.lmgy.redirect.bean.HostData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by lmgy on 15/8/2019
 */
public class HostSettingAdapter extends RecyclerView.Adapter<HostSettingAdapter.HostSettingViewHolder> {
    private Context mContext;
    private List<String> mSelectedIds;
    private AsyncListDiffer<HostData> mDiffer;
    private DiffUtil.ItemCallback<HostData> diffCallback = new DiffUtil.ItemCallback<HostData>() {
        @Override
        public boolean areItemsTheSame(@NonNull HostData oldItem, @NonNull HostData newItem) {
            return oldItem.getHostName().equals(newItem.getHostName()) && oldItem.getIpAddress().equals(newItem.getIpAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull HostData oldItem, @NonNull HostData newItem) {
            return oldItem.getHostName().equals(newItem.getHostName()) && oldItem.getIpAddress().equals(newItem.getIpAddress());
        }
    };

    public HostSettingAdapter(Context context, List<HostData> hostDataList) {
        this.mContext = context;
        mDiffer = new AsyncListDiffer<>(this, diffCallback);
        mSelectedIds = new ArrayList<>();
        mDiffer.submitList(hostDataList);
    }

    public void setHostDataList(List<HostData> mHostDataList) {
        mDiffer.submitList(mHostDataList);
    }

    @NotNull
    @Override
    public HostSettingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_list, parent, false);
        return new HostSettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HostSettingViewHolder holder, int position) {
        holder.setData(getItem(position), position);
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public HostData getItem(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    public void setSelectedIds(List<String> selectedIds) {
        this.mSelectedIds = selectedIds;
        notifyDataSetChanged();
    }

    class HostSettingViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private FrameLayout rootView;

        HostSettingViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            rootView = itemView.findViewById(R.id.root_view);
        }

        public void setData(HostData hostData, int position) {
            String text = hostData.getIpAddress()
                    + "   " + hostData.getHostName()
                    + "   " + hostData.getRemark();
            if (!hostData.getType())
                text = "#" + text;
            title.setText(text);
            if (mSelectedIds.contains(String.valueOf(position))) {
                rootView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorControlActivated)));
            } else {
                rootView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext, android.R.color.transparent)));
            }
        }
    }
}
