package ndejaco.pollgeo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ndejaco.pollgeo.Model.GroupPoll;
import ndejaco.pollgeo.Model.Poll;
import ndejaco.pollgeo.Model.PollActivity;


public class VoterViewActivity extends ListActivity {

    private static final String TAG = VoterViewActivity.class.getSimpleName();

    private VoterViewAdapter mVoterViewAdapter;
    private int voteOption;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter_view);
        ListView lv = getListView();

        // Gets intent passed to this activity
        Intent passed = getIntent();
        type = passed.getStringExtra("type");


        // Intent includes poll id of poll and option number of option we wish to return votes from
        String thePoll = (String) passed.getStringExtra("Poll");
        String optionNumber = passed.getStringExtra("option number");

        //Log.i(TAG, thePoll.getTitle());
        Log.i(TAG, optionNumber);

        // Sets empty voter view adapter but then calls updateData to query for data.
        mVoterViewAdapter = new VoterViewAdapter(this, new ArrayList<ParseUser>());
        setListAdapter(mVoterViewAdapter);
        updateData(thePoll, optionNumber);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_voter_view, menu);
        return true;
    }

    private void updateData(String thePoll, String optionNumber) {
        voteOption = Integer.parseInt(optionNumber);
        if (type.equals("local")) {
            ParseQuery<Poll> query = ParseQuery.getQuery("Poll");
            query.getInBackground(thePoll, new GetCallback<Poll>() {
                public void done(Poll object, ParseException e) {
                    if (e == null) {
                        ArrayList<ParseUser> users = (ArrayList<ParseUser>) object.getOptionVoters(voteOption);
                        mVoterViewAdapter.clear();
                        mVoterViewAdapter.addAll(users);

                        try{
                            ActionBar actionBar = getActionBar();
                            actionBar.setDisplayShowTitleEnabled(true);
                            actionBar.setTitle("Voters for " + object.getOption(voteOption));
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setHomeButtonEnabled(true);

                        }catch(NoSuchMethodError w){

                        }
                    } else {

                    }
                }
            });
        }

        else if (type.equals("group")) {
            ParseQuery<GroupPoll> query = ParseQuery.getQuery("GroupPoll");
            query.getInBackground(thePoll, new GetCallback<GroupPoll>() {
                public void done(GroupPoll object, ParseException e) {
                    if (e == null) {
                        ArrayList<ParseUser> users = (ArrayList<ParseUser>) object.getOptionVoters(voteOption);
                        mVoterViewAdapter.clear();
                        mVoterViewAdapter.addAll(users);
                        try{
                            ActionBar actionBar = getActionBar();
                            actionBar.setDisplayShowTitleEnabled(true);
                            actionBar.setTitle("Voters for " + object.getOption(voteOption));
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setHomeButtonEnabled(true);

                        }catch(NoSuchMethodError w){

                        }
                    } else {

                    }
                }
            });
        }

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
}
