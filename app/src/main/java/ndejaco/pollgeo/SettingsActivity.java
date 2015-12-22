package ndejaco.pollgeo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SettingsActivity extends Activity {

    protected ParseUser currentUser;
    private ListView mDrawerList;
    private String[] mSections;
    private DrawerLayout mDrawerLayout;
    protected SeekBar searchRadiusBar;
    protected TextView searchRadius;
    protected TextView uName;
    protected Button submitButton;
    protected String currName;
    protected int currRadius;
    protected Context mContext;
    protected CheckBox radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        currentUser = ParseUser.getCurrentUser();

        mContext = this;
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

        searchRadiusBar = (SeekBar)findViewById(R.id.discoveryRadius);
        searchRadius = (TextView)findViewById(R.id.distance);
        uName = (TextView)findViewById(R.id.changeName);
        currentUser = ParseUser.getCurrentUser();
        try {
            currentUser.fetchIfNeeded();
        }catch(Exception e){
            return;
        }


        currName = (String)currentUser.get("name");
        uName.setText(currName);
        if(currentUser.get("searchRadius") == null){
            currRadius = 10;
            currentUser.put("searchRadius",10);
            currentUser.saveEventually();
        }
        else {
            currRadius = (int) currentUser.get("searchRadius");
        }

        searchRadius.setText(Integer.toString(currRadius));

        searchRadiusBar.setProgress(currRadius);
        searchRadiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                searchRadius.setText(Integer.toString(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final CheckBox pieCheck = (CheckBox) findViewById(R.id.pieOption);
        final CheckBox barCheck = (CheckBox) findViewById(R.id.barOption);
        pieCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barCheck.setChecked(false);
                pieCheck.setChecked(true);
                currentUser.put("chartPref", "pie");
            }
        });

        barCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barCheck.setChecked(true);
                pieCheck.setChecked(false);
                currentUser.put("chartPref", "bar");
            }
        });

        if(currentUser.get("chartPref") == null){
            currentUser.put("chartPref","pie");
            pieCheck.setChecked(true);
        }else if(currentUser.get("chartPref").equals("pie")){
            pieCheck.setChecked(true);
        }else{
            barCheck.setChecked(true);
        }


        radioButton = (CheckBox)findViewById(R.id.userNameRadio);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uName.setText("");
                if(!radioButton.isChecked()){
                    uName.setVisibility(View.INVISIBLE);
                }
                else{
                    uName.setVisibility(View.VISIBLE);
                }
            }
        });

        uName.setVisibility(View.INVISIBLE);
        uName.setText(currName);
        submitButton = (Button)findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             boolean changed = false;
             if(!uName.getText().equals("") && radioButton.isChecked()) {
                 currentUser.put("name", uName.getText().toString());
                 changed = true;
             }
             try{
                 int r = Integer.parseInt(searchRadius.getText().toString());
                 if(r != currRadius){
                     if(r == 0)
                         r = 1;
                     changed = true;
                     currentUser.put("searchRadius",r);
                 }
             }catch(Exception e) {

             }
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Intent intent0 = new Intent(mContext, LocalHomeListActivity.class);
                        intent0.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent0);
                    }
                });
            }
        });

        try{
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("App Settings");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        }catch(NoSuchMethodError e){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
}