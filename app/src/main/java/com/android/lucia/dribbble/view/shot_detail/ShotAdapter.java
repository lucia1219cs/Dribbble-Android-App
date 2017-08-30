package com.android.lucia.dribbble.view.shot_detail;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.model.Shot;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

public class ShotAdapter extends RecyclerView.Adapter {

    private static final int SHOT_IMAGE_VIEW_TYPE = 0;
    private static final int SHOT_INFO_VIEW_TYPE = 1;

    private Shot data;

    private ShotFragment shotFragment;

    public ShotAdapter(@NonNull ShotFragment fragment, @NonNull Shot shot) {
        this.data = shot;
        shotFragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch(viewType) {
            case SHOT_IMAGE_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_image, parent, false);
                return new ShotImageViewHolder(view);
            case SHOT_INFO_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_info, parent, false);
                return new ShotInfoViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case SHOT_IMAGE_VIEW_TYPE:
                ShotImageViewHolder shotImageViewHolder = (ShotImageViewHolder) holder;
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(data.getImageUrl()))
                        .setAutoPlayAnimations(true)
                        .build();
                shotImageViewHolder.image.setController(controller);
                break;
            case SHOT_INFO_VIEW_TYPE:
                ShotInfoViewHolder shotInfoViewHolder = (ShotInfoViewHolder) holder;

                shotInfoViewHolder.likeCount.setText(String.valueOf(data.likes_count));
                shotInfoViewHolder.bucketCount.setText(String.valueOf(data.buckets_count));
                shotInfoViewHolder.viewCount.setText(String.valueOf(data.views_count));
                shotInfoViewHolder.title.setText(data.title);
                shotInfoViewHolder.authorName.setText(data.user.name);
                shotInfoViewHolder.authorPicture.setImageURI(Uri.parse(data.user.avatar_url));
                shotInfoViewHolder.description.setText(Html.fromHtml(data.description == null ? "" : data.description));

                if (data.liked) {
                    shotInfoViewHolder.likeButton.setImageResource(R.drawable.ic_favorite_dribbble_18dp);
                }
                else {
                    shotInfoViewHolder.likeButton.setImageResource(R.drawable.ic_favorite_border_black_18dp);
                }

                if (data.bucketed){
                    shotInfoViewHolder.bucketButton.setImageResource(R.drawable.ic_inbox_dribbble_18dp);
                }
                else {
                    shotInfoViewHolder.bucketButton.setImageResource(R.drawable.ic_inbox_black_18dp);
                }

                shotInfoViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.updateLike(!data.liked) ;
                    }
                });

                shotInfoViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    shotFragment.updateBucket();
                    }
                });

                shotInfoViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shotFragment.share();
                    }
                });

                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return SHOT_IMAGE_VIEW_TYPE;
            case 1:
                return SHOT_INFO_VIEW_TYPE;
            default:
                return -1;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
