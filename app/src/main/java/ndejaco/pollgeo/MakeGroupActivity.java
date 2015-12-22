package ndejaco.pollgeo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.facebook.AccessToken;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ndejaco.pollgeo.Model.Group;
import ndejaco.pollgeo.Model.Poll;

/**
 * Created by Matthew on 10/13/2015.
 * MakeGroupActivity is responsible for making a group
 */
public class MakeGroupActivity extends Activity {

    private static final String TAG = MakeGroupActivity.class.getSimpleName();

    // set up element variables for user interactions with make_group.xml
    private TextView textView;
    private EditText groupName; // groupName will be the users choice for the group name
    private Button createGroupButton; // button when once clicked, creates a group
    private Button finishButton; // button will exit the make group activity
    private Group currentGroup; // the current group being created in this certain activity
    private ParseUser creator; // the user who is making the group
    private friendsViewAdapter friendsViewAdapter;
    private ArrayList<ParseUser> friendsList = new ArrayList<ParseUser>();

    // elements for side drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mSections;
    private PullRefreshLayout swipeLayout;
    private ProfilePictureView fbPhoto;
    private Button photoButton;
    private ParseFile photoFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_group);

        // Gets intent passed to this activity
        Intent passed = getIntent();

        // set up side drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        fbPhoto = (ProfilePictureView) findViewById(R.id.thumbnail);

        if (ParseUser.getCurrentUser() != null) {
            fbPhoto.setPresetSize(ProfilePictureView.LARGE);
            String profileId = ParseUser.getCurrentUser().getString("facebookId");
            if (profileId != null) {
                fbPhoto.setProfileId(ParseUser.getCurrentUser().getString("facebookId"));
            } else {

            }
        }
        // set up the drawer's list view with items and click listener
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerList.setAdapter(new DrawerAdapter(this, mSections));

        // Gets current user
        creator = ParseUser.getCurrentUser();

        // textView will prompt user for group name, then when the group is created, display the name of the group
        textView = (TextView) findViewById(R.id.textView);
        //Set up textField for group name insertion
        groupName = (EditText) findViewById(R.id.groupName);

        // set listener for make group button
        createGroupButton = (Button) findViewById(R.id.createGroupButton);
        createGroupButton.setEnabled(true);
        createGroupButton.setClickable(true);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCreateGroup();
            }
        });

        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setVisibility(View.INVISIBLE);
        finishButton.setEnabled(false);
        finishButton.setClickable(false);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishButton.setEnabled(false); //dont want user to spam click the button
                finishButton.setClickable(false);
                navigateToGroupActivity();
            }
        });

        // set up side drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        fbPhoto = (ProfilePictureView) findViewById(R.id.thumbnail);
        fbPhoto.setPresetSize(ProfilePictureView.LARGE);

        if (ParseUser.getCurrentUser() != null) {
            String profileId = ParseUser.getCurrentUser().getString("facebookId");
            if (profileId != null) {
                fbPhoto.setProfileId(ParseUser.getCurrentUser().getString("facebookId"));
            } else {

            }
        }
        // set up the drawer's list view with items and click listener
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerList.setAdapter(new DrawerAdapter(this, mSections));

        photoButton = (Button) findViewById(R.id.photoButton);
        photoButton.setVisibility(View.INVISIBLE);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Pollgeo");

        // grab the users friend and display them to add
        setUpFriendsList();

    }

    /*
    setUpFriendsList() maekes a request to Facebook to grab the Parse users' friends, creating the friendsList array list
     in the process.
     */
    public void setUpFriendsList() {
        /* make the API call to get users friends list*/
        GraphRequest friendsRequest = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            try {
                                // make friendsList from the data response
                                JSONObject data = response.getJSONObject();
                                JSONArray array = data.getJSONArray("data");
                                Log.d("data", data.toString());
                                Log.d(TAG + ":array", array.toString());
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject friend = array.getJSONObject(i);
                                    // grab the facebook id, query for user and add the user to the friends list
                                    Log.d(TAG + ":id", friend.getString("id"));
                                    Log.d(TAG + ":name", friend.getString("name") + " " + i);
                                    // friendID is the facebook ID of the friend in the group
                                    String friendID = friend.getString("id");
                                    //fbIds.add(friendID);
                                    //now query based off friendID and grab the ParseUser, call query function
                                    searchForFriend(friendID);


                                } // end of for loop
                                Log.d(TAG, "IN Request call");
                                Log.d(TAG, "FriendsList array size: ---" + friendsList.size());
                                for (ParseUser pu : friendsList) {
                                    Log.d(TAG, "IN FRIENDS LIST FOR LOOP!");
                                    Log.d(TAG, "name " + pu.get("name") + " IN FRIENDS LIST");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "Problem Facebook request for friends in OnCreate()");
                            }
                        } else {
                            Log.d(TAG, "Facebook friends request could not be found");
                        }
                    }
                });
        Bundle params = new Bundle();
        params.putString("fields", "id, name");
        friendsRequest.setParameters(params);
        friendsRequest.executeAsync(); // execute the request


    }

    /*
    setupAdapter sets up the list view adapter, friendsViewAdapter for adding friends
     */
    public void setupAdapter() {
        Log.d(TAG, "In SetupAdapter, FriendsList array size: ---" + friendsList.size());
        for (ParseUser pu : friendsList) {
            Log.d(TAG, "IN FRIENDS LIST FOR LOOP!");
            Log.d(TAG, "name " + pu.get("name") + " IN FRIENDS LIST");
        }
        // Sets a friends View Adapter with friendsList
//       ListView friendsListView = (ListView) findViewById(android.R.id.list);
        friendsViewAdapter = new friendsViewAdapter(this, friendsList, currentGroup);
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(friendsViewAdapter);

    }


    /*
    navigateToGroupActivity exits the make group activity and its view and moves the user back to the main group view, GroupActivity
     */
    public void navigateToGroupActivity() {

        List<ParseUser> u = currentGroup.getMembers();
        for(ParseUser pu : u){
            try{
                pu.fetchIfNeeded();
                ParsePush push = new ParsePush();
                push.setChannel(pu.getObjectId());
                push.setMessage("You have been added to a group: " + currentGroup.getName() + "!");
                push.sendInBackground();
            }catch(Exception e){

            }
        }

        currentGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                Intent intent = new Intent(MakeGroupActivity.this, GroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

    }

    /*
    navigateToCreateCroup is called when the create group button is clicked. This method will set up creating a group
     */
    public void navigateToCreateGroup() {
        //Grab the name the user gave for the group name
        String groupNameString = groupName.getText().toString().trim();

        // If either title name is empty, pop up an alert to the user
        if (groupNameString.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MakeGroupActivity.this);
            builder.setMessage("A Group must have a name! Please enter a name for the group before creating").setTitle("Oops!").
                    setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            //exit method now and enable createGroupButton to work again
            createGroupButton.setEnabled(true); //dont want user to spam click the button
            createGroupButton.setClickable(true);
            return;
        } else { //create a group, and add the user who created the group to the group
            //create the group
            currentGroup = createGroup(groupNameString); //create the initial empty group
            addMember(creator); //first add the person who created the group to the group
            Log.d(TAG, "MAKEGROUP BUTTON FRIENDSLIST SIZE: --- " + friendsList.size());
            for (ParseUser pu : friendsList) {
                Log.d(TAG, "Adding " + pu.get("name") + " to current group");
            }
            // set up the list for adding friends
            setupAdapter();

            //set createGroupButton and groupName to GONE
            createGroupButton.setText("GROUP " + groupNameString + " CREATED");
            createGroupButton.setVisibility(View.GONE);
            groupName.setVisibility(View.GONE);
            textView.setText(groupNameString);
            textView.setAllCaps(true);
            //make finish button visible
            finishButton.setVisibility(View.VISIBLE);
            finishButton.setEnabled(true); //dont want user to spam click the button
            finishButton.setClickable(true);

            photoButton.setVisibility(View.VISIBLE);
            photoButton.setEnabled(true); //dont want user to spam click the button
            photoButton.setClickable(true);



        }

    }


    /*
        createGroup takes in a string which is to be the name of a group and list of usernames for the users to be in the
        group and creates a group, and returns that group
     */
    private Group createGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        return group;
    }


    /*
    searchForFriend takes in a Facebook string id and queries the Parse database for the user with that Facebook id
     */
    public void searchForFriend(String fbId) {

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("facebookId", fbId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {

                if (e == null) {
                    //query successful
                    if (objects.size() > 0) {
                        Log.d(TAG, "query result = " + objects);
                        Log.d(TAG, "object size = " + objects.size());
                        Log.d(TAG, "object  = " + objects.get(0));
                        ParseUser fbFriend = objects.get(0);
                        //fbFriend represents the new ParseUser being added to friendsList
                        if (fbFriend != null) {
                            // add the Facebook friend to the friendsList Array
                            Log.d(TAG, "Friend ADDED, name:" + fbFriend.get("name"));
                            friendsList.add(fbFriend);
                        }
                    }


                } else {
                    //something went wrong
                    Log.d(TAG, "Facebook friend could not be added");
                }

            }
        }); // end of query

    }


    /*
    addMember takes in ParseUser object and adds the user to the currentGroup being created
     */
    private void addMember(ParseUser user) {

        currentGroup.addMember(user);

    } //end of addMember()

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
            onBackPressed();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);

                    Bitmap selectedImageScaled = Bitmap.createScaledBitmap(selectedImage, 100, 100, false);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    selectedImageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                    byte[] scaledData = bos.toByteArray();

                    // Save the scaled image to Parse
                    photoFile = new ParseFile("group_photo.jpg", scaledData);
                    photoFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (currentGroup != null) {
                                Log.i(TAG, "uploaded photo");
                                currentGroup.addPhoto(photoFile);
                                currentGroup.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Log.i(TAG, "added Photo");
                                    }
                                });


                            }
                        }
                    });

                }
                break;

            default:
                break;

        }


    }
}

