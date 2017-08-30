package com.android.lucia.dribbble.view.bucket_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.lucia.dribbble.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BucketViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.bucket_layout) View bucketLayout;
    @BindView(R.id.bucket_name) public TextView bucketName;
    @BindView(R.id.bucket_shot_count) public TextView bucketShotCount;
    @BindView(R.id.bucket_shot_chosen) public ImageView bucketChosen;

    public BucketViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
