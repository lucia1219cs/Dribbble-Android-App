package com.android.lucia.dribbble.view.shot_list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.model.Shot;
import com.android.lucia.dribbble.utils.Utils;
import com.android.lucia.dribbble.view.base.LoadMoreListener;
import com.android.lucia.dribbble.view.shot_detail.ShotActivity;
import com.android.lucia.dribbble.view.shot_detail.ShotFragment;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.google.gson.reflect.TypeToken;

import java.util.List;


public class ShotListAdapter extends RecyclerView.Adapter {

    private static final int SHOT_VIEW_TYPE = 0;
    private static final int LOADING_VIEW_TYPE = 1;

    private List<Shot> data;
    private LoadMoreListener loadMoreListener;
    private boolean showLoading;
    private ShotListFragment shotListFragment;

    public ShotListAdapter(@NonNull ShotListFragment shotListFragment, @NonNull List<Shot> data, @NonNull LoadMoreListener loadMoreListener) {
        this.shotListFragment = shotListFragment;
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch(viewType) {
            case SHOT_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shot, parent, false);
                return new ShotViewHolder(view);
            case LOADING_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loading, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {
            case SHOT_VIEW_TYPE:
                final Shot shot = data.get(position);

                ShotViewHolder shotViewHolder = (ShotViewHolder) holder;
                shotViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
                shotViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
                shotViewHolder.viewCount.setText(String.valueOf(shot.views_count));

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(shot.getImageUrl()))
                        .setAutoPlayAnimations(true)
                        .build();
                shotViewHolder.image.setController(controller);

                shotViewHolder.cover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = holder.itemView.getContext();
                        Intent intent = new Intent(context, ShotActivity.class);
                        intent.putExtra(ShotFragment.KEY_SHOT,
                                Utils.toString(shot, new TypeToken<Shot>(){}));
                        intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);
                        shotListFragment.startActivityForResult(intent, ShotListFragment.REQ_CODE_SHOT);
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
            return SHOT_VIEW_TYPE;
        }
        else {
            return LOADING_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    public void append(@NonNull List<Shot> moreShots) {
        data.addAll(moreShots);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public List<Shot> getData() {
        return data;
    }
}
