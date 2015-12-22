
package ndejaco.pollgeo;


import android.os.Bundle;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


import com.baoyz.widget.PullRefreshLayout;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ndejaco.pollgeo.Model.Group;
import ndejaco.pollgeo.Model.Poll;

/**
 * Created by Matthew on 10/11/2015.
 * <p/>
 * GroupActivity is the activity that will handle Group Poll functionality
 * It will display group polls are in and the option to make a group poll
 */
public class GroupActivity extends AppCompatActivity {

    // TAG used for debugging
    private static final String TAG = GroupActivity.class.getSimpleName();
    private Button create_button;
    private Button logOutButton;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mSections;
    private PullRefreshLayout swipeLayout;
    private GroupViewAdapter mGroupViewAdapter;
    private ProfilePictureView fbPhoto;
    private ListView groupList;
    private Toolbar toolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_home);



        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mGroupViewAdapter = new GroupViewAdapter(this, new ArrayList<Group>());
        groupList = (ListView) findViewById(R.id.list);
        groupList.setAdapter(mGroupViewAdapter);

        swipeLayout = (PullRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
                swipeLayout.setRefreshing(false);
            }
        });

        fbPhoto = (ProfilePictureView) findViewById(R.id.thumbnail);

        if (ParseUser.getCurrentUser() != null) {
            fbPhoto.setPresetSize(ProfilePictureView.LARGE);
            String profileId = ParseUser.getCurrentUser().getString("facebookId");
            if (profileId != null) {
                fbPhoto.setProfileId(ParseUser.getCurrentUser().getString("facebookId"));
            } else {

            }
        }

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group clicked = (Group) mGroupViewAdapter.getItem(position);
                navigateToGroupPoll(clicked);
            }
        });


        // set up the drawer's list view with items and click listener
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerList.setAdapter(new DrawerAdapter(this, mSections));

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        ActionBar actionBar = getSupportActionBar();


        try{
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Groups");
        }catch(NoSuchMethodError e){

        }

        updateData();


    }

    private void navigateToGroupPoll(Group clicked) {
        String objectId = clicked.getObjectId();
        Intent intent = new Intent(this, GroupHomeListActivity.class);

        // Send info on both the objectId and name of the group
        Bundle extras = new Bundle();
        extras.putString("Group",objectId);
        extras.putString("groupName",clicked.getName());
        intent.putExtras(extras);

        startActivity(intent);
    }

    private void updateData() {
        ParseUser current = ParseUser.getCurrentUser();
        ArrayList<ParseUser> users = new ArrayList<ParseUser>();
        users.add(current);
        ParseQuery<Group> query = new ParseQuery<Group>("Group");
        query.whereContainedIn("members", users);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> objects, ParseException e) {
                if (objects != null) {
                    mGroupViewAdapter.clear();
                    mGroupViewAdapter.addAll(objects);
                }

            }
        });
    }

    /*
    navigateToMakeGroup is called when the makeGroupButton is pressed.
    This button will take the user to the make_group.xmlut to make a group
     */
    private void navigateToMakeGroup() {
        Intent intent = new Intent(this, MakeGroupActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_make, menu);
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

        if (id == R.id.create_group) {
            navigateToMakeGroup();
        }

        return super.onOptionsItemSelected(item);
    }

}
