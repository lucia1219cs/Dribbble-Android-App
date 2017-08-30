package com.android.lucia.dribbble.view.bucket_list;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.view.shot_list.ShotListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BucketShotListActivity extends AppCompatActivity {

    public static final String KEY_BUCKET_NAME = "bucketName";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        ButterKnife.bind(this);

        setupSupportActionBar();

        setTitle(getIntent().getStringExtra(KEY_BUCKET_NAME));

        if (savedInstanceState == null) {
            String bucketId = getIntent().getStringExtra(ShotListFragment.KEY_BUCKET_ID);

            Bundle bundle = new Bundle();
            bundle.putInt(ShotListFragment.KEY_LIST_TYPE, ShotListFragment.LIST_TYPE_BUCKET);
            bundle.putString(ShotListFragment.KEY_BUCKET_ID, bucketId);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, ShotListFragment.newInstance(bundle))
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSupportActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
