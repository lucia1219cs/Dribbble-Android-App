package com.android.lucia.dribbble.view.bucket_list;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.lucia.dribbble.DribbbleAPI.DribbbleAPI;
import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.model.Bucket;
import com.android.lucia.dribbble.view.base.ItemDecoration;
import com.android.lucia.dribbble.view.base.LoadMoreListener;
import com.google.gson.JsonSyntaxException;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BucketListFragment extends Fragment {

    public static final int REQ_CODE_NEW_BUCKET = 100;
    public static final String KEY_CHOOSE_MODE = "choose_mode";
    public static final String KEY_CHOSEN_BUCKET_IDS = "chosen_bucket_ids";

    @BindView(R.id.fab) FloatingActionButton fabButton;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private BucketListAdapter adapter;
    private boolean chooseMode;
    private List<String> chosenBucketIds;

    public static BucketListFragment newInstance(@NonNull Bundle args) {
        BucketListFragment fragment = new BucketListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fab_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new ItemDecoration(getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        chooseMode = getArguments().getBoolean(KEY_CHOOSE_MODE);
        if (chooseMode) {
            chosenBucketIds = getArguments().getStringArrayList(KEY_CHOSEN_BUCKET_IDS);
            if (chosenBucketIds == null) {
                chosenBucketIds = new ArrayList<>();
            }
        }

        adapter = new BucketListAdapter(new ArrayList<Bucket>(), new LoadMoreListener() {
            @Override
            public void onLoadMore() {
                AsyncTaskCompat.executeParallel(new LoadBucketTask(adapter.getDataCount() / DribbbleAPI.COUNT_PER_PAGE + 1));
            }
        }, chooseMode);
        recyclerView.setAdapter(adapter);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewBucketFragment newBucketFragment = new NewBucketFragment();
                newBucketFragment.setTargetFragment(BucketListFragment.this, REQ_CODE_NEW_BUCKET);
                newBucketFragment.show(getFragmentManager(), null);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (chooseMode) {
            inflater.inflate(R.menu.bucket_list_choose_mode_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            ArrayList<String> chosenBucketIds = adapter.getSelectedBucketIds();

            Intent result = new Intent();
            result.putStringArrayListExtra(KEY_CHOSEN_BUCKET_IDS, chosenBucketIds);
            getActivity().setResult(Activity.RESULT_OK, result);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_NEW_BUCKET && resultCode == Activity.RESULT_OK) {
            Bucket bucket = new Bucket();
            bucket.name = data.getStringExtra(NewBucketFragment.KEY_BUCKET_NAME);
            bucket.description = data.getStringExtra(NewBucketFragment.KEY_BUCKET_DESCRIPTION);
            if (!TextUtils.isEmpty(bucket.name)) {
                AsyncTaskCompat.executeParallel(new CreateBucketTask(bucket));
            }
        }

    }

    private class LoadBucketTask extends AsyncTask<Void, Void, List<Bucket>> {
        int page;
        Exception exception;

        LoadBucketTask(int page) {
            this.page = page;
        }

        @Override
        protected List<Bucket> doInBackground(Void... params) {
            try {
                return DribbbleAPI.getUserBuckets(page);
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Bucket> buckets) {

            if (exception == null && buckets != null) {
                if (chooseMode) {
                    for (Bucket bucket : buckets) {
                        if (chosenBucketIds.contains(bucket.id)) {
                            bucket.isChoosing = true;
                        }
                    }
                }
                adapter.append(buckets);
                adapter.setShowLoading(buckets.size() == DribbbleAPI.COUNT_PER_PAGE);
            }
            else if (exception != null) {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class CreateBucketTask extends AsyncTask<Void, Void, Bucket> {
        Bucket bucket;
        Exception exception;

        CreateBucketTask(Bucket bucket) {
            this.bucket = bucket;
        }

        @Override
        protected Bucket doInBackground(Void... params) {
            try {
                return DribbbleAPI.createBucket(bucket);
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bucket bucket) {

            if (exception == null && bucket != null) {
                adapter.append(bucket);
            }
            else if (exception != null) {
                Snackbar.make(getView(), exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private List<Bucket> fakeData() {
        List<Bucket> bucketList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; ++i) {
            Bucket bucket = new Bucket();
            bucket.name = "Bucket" + i;
            bucket.shots_count = random.nextInt(100);
            bucketList.add(bucket);
        }
        return bucketList;
    }
}
