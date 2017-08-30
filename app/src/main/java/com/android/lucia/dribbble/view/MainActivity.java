package com.android.lucia.dribbble.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.lucia.dribbble.DribbbleAPI.DribbbleAPI;
import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.authenticate.LoginActivity;
import com.android.lucia.dribbble.model.User;
import com.android.lucia.dribbble.view.bucket_list.BucketListFragment;
import com.android.lucia.dribbble.view.shot_list.ShotListFragment;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setupActionBar();
        setupDrawer();

        if (savedInstanceState == null) {

            Bundle bundle = new Bundle();
            bundle.putInt(ShotListFragment.KEY_LIST_TYPE, ShotListFragment.LIST_TYPE_POPULAR);
            ShotListFragment fragment = ShotListFragment.newInstance(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        //Set home should be displayed as an "up" affordance.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Enable the "home" button in the corner of the action bar.
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
        );

        drawerLayout.setDrawerListener(drawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                if (item.isChecked()) {
                    drawerLayout.closeDrawers();
                    return false;
                }

                Fragment fragment = null;

                switch (item.getItemId()) {
                    case R.id.drawer_item_home:
                        Bundle bundle = new Bundle();
                        bundle.putInt(ShotListFragment.KEY_LIST_TYPE, ShotListFragment.LIST_TYPE_POPULAR);
                        fragment = ShotListFragment.newInstance(bundle);
                        setTitle(R.string.title_home);
                        break;
                    case R.id.drawer_item_likes:
                        bundle = new Bundle();
                        bundle.putInt(ShotListFragment.KEY_LIST_TYPE, ShotListFragment.LIST_TYPE_LIKED);
                        fragment = ShotListFragment.newInstance(bundle);
                        break;
                    case R.id.drawer_item_buckets:
                        bundle = new Bundle();
                        bundle.putBoolean(BucketListFragment.KEY_CHOOSE_MODE, false);
                        bundle.putStringArrayList(BucketListFragment.KEY_CHOSEN_BUCKET_IDS, new ArrayList<String>());
                        fragment = BucketListFragment.newInstance(bundle);
                        break;
                }

                drawerLayout.closeDrawers();

                if (fragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }

                return true;
            }
        });

        setupNavHeader();
    }

    private void setupNavHeader() {

        View headerView = navigationView.getHeaderView(0);
        User user = DribbbleAPI.getCurrentUser();

        ((SimpleDraweeView) headerView.findViewById(R.id.nav_header_user_picture)).setImageURI(Uri.parse(user.avatar_url));
        ((TextView) headerView.findViewById(R.id.nav_header_user_name)).setText(user.name);

        headerView.findViewById(R.id.nav_header_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DribbbleAPI.Logout(MainActivity.this);

                Intent intent  = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
