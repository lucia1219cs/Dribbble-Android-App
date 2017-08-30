package com.android.lucia.dribbble.view.shot_list;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.lucia.dribbble.DribbbleAPI.DribbbleAPI;
import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.model.Shot;
import com.android.lucia.dribbble.model.User;
import com.android.lucia.dribbble.utils.Utils;
import com.android.lucia.dribbble.view.base.ItemDecoration;
import com.android.lucia.dribbble.view.base.LoadMoreListener;
import com.android.lucia.dribbble.view.shot_detail.ShotFragment;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.lucia.dribbble.DribbbleAPI.DribbbleAPI.COUNT_PER_PAGE;


public class ShotListFragment extends Fragment {

    public static final int REQ_CODE_SHOT = 100;
    public static final String KEY_LIST_TYPE = "listType";
    public static final String KEY_BUCKET_ID = "bucketId";
    public static final int LIST_TYPE_POPULAR = 1;
    public static final int LIST_TYPE_LIKED = 2;
    public static final int LIST_TYPE_BUCKET = 3;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private ShotListAdapter adapter;

    public static ShotListFragment newInstance(@NonNull Bundle args) {
        ShotListFragment fragment = new ShotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new ItemDecoration(getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        adapter = new ShotListAdapter(this, new ArrayList<Shot>(), new LoadMoreListener() {
            @Override
            public void onLoadMore() {
                AsyncTaskCompat.executeParallel(new LoadShotTask(adapter.getDataCount() / DribbbleAPI.COUNT_PER_PAGE + 1));
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SHOT && resultCode == Activity.RESULT_OK) {
            Shot updatedShot = Utils.toObject(data.getStringExtra(ShotFragment.KEY_SHOT),
                    new TypeToken<Shot>(){});
            for (Shot shot : adapter.getData()) {
                if (TextUtils.equals(shot.id, updatedShot.id)) {
                    shot.likes_count = updatedShot.likes_count;
                    shot.buckets_count = updatedShot.buckets_count;
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    private class LoadShotTask extends AsyncTask<Void, Void, List<Shot>> {
        int page;
        Exception exception;
        int listType = getArguments().getInt(KEY_LIST_TYPE);

        LoadShotTask(int page) {
            this.page = page;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            try {
                switch (listType) {
                    case LIST_TYPE_POPULAR:
                        return DribbbleAPI.getShots(page);
                    case LIST_TYPE_LIKED:
                        return DribbbleAPI.getLikedShots(page);
                    case LIST_TYPE_BUCKET:
                        String bucketId = getArguments().getString(KEY_BUCKET_ID);
                        return DribbbleAPI.getBucketShots(bucketId, page);
                    default:
                        return DribbbleAPI.getShots(page);
                }
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {

            if (exception == null && shots != null) {
                adapter.append(shots);
                adapter.setShowLoading(shots.size() == DribbbleAPI.COUNT_PER_PAGE);
            }
            else if (exception != null) {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private List<Shot> fakeData(int page) {
        List<Shot> shotList = new ArrayList<>();
        Random random = new Random();

        int count = page < 2 ? COUNT_PER_PAGE : 10;

        for (int i = 0; i < count; ++i) {
            Shot shot = new Shot();
            shot.title = "shot" + i;
            shot.images = new HashMap<>();
            shot.images.put(Shot.IMAGE_HIDPI, imageUrls[random.nextInt(imageUrls.length)]);
            shot.views_count = random.nextInt(10000);
            shot.likes_count = random.nextInt(200);
            shot.buckets_count = random.nextInt(50);
            shot.description = "description";
            shot.user = new User();
            shot.user.name = shot.title + " author";
            shotList.add(shot);
        }
        return shotList;
    }

    private static final String[] imageUrls = {
            "https://d13yacurqjgara.cloudfront.net/users/58851/screenshots/3400841/dribbble_pretoria-04.png",
            "https://d13yacurqjgara.cloudfront.net/users/41719/screenshots/3400864/octowheel.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/1008875/screenshots/3399601/old-pc.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/4381/screenshots/3400780/dribbble-1.png",
            "https://d13yacurqjgara.cloudfront.net/users/559871/screenshots/3401056/gradient_fox.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/79723/screenshots/3401386/untitled-9-01.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/698/screenshots/3401039/ss-2017-cover.png",
            "https://d13yacurqjgara.cloudfront.net/users/45389/screenshots/3400936/portfolium-spaceman.png",
            "https://d13yacurqjgara.cloudfront.net/users/65767/screenshots/3400922/peter_deltondo_virta_health_iphone_responsive_mobile_menu_2x.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/203446/screenshots/3400931/bitmap.png",
            "https://d13yacurqjgara.cloudfront.net/users/235360/screenshots/3400791/how-to.png",
            "https://d13yacurqjgara.cloudfront.net/users/58267/screenshots/3401160/people-socks-rebranding.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/363877/screenshots/3400983/gentle-bird-w.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/33298/screenshots/3400699/dribhat2.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/879147/screenshots/3401051/aaa.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/98561/screenshots/3401583/new_user_experience_style_frames_1x.png",
            "https://d13yacurqjgara.cloudfront.net/users/371094/screenshots/3401298/richkid.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/875337/screenshots/3400965/bg-plane.jpg",
            "https://d13yacurqjgara.cloudfront.net/users/1365782/screenshots/3399506/new_copy_18.png",
            "https://d13yacurqjgara.cloudfront.net/users/44338/screenshots/3401460/aw1_drib.png"
    };
}
