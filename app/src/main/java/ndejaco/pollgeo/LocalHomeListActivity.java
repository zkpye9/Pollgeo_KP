package ndejaco.pollgeo;


import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;

import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;



import com.baoyz.widget.PullRefreshLayout;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ndejaco.pollgeo.Model.*;

public class LocalHomeListActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // TAG used for debugging
    private static final String TAG = LocalHomeListActivity.class.getSimpleName();
    // Button used to create new poll
    private Button create_button;
    private Button logOutButton;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private ListView mDrawerList;
    private String[] mSections;
    private ActionBarDrawerToggle mDrawerToggle;

    //Refresh layout swipe
    private PullRefreshLayout swipeLayout;
    //HomeViewAdapter responsible for setting contents of listView
    private HomeViewAdapter mHomeViewAdapter;

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location lastLocation;
    private Location currentLocation;
    private ParseUser currentUser;
    private ProfilePictureView fbPhoto;


    /*
     * Define a request code to send to Google Play services This code is returned in
     * Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);


        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        // Gets current user, if null goes to login screen, if not logs current user
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null) {
            navigateToLogin();
        } else {
            currentUser = current;
        }

        mContext = this;


        logOutButton = (Button) findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                navigateToLogin();
            }
        });

        // Sets a blank homeView Adapter with no data
        mHomeViewAdapter = new HomeViewAdapter(this, new ArrayList<Poll>());


        // Change this to be able to extend AppCompatActivity
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(mHomeViewAdapter);

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

        // set up the drawer's list view with items and click listener
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSections = getResources().getStringArray(R.array.sections_array);
        mDrawerList.setAdapter(new DrawerAdapter(this, mSections));


        // set up app toolbar and actionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();


        try{
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Local Polls");
        }catch(NoSuchMethodError e){

        }

    }


    private byte[] compressAndConvertImageToByteFrom(Bitmap imageBitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();

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
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        if(myLoc == null){
            return;
        }


        Log.i(TAG, "Starting query");
        ParseQuery<Poll> query = new ParseQuery<Poll>("Poll");
        int s = 10;
        if(currentUser.get("searchRadius") != null){
            s = (int)currentUser.get("searchRadius");
        }
        query.whereWithinMiles("location", geoPointFromLocation(myLoc), s);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Poll>() {

            @Override
            public void done(List<Poll> polls, com.parse.ParseException e) {
                if (polls != null) {
                    mHomeViewAdapter.clear();
                    mHomeViewAdapter.addAll(polls);
                }
            }
        });
    }


    // Private method moves to MakePollActivity when make poll button was clicked.
    private void navigateToMakePoll() {
        Intent intent = new Intent(this, MakePollActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc == null) {
            Toast.makeText(LocalHomeListActivity.this,
                    "Please try again after your location is turned on", Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra("type", "local");
        intent.putExtra(PollgeoApplication.INTENT_EXTRA_LOCATION, myLoc);

        startActivity(intent);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    onOptionsItemSelected handles the clickable action tabs in the action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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

    /*
   * Called when the Activity is no longer visible at all. Stop updates and disconnect.
   */
    @Override
    public void onStop() {
        // If the client is connected
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        locationClient.disconnect();

        super.onStop();
    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();

        // Connect to the location services client
        locationClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Resumed");
    }

    /*
   * Verify that Google Play services is available before making a request.
   *
   * @return true if Google Play services is available, otherwise false
   */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            if (PollgeoApplication.APPDEBUG) {
                // In debug mode, log the status
                Log.i(TAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {

            Log.i(TAG, "Google play services unavailable");
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), PollgeoApplication.APPTAG);
            }
            return false;
        }
    }

    private void stopPeriodicUpdates() {
        locationClient.disconnect();
        Log.i(TAG, "stop periodic updates");
    }

    public void onConnected(Bundle bundle) {
        if (PollgeoApplication.APPDEBUG) {
            Log.i(TAG, "Connected to location");
        }
        startPeriodicUpdates();
    }

    /*
     * Called by Location Services if the connection to the location client drops because of an error.
     */
    public void onDisconnected() {
        if (PollgeoApplication.APPDEBUG) {
            Log.i(TAG, "Disconnect location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects. If the error has a resolution, try
        // sending an Intent to start a Google Play services activity that can resolve error.
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                if (PollgeoApplication.APPDEBUG) {
                    // Thrown if Google Play services canceled the original PendingIntent
                    Log.i(TAG, "An error occurred when connecting to location services.", e);
                }
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /*
     * Report location updates to the UI.
     */
    public void onLocationChanged(Location location) {

        Log.i(TAG, "Location changed");
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInMilesTo(geoPointFromLocation(lastLocation)) < 10) {
            // If the location hasn't changed by more than 10 miles, ignore it.
            return;
        }
        lastLocation = location;
        updateData();
    }

    /*
   * Helper method to get the Parse GEO point representation of a location
   */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    /*
     * In response to a request to start updates, send a request to Location Services
     */
    private void startPeriodicUpdates() {

        Log.i(TAG, "Starting periodic updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }


    /*
     * Get the current location
     */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            Log.i(TAG, "Services not connected");
            return null;
        }
    }

    /*
   * Show a dialog returned by Google Play services for the connection error code
   */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), PollgeoApplication.APPTAG);
        }
    }

    /*
   * Define a DialogFragment to display the error dialog generated in showErrorDialog.
   */
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /*
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}