package com.android.lucia.dribbble.view.shot_detail;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.lucia.dribbble.DribbbleAPI.DribbbleAPI;
import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.model.Bucket;
import com.android.lucia.dribbble.model.Shot;
import com.android.lucia.dribbble.utils.Utils;
import com.android.lucia.dribbble.view.bucket_list.BucketListActivity;
import com.android.lucia.dribbble.view.bucket_list.BucketListFragment;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ShotFragment extends Fragment {

    public static final String KEY_SHOT = "shot";
    public static final int REQ_CODE_BUCKET = 100;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private ShotAdapter adapter;
    private Shot shot;
    private ArrayList<String> collectedBucketIds;

    public static ShotFragment newInstance(@NonNull Bundle args) {
        ShotFragment fragment = new ShotFragment();
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
        shot = Utils.toObject(getArguments().getString(KEY_SHOT), new TypeToken<Shot>(){});
        adapter = new ShotAdapter(this, shot);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        AsyncTaskCompat.executeParallel(new CheckShotLikeTask());
        AsyncTaskCompat.executeParallel(new LoadCollectedBucketIdsTask());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_BUCKET && resultCode == Activity.RESULT_OK) {
            List<String> chosenBucketIds = data.getStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS);

            List<String> addedBucketIds = new ArrayList<String>();
            List<String> removedBucketIds = new ArrayList<String>();

            for (String id : chosenBucketIds) {
                if (!collectedBucketIds.contains(id)) {
                    addedBucketIds.add(id);
                }
            }

            for (String id : collectedBucketIds) {
                if (!chosenBucketIds.contains(id)) {
                    removedBucketIds.add(id);
                }
            }

            AsyncTaskCompat.executeParallel(new UpdateBucketTask(addedBucketIds, removedBucketIds));
        }
    }

    public void updateLike(Boolean liked) {
        AsyncTaskCompat.executeParallel(new UpdateLikeTask(liked));
    }

    public void updateBucket() {
        if (collectedBucketIds == null) {
            Snackbar.make(getView(), R.string.shot_detail_loading_buckets, Snackbar.LENGTH_LONG).show();
        }
        else {
            Intent intent = new Intent(getContext(), BucketListActivity.class);
            intent.putExtra(BucketListFragment.KEY_CHOOSE_MODE, true);
            intent.putExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS, collectedBucketIds);
            startActivityForResult(intent, ShotFragment.REQ_CODE_BUCKET);
        }
    }

    public void share() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getString(R.string.share_shot)));
    }

    private class CheckShotLikeTask extends AsyncTask<Void, Void, Boolean> {

        Exception exception;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return DribbbleAPI.isLikingShot(shot.id);
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean liked) {
            if (exception == null) {
                shot.liked = liked;
                adapter.notifyDataSetChanged();
            }
            else {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateLikeTask extends AsyncTask<Void, Void, Void> {
        boolean liked;
        Exception exception;

        UpdateLikeTask(boolean liked) {
            this.liked = liked;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (liked) {
                    DribbbleAPI.likeShot(shot.id);
                }
                else {
                    DribbbleAPI.unlikeShot(shot.id);
                }
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exception == null) {
                shot.liked = liked;
                shot.likes_count += liked ? 1 : -1;
                adapter.notifyDataSetChanged();

                Intent resultIntent = new Intent();
                resultIntent.putExtra(KEY_SHOT, Utils.toString(shot, new TypeToken<Shot>(){}));
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
            }
            else {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }

    }

    private class LoadCollectedBucketIdsTask extends AsyncTask<Void, Void, List<String>> {

        Exception exception;

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                List<Bucket> userBuckets = DribbbleAPI.getUserBuckets();
                List<Bucket> shotBuckets = DribbbleAPI.getShotBuckets(shot.id);


                Set<String> set = new HashSet<>();
                for (Bucket bucket : shotBuckets) {
                    set.add(bucket.id);
                }

                List<String> collectedBucketIds = new ArrayList<>();
                for (Bucket bucket : userBuckets) {
                    if (set.contains(bucket.id)) {
                        collectedBucketIds.add(bucket.id);
                    }
                }

                return collectedBucketIds;
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> bucketIds) {
            if (exception == null) {
                updateCollectedBucketIds(bucketIds);
            }
            else {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateBucketTask extends AsyncTask<Void, Void, Void> {

        private List<String> addedBucketIds;
        private List<String> removedBucketIds;
        private Exception exception;

        private UpdateBucketTask(@NonNull List<String> added, @NonNull List<String> removed) {
            addedBucketIds = added;
            removedBucketIds = removed;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (String bucketId : addedBucketIds) {
                    DribbbleAPI.addShotToBucket(shot.id, bucketId);
                }

                for (String bucketId : removedBucketIds) {
                    DribbbleAPI.removeShotFromBucket(shot.id, bucketId);
                }
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exception == null) {
                updateCollectedBucketIds(addedBucketIds, removedBucketIds);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(KEY_SHOT, Utils.toString(shot, new TypeToken<Shot>(){}));
                getActivity().setResult(Activity.RESULT_OK, resultIntent);

            } else {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void updateCollectedBucketIds(@NonNull List<String> bucketIds) {
        if (collectedBucketIds == null) {
            collectedBucketIds = new ArrayList<>();
        }

        collectedBucketIds.clear();
        collectedBucketIds.addAll(bucketIds);

        shot.bucketed = !bucketIds.isEmpty();
        adapter.notifyDataSetChanged();
    }

    public void updateCollectedBucketIds(List<String> addedBucketIds, List<String> removedBucketIds) {

        if (collectedBucketIds == null) {
            collectedBucketIds = new ArrayList<>();
        }

        if (addedBucketIds != null) {
            collectedBucketIds.addAll(addedBucketIds);
        }

        if (removedBucketIds != null) {
            collectedBucketIds.removeAll(removedBucketIds);
        }

        shot.bucketed = !collectedBucketIds.isEmpty();
        shot.buckets_count += addedBucketIds.size() - removedBucketIds.size();
        adapter.notifyDataSetChanged();
    }
}
