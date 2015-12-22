package ndejaco.pollgeo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import ndejaco.pollgeo.Model.GroupPoll;
import ndejaco.pollgeo.Model.LocalPoll;
import ndejaco.pollgeo.Model.Poll;

public class MakePollActivity extends Activity {

    private EditText title;
    private EditText option1;
    private EditText option2;
    private EditText option3;
    private EditText option4;
    private Button submit;
    private Poll currentPoll;
    private ParseGeoPoint geoPoint;
    private String type;
    private String objectId;
    private int optionCount;

    private TextView option1Count;
    private TextView option2Count;
    private TextView option3Count;
    private TextView option4Count;
    private TextView titleCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_poll);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }

        //Logs current user
        Log.i(MakePollActivity.class.getSimpleName(), currentUser.getUsername());

        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if (type.equals("local")) {
            Location location = intent.getParcelableExtra(PollgeoApplication.INTENT_EXTRA_LOCATION);
            geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        } else if (type.equals("group")) {
            objectId = intent.getStringExtra("groupId");
        }

        // Instance variables for title, options, and submit button
        title = (EditText) findViewById(R.id.userTitleText);
        option1 = (EditText) findViewById(R.id.option1);
        option2 = (EditText) findViewById(R.id.option2);
        option3 = (EditText) findViewById(R.id.option3);
        option4 = (EditText) findViewById(R.id.option4);
        submit = (Button) findViewById(R.id.submit);

        // set up option character count listeners
        option1Count = (TextView) findViewById(R.id.charCount1);
        option2Count = (TextView) findViewById(R.id.charCount2);
        option3Count = (TextView) findViewById(R.id.charCount3);
        option4Count = (TextView) findViewById(R.id.charCount4);
        titleCount = (TextView) findViewById(R.id.titleCount);

        option1.addTextChangedListener(mTextEditorWatcher1);
        option2.addTextChangedListener(mTextEditorWatcher2);
        option3.addTextChangedListener(mTextEditorWatcher3);
        option4.addTextChangedListener(mTextEditorWatcher4);
        title.addTextChangedListener(mTextEditorWatcherTitle);


        // On click of submit gets title and option strings.
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleString = title.getText().toString().trim();
                String o1 = option1.getText().toString().trim();
                String o2 = option2.getText().toString().trim();
                String o3 = option3.getText().toString().trim();
                String o4 = option4.getText().toString().trim();

                // R.string.make_poll_error_msg

                // If either is title, option1, or option2 is empty. Uses alert dialog
                if (titleString.isEmpty() || o1.isEmpty() || o2.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MakePollActivity.this);
                    builder.setMessage("Poll must at least have a title and the first two options filled out").setTitle(R.string.make_poll_error_title).
                            setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {

                    //check the poll's options that are filled out and set the correct optionCount
                    if (!o1.isEmpty() && !o2.isEmpty() && !o3.isEmpty() && !o4.isEmpty()){
                        optionCount = 4;
                    }
                    if (o3.isEmpty() && o4.isEmpty()){
                        optionCount = 2;
                    }
                    if (!o3.isEmpty() && o4.isEmpty()) {
                        optionCount = 3;
                    }
                    if (o3.isEmpty() && !(o4.isEmpty())){
                        optionCount = 3;
                        //set o3 == o4 to help make things easier, there will only be 3 options in the poll and
                        // since the user left option3 empty, switch option4's content into option3
                        Log.i(MakePollActivity.class.getSimpleName(), "option count set TO: " + optionCount);
                        o3 = o4;
                    }
                    Log.i(MakePollActivity.class.getSimpleName(), "option count set TO: " + optionCount);
                    // Adds options to an array list.
                    ArrayList<String> options = new ArrayList<String>();
                    options.add(o1);
                    options.add(o2);
                    options.add(o3);
                    options.add(o4);

                    // Creates the poll by calling create poll, passing title, and options arrayList
                    currentPoll = createPoll(titleString, options);

                    // Saves the current poll, and then if successful moves back to HomeListActivity
                    // If not successful sends alert dialog error message.
                    currentPoll.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseUser.getCurrentUser().increment("score", 10);
                                ParseUser.getCurrentUser().saveEventually();
                                try{
                                    currentPoll.fetchIfNeeded();
                                    ParsePush.subscribeInBackground(currentPoll.getObjectId());
                                }catch(Exception e2) {
                                    ;
                                }
                                if (type.equals("local")) {
                                    Intent intent = new Intent(MakePollActivity.this, LocalHomeListActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }

                                else if (type.equals("group")) {
                                    Intent intent = new Intent(MakePollActivity.this, GroupHomeListActivity.class);
                                    intent.putExtra("Group", objectId);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MakePollActivity.this);
                                builder.setMessage(e.getMessage()).setTitle(R.string.make_poll_error_title).
                                        setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Create Your Poll");

    }

    // Private method to create a poll object.
    private Poll createPoll(String title, ArrayList<String> options) {

        if (type.equals("local")) {
            LocalPoll currentPoll = new LocalPoll();
            currentPoll.setOptionCount(optionCount);
            currentPoll.setOptions(options);
            currentPoll.setUser(ParseUser.getCurrentUser());
            currentPoll.setTitle(title);
            currentPoll.setLocation(geoPoint);
            currentPoll.setTotalOptions(options.size());
            currentPoll.setUserId(ParseUser.getCurrentUser().getObjectId());
            return currentPoll;
        }

        else if (type.equals("group")) {
            GroupPoll currentPoll = new GroupPoll();
            currentPoll.setOptions(options);
            currentPoll.setOptionCount(optionCount);
            currentPoll.setUser(ParseUser.getCurrentUser());
            currentPoll.setTitle(title);
            currentPoll.setGroup(objectId);
            currentPoll.setTotalOptions(options.size());
            return currentPoll;
        }

        return null;

    }

    // Private method to navigate to loginActivity if current user is null.
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_poll, menu);
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

        return super.onOptionsItemSelected(item);
    }

    /*
    mTextEditorWatcher# is to keep a character count on the EditText option elements
     */
    private final TextWatcher mTextEditorWatcher1 = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            option1Count.setText(String.valueOf(s.length()));
        }
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mTextEditorWatcher2 = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            option2Count.setText(String.valueOf(s.length()));
        }
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mTextEditorWatcher3 = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            option3Count.setText(String.valueOf(s.length()));
        }
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mTextEditorWatcher4 = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            option4Count.setText(String.valueOf(s.length()));
        }
        public void afterTextChanged(Editable s) {
        }
    };
    private final TextWatcher mTextEditorWatcherTitle = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            titleCount.setText(String.valueOf(s.length()));
        }
        public void afterTextChanged(Editable s) {
        }
    };
}
