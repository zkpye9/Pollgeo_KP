package ndejaco.pollgeo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ndejaco.pollgeo.Model.Group;
import ndejaco.pollgeo.Model.Poll;

/**
 * Created by Nicholas on 10/31/2015.
 */
public class GroupHomeListActivity extends AppCompatActivity {

    // TAG used for debugging
    private static final String TAG = GroupHomeListActivity.class.getSimpleName();
    // Button used to create new poll
    private Button create_button;
    private Button logOutButton;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private ListView mDrawerList;
    private String[] mSections;
    private Toolbar toolbar;

    //Refresh layout swipe
    private PullRefreshLayout swipeLayout;
    //HomeViewAdapter responsible for setting contents of listView
    private HomeViewAdapter mGroupHomeViewAdapter;


    private ParseUser currentUser;
    private ProfilePictureView fbPhoto;
    private String objectId; // id of group
    private String groupName;  // name of group



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        objectId = extras.getString("Group");
        groupName = extras.getString("groupName");


        // Gets current user, if null goes to login screen, if not logs current user
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null) {
            navigateToLogin();
        } else {
            currentUser = current;
        }


        // Button listener will navigate to screen where user can make poll

        // Sets a blank homeView Adapter with no data
        mGroupHomeViewAdapter = new HomeViewAdapter(this, new ArrayList<Poll>());
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(mGroupHomeViewAdapter);

        // Set up the PullRefreshLayout and add the listener.
        swipeLayout = (PullRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
                swipeLayout.setRefreshing(false);
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        fbPhoto = (ProfilePictureView) findViewById(R.id.thumbnail);

        if (currentUser != null) {
            fbPhoto.setPresetSize(ProfilePictureView.LARGE);
            String profileId = currentUser.getString("facebookId");
            if (profileId != null) {
                fbPhoto.setProfileId(currentUser.getString("facebookId"));
            } else {

            }
        }


        Log.d(TAG, "groupNAME DOE: " + groupName);

        // set up the drawer's list view with items and click listener
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerList.setAdapter(new DrawerAdapter(this, mSections));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // just in case the groupName may be null, which shouldnt ever happen though
        if (groupName == null){
            groupName = "Group";
        }

        try{
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(groupName);// set the title to the group name
        }catch(NoSuchMethodError e){

        }


        updateData();

    }

    // Private method will navigate to login screen if current user is null
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Updates the data and sets HomeViewAdapter as data
    public void updateData() {

        // Queries poll data and orders by most recent polls. Finds in background.
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()){
            create_button.setText("No Internet Connection Detected :(");
        }


        Log.i(TAG, "Starting query");
        ParseQuery<Poll> query = new ParseQuery<Poll>("GroupPoll");
        query.orderByDescending("createdAt");
        query.whereEqualTo("group", objectId);
        query.findInBackground(new FindCallback<Poll>() {

            @Override
            public void done(List<Poll> polls, com.parse.ParseException e) {
                if (polls != null) {
                    mGroupHomeViewAdapter.clear();
                    mGroupHomeViewAdapter.addAll(polls);
                }
            }
        });
    }


    // Private method moves to MakePollActivity when make poll button was clicked.
    private void navigateToMakePoll() {
        Intent intent = new Intent(this, MakePollActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", "group");
        intent.putExtra("groupId", objectId);
        startActivity(intent);
    }


    private void setGroupName(String name){
        groupName = name;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Handle action bar item clicks here.
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Log.i(TAG, "Settings clicked");
                return true;
            case R.id.create_poll:
                // User chose the "create_poll" action, take the user to create poll
                Log.i(TAG, "create_poll clicked");
                // go to MakePollActivity
                navigateToMakePoll();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
