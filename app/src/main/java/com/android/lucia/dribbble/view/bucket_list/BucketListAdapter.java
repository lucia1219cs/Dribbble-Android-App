package com.android.lucia.dribbble.view.bucket_list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.model.Bucket;
import com.android.lucia.dribbble.view.base.LoadMoreListener;
import com.android.lucia.dribbble.view.shot_list.ShotListFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;


public class BucketListAdapter extends RecyclerView.Adapter {

    private static final int BUCKET_VIEW_TYPE = 0;
    private static final int LOADING_VIEW_TYPE = 1;

    private List<Bucket> data;
    private LoadMoreListener loadMoreListener;
    private boolean showLoading;
    private boolean chooseMode;

    public BucketListAdapter(@NonNull List<Bucket> data, @NonNull LoadMoreListener loadMoreListener, Boolean chooseMode) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.chooseMode = chooseMode;
        showLoading = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch(viewType) {
            case BUCKET_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bucket, parent, false);
                return new BucketViewHolder(view);
            case LOADING_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loading, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            default:
                return null;

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {
            case BUCKET_VIEW_TYPE:
                final Bucket bucket = data.get(position);

                BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;

                String bucketShotCountString = MessageFormat.format(
                        holder.itemView.getContext().getResources().getString(R.string.shot_count),
                        bucket.shots_count);

                bucketViewHolder.bucketName.setText(String.valueOf(bucket.name));
                bucketViewHolder.bucketShotCount.setText(bucketShotCountString);

                if(!chooseMode) {
                    bucketViewHolder.bucketChosen.setVisibility(View.GONE);
                    bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), BucketShotListActivity.class);
                            intent.putExtra(BucketShotListActivity.KEY_BUCKET_NAME, bucket.name);
                            intent.putExtra(ShotListFragment.KEY_BUCKET_ID, bucket.id);
                            v.getContext().startActivity(intent);
                        }
                    });
                }
                else {
                    bucketViewHolder.bucketChosen.setVisibility(View.VISIBLE);

                    if (bucket.isChoosing) {
                        bucketViewHolder.bucketChosen.setImageResource(R.drawable.ic_check_box_black_24dp);
                    }
                    else {
                        bucketViewHolder.bucketChosen.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
                    }
                }

                bucketViewHolder.bucketChosen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket.isChoosing = !bucket.isChoosing;
                        notifyItemChanged(position);
                    }
                });

                break;
            case LOADING_VIEW_TYPE:
                loadMoreListener.onLoadMore();
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < data.size()) {
            return BUCKET_VIEW_TYPE;
        }
        else {
            return LOADING_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    public void append(@NonNull List<Bucket> moreBuckets) {
        data.addAll(moreBuckets);
        notifyDataSetChanged();
    }

    public void append(@NonNull Bucket newBucket) {
        data.add(newBucket);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    @NonNull
    public ArrayList<String> getSelectedBucketIds() {
        ArrayList<String> selectedBucketIds = new ArrayList<>();
        for (Bucket bucket : data) {
            if (bucket.isChoosing) {
                selectedBucketIds.add(bucket.id);
            }
        }
        return selectedBucketIds;
    }

}
