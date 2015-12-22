package ndejaco.pollgeo;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ndejaco.pollgeo.Model.Poll;

public class ProfileActivity extends Activity {

    protected ParseUser currentUser;
    private ListView mDrawerList;
    private String[] mSections;
    private DrawerLayout mDrawerLayout;
    private HomeViewAdapter mProfileViewAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    //Refresh layout swipe
    private PullRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle extras = getIntent().getExtras();
        String user = "";
        if (extras != null) {
            user = (String) extras.get("target");
        }

        ParseUser pu = null;
        if (user.equals(ParseUser.getCurrentUser().getUsername())) {
            pu = ParseUser.getCurrentUser();
        } else if (!user.equals("")) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", user);
            List<ParseUser> userList = null;
            try {
                userList = query.find();
            } catch (Exception e) {
            }

            if (userList != null) {
                try {
                    pu = userList.get(0);
                } catch (Exception e) {
                    Log.e("In profile activity", "The list is empty.");
                }
            } else {
                Log.e("In profile activity", "The returned list is null");
            }
        }

        if (pu == null) {
            Log.e("in profile activity", "exception happened.");
        }

        currentUser = pu;

        ProfilePictureView fbPhoto = (ProfilePictureView) findViewById(R.id.profilePicture);
        fbPhoto.setPresetSize(ProfilePictureView.LARGE);
        if (pu.get("facebookId") != null) {
            fbPhoto.setProfileId((String) pu.get("facebookId"));
        }

        TextView userName = (TextView) findViewById(R.id.profileName);
        userName.setText((String) pu.get("name"));

        TextView scoreText = (TextView) findViewById(R.id.scoreText);
        Integer userScore = (Integer) pu.get("score");
        if (userScore == null) {
            userScore = 0;
        }

        scoreText.setText("Score: " + userScore);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ProfilePictureView fbThumb = (ProfilePictureView) findViewById(R.id.thumbnail);

        if (currentUser != null) {
            fbThumb.setPresetSize(ProfilePictureView.LARGE);
            String profileId = ParseUser.getCurrentUser().getString("facebookId");
            if (profileId != null) {
                fbThumb.setProfileId(profileId);
            } else {

            }
        }

        // set up the drawer's list view with items and click listener
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerList.setAdapter(new DrawerAdapter(this, mSections));

        ListView lv = (ListView) findViewById(R.id.list);


        mProfileViewAdapter = new HomeViewAdapter(this, new ArrayList<Poll>());
        lv.setAdapter(mProfileViewAdapter);


        android.app.ActionBar actionBar = getActionBar();
        try{
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(currentUser.get("name") + "'s profile");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        }
        catch(NoSuchMethodError e){
        }

        updateData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateData() {

        // Queries poll data and orders by most recent polls. Finds in background.
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return;
        }

        ParseQuery<Poll> query = new ParseQuery<Poll>("Poll");
        query.orderByDescending("createdAt");
        try {
            currentUser.fetchIfNeeded();
        } catch (Exception e) {
            return;
        }

        Log.i("Here -->", "In update data with " + currentUser.getObjectId());
        query.whereEqualTo("userID", currentUser.getObjectId());
        query.findInBackground(new FindCallback<Poll>() {

            @Override
            public void done(List<Poll> polls, com.parse.ParseException e) {
                if (polls != null) {
                    mProfileViewAdapter.clear();
                    mProfileViewAdapter.addAll(polls);
                }
            }
        });
    }
}