package ndejaco.pollgeo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ndejaco.pollgeo.Model.GroupPoll;
import ndejaco.pollgeo.Model.LocalPoll;
import ndejaco.pollgeo.Model.Poll;


/**
 * Created by Nicholas on 9/28/2015.
 */


public class HomeViewAdapter extends ArrayAdapter<Poll> {

    // Private Context that will be used to do an intent
    private Context mContext;
    // Private List of polls to hold our data
    private List<Poll> mPolls;
    // Private Poll for current poll
    private Poll poll;
    private static String CHART_ID = "chart";

    private Poll votedPoll;
    private int votedOption;
    private int optionCount;
    private ImageButton shareButton;
    private ParseUser currentUser;
    private HashMap<Integer, Chart> mCharts;

    private static final String TAG = HomeViewAdapter.class.getSimpleName();


    // Sets instance variables and calls super class constructor
    public HomeViewAdapter(Context context, List<Poll> objects) {
        super(context, R.layout.home_list_item, objects);
        this.mContext = context;
        this.mPolls = objects;
        this.mCharts = new HashMap<Integer, Chart>();
        currentUser = ParseUser.getCurrentUser();
        try{
            currentUser.fetchIfNeeded();
        }catch(Exception e){

        }
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {

        // If the view passed is null it inflates the home list item view to create a new one
        if (v == null) {
            v = View.inflate(getContext(), R.layout.home_list_item, null);
        }

        // Saves current poll at the index in the List of Polls
        poll = mPolls.get(position);

        Chart chart;
        Chart pChart = (Chart) v.findViewById(R.id.pieChart);
        Chart bChart = (Chart) v.findViewById(R.id.barChart);
        try{
            if(currentUser.get("chartPref").equals("pie")){
                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> descriptions = new ArrayList<>();

                for (int i = 0; i < 4; i++) {
                    int votes = poll.getOptionCount(i);
                    if (votes != 0) {
                        entries.add(new Entry(votes, i));
                        descriptions.add(poll.getOption(i));
                    }
                }

                chart = pChart;
                bChart.setVisibility(View.INVISIBLE);
                pChart.setVisibility(View.VISIBLE);
                chart.setNoDataText("");
                PieDataSet ds = new PieDataSet(entries, "");
                int colors[] = {Color.parseColor("#6699FF"), Color.parseColor("#FF3838"),
                        Color.parseColor("#FFE354"), Color.parseColor("#33CC33")};
                ds.setColors(colors);
                PieData pd = new PieData(descriptions, ds);
                chart.setData(pd);
                chart.setDescription("");
                chart.invalidate();
            }else{
                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> descriptions = new ArrayList<>();

                for (int i = 0; i < 4; i++) {
                    int votes = poll.getOptionCount(i);
                    if(poll.getOption(i).equals("")){
                        continue;
                    }
                    entries.add(new BarEntry(votes, i));
                    descriptions.add(poll.getOption(i));
                }
                chart = bChart;
                bChart.setVisibility(View.VISIBLE);
                pChart.setVisibility(View.INVISIBLE);
                chart.setNoDataText("");
                BarDataSet ds = new BarDataSet(entries,"");
                int colors[] = {Color.parseColor("#6699FF"), Color.parseColor("#FF3838"),
                        Color.parseColor("#FFE354"), Color.parseColor("#33CC33")};
                ds.setColors(colors);
                BarData bd = new BarData(descriptions,ds);
                chart.setData(bd);
                chart.setDescription("");
                chart.invalidate();
            }
        }catch(Exception e){
            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<String> descriptions = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                int votes = poll.getOptionCount(i);
                if (votes != 0) {
                    entries.add(new Entry(votes, i));
                    descriptions.add(poll.getOption(i));
                }
            }
            Log.e("tag",e.toString());
            chart = pChart;
            bChart.setVisibility(View.INVISIBLE);
            pChart.setVisibility(View.VISIBLE);
            chart.setNoDataText("");
            PieDataSet ds = new PieDataSet(entries, "");
            int colors[] = {Color.parseColor("#6699FF"), Color.parseColor("#FF3838"),
                    Color.parseColor("#FFE354"), Color.parseColor("#33CC33")};
            ds.setColors(colors);
            PieData pd = new PieData(descriptions, ds);
            chart.setData(pd);
            chart.setDescription("");
            chart.invalidate();
        }

        mCharts.put(position, chart);

        optionCount = poll.getOptions();
        Log.d(TAG, "option count: " + optionCount);

        ImageButton deleteButton = (ImageButton) v.findViewById(R.id.pollDelete);
        if (poll.getUser() != ParseUser.getCurrentUser()) {
            deleteButton.setVisibility(View.GONE);
        }

        else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setTag(position);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    String pollID = null;
                    try{
                        mPolls.get(pos).fetchIfNeeded();
                        pollID = (String)mPolls.get(pos).get("objectId");
                        ParsePush.unsubscribeInBackground(pollID);
                    }
                    catch(Exception e){

                    }
                    mPolls.get(pos).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("Poll deleted").
                                    setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            notifyDataSetChanged();
                        }
                    });

                }
            });
        }

        // Creates buttons and textviews
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView option1 = (TextView) v.findViewById(R.id.option1);
        TextView option2 = (TextView) v.findViewById(R.id.option2);
        TextView option3 = (TextView) v.findViewById(R.id.option3);
        TextView option4 = (TextView) v.findViewById(R.id.option4);

        ImageButton option1button = (ImageButton) v.findViewById(R.id.option1button);
        ImageButton option2button = (ImageButton) v.findViewById(R.id.option2button);
        ImageButton option3button = (ImageButton) v.findViewById(R.id.option3button);
        ImageButton option4button = (ImageButton) v.findViewById(R.id.option4button);

        option1button.setEnabled(true);
        option2button.setEnabled(true);
        option3button.setEnabled(true);
        option4button.setEnabled(true);

        //Sets title of poll to current polls title *************************************************************
        title.setText((String) poll.getTitle());
        title.setGravity(Gravity.CENTER);
        title.setTag(position);
        ParseUser pu = poll.getUser();
        title.setOnClickListener(new TitleClickListener(pu, mContext));
        String titleText = null;
        try {
            pu.fetchIfNeeded();
            titleText = title.getText() + "\nby " + poll.getUser().getString("name");
        } catch (Exception e) {
        }

        if (titleText != null) {
            SpannableString ss = new SpannableString(titleText);
            ss.setSpan(new MyClickableSpan(titleText), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            title.setText(ss);
            title.setTextColor(Color.parseColor("#FFDE5842"));
        }


        // Sets option texts
        option1.setText((String) poll.getOption(0));
        option2.setText((String) poll.getOption(1));
        option3.setText((String) poll.getOption(2));
        option4.setText((String) poll.getOption(3));

        TextView votes1 = (TextView) v.findViewById(R.id.votes1);
        TextView votes2 = (TextView) v.findViewById(R.id.votes2);
        TextView votes3 = (TextView) v.findViewById(R.id.votes3);
        TextView votes4 = (TextView) v.findViewById(R.id.votes4);

        // Sets votes texts
        votes1.setText((String) (poll.getOptionCount(0) + ""));
        votes2.setText((String) (poll.getOptionCount(1) + ""));
        votes3.setText((String) (poll.getOptionCount(2) + ""));
        votes4.setText((String) (poll.getOptionCount(3) + ""));

        // make some elements invisible depending on the option count
        if (optionCount == 3){
            //set all elements corresponding to option 4 invisible/gone
            votes1.setVisibility(View.VISIBLE);
            option1.setVisibility(View.VISIBLE);
            option1button.setVisibility(View.VISIBLE);

            votes2.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option2button.setVisibility(View.VISIBLE);

            votes3.setVisibility(View.VISIBLE);
            option3.setVisibility(View.VISIBLE);
            option3button.setVisibility(View.VISIBLE);

            votes4.setVisibility(View.GONE);
            option4.setVisibility(View.GONE);
            option4button.setVisibility(View.GONE);
        }
        else if (optionCount == 2){
            //set all elements corresponding to option 3 and 4 invisible/gone
            votes1.setVisibility(View.VISIBLE);
            option1.setVisibility(View.VISIBLE);
            option1button.setVisibility(View.VISIBLE);

            votes2.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option2button.setVisibility(View.VISIBLE);

            votes4.setVisibility(View.GONE);
            option4.setVisibility(View.GONE);
            option4button.setVisibility(View.GONE);

            votes3.setVisibility(View.GONE);
            option3.setVisibility(View.GONE);
            option3button.setVisibility(View.GONE);
        }
        else {
            votes1.setVisibility(View.VISIBLE);
            option1.setVisibility(View.VISIBLE);
            option1button.setVisibility(View.VISIBLE);

            votes2.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option2button.setVisibility(View.VISIBLE);

            votes3.setVisibility(View.VISIBLE);
            option3.setVisibility(View.VISIBLE);
            option3button.setVisibility(View.VISIBLE);

            votes4.setVisibility(View.VISIBLE);
            option4.setVisibility(View.VISIBLE);
            option4button.setVisibility(View.VISIBLE);
        }

        boolean buttonSet = false;
        if (!buttonSet && poll.getList("option" + 0 + "count") != null) {
            for (int j = 0; j < poll.getList("option" + 0 + "count").size(); j++) {
                if (ParseUser.getCurrentUser() == (ParseUser) poll.getList("option" + 0 + "count").get(j)) {
                    option1button.setBackgroundResource(R.drawable.voted_star);
                    option2button.setBackgroundResource(R.drawable.circle_blank);
                    option3button.setBackgroundResource(R.drawable.circle_blank);
                    option4button.setBackgroundResource(R.drawable.circle_blank);
                    buttonSet = true;
                }
            }
        }

        if(!buttonSet && poll.getList("option" + 1 + "count")!= null) {
            for (int j = 0; j < poll.getList("option" + 1 + "count").size(); j++) {
                if (ParseUser.getCurrentUser() == (ParseUser) poll.getList("option" + 1 + "count").get(j)) {
                    option2button.setBackgroundResource(R.drawable.voted_star);
                    option1button.setBackgroundResource(R.drawable.circle_blank);
                    option3button.setBackgroundResource(R.drawable.circle_blank);
                    option4button.setBackgroundResource(R.drawable.circle_blank);
                    buttonSet = true;
                }
            }
        }

        if(!buttonSet && poll.getList("option" + 2 + "count")!= null) {
            for (int j = 0; j < poll.getList("option" + 2 + "count").size(); j++) {
                if (ParseUser.getCurrentUser() == (ParseUser) poll.getList("option" + 2 + "count").get(j)) {
                    option3button.setBackgroundResource(R.drawable.voted_star);
                    option1button.setBackgroundResource(R.drawable.circle_blank);
                    option2button.setBackgroundResource(R.drawable.circle_blank);
                    option4button.setBackgroundResource(R.drawable.circle_blank);
                    buttonSet = true;
                }
            }
        }

        if(!buttonSet && poll.getList("option" + 3 + "count")!= null) {
            for (int j = 0; j < poll.getList("option" + 3 + "count").size(); j++) {
                if (ParseUser.getCurrentUser() == (ParseUser) poll.getList("option" + 3 + "count").get(j)) {
                    option4button.setBackgroundResource(R.drawable.voted_star);
                    option1button.setBackgroundResource(R.drawable.circle_blank);
                    option2button.setBackgroundResource(R.drawable.circle_blank);
                    option3button.setBackgroundResource(R.drawable.circle_blank);
                    buttonSet = true;
                }
            }
        }
        
        if (!buttonSet) {
            option4button.setBackgroundResource(R.drawable.circle_blank);
            option1button.setBackgroundResource(R.drawable.circle_blank);
            option2button.setBackgroundResource(R.drawable.circle_blank);
            option3button.setBackgroundResource(R.drawable.circle_blank);
        }

        // Sets option1 button tag to store its position in mPolls.
        // Then on click adds vote to correct poll and correct option
        // Also, adds vote activity to link current user with option with poll
        option1button.setTag(position);
        option1button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                addVote(mPolls.get(position), 0);
            }
        });

        // Sets option2 button tag to store its position in mPolls.
        // Then on click adds vote to correct poll and correct option
        // Also, adds vote activity to link current user with option with poll

        option2button.setTag(position);
        option2button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                addVote(mPolls.get(position), 1);
            }
        });

        // Sets option3 button tag to store its position in mPolls.
        // Then on click adds vote to correct poll and correct option
        // Also, adds vote activity to link current user with option with poll

        option3button.setTag(position);
        option3button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                addVote(mPolls.get(position), 2);
            }
        });

        // Sets option4 button tag to store its position in mPolls.
        // Then on click adds vote to correct poll and correct option
        // Also, adds vote activity to link current user with option with poll

        option4button.setTag(position);
        option4button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                addVote(mPolls.get(position), 3);
            }
        });



        // Sets vote1 tag position in order to get correct poll onClick
        // On click navigates to voter view to show which users voted on that option in this poll
        votes1.setTag(position);
        votes1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                navigateToVoterView(mPolls.get(position), "0");
            }
        });

        // Sets vote2 tag position in order to get correct poll onClick
        // On click navigates to voter view to show which users voted on that option in this poll
        votes2.setTag(position);
        votes2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                navigateToVoterView(mPolls.get(position), "1");
            }
        });

        // Sets votes3 tag position in order to get correct poll onClick
        // On click navigates to voter view to show which users voted on that option in this poll

        votes3.setTag(position);
        votes3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                navigateToVoterView(mPolls.get(position), "2");
            }
        });


        // Sets vote4 tag position in order to get correct poll onClick
        // On click navigates to voter view to show which users voted on that option in this poll
        votes4.setTag(position);
        votes4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                navigateToVoterView(mPolls.get(position), "3");
            }
        });

        shareButton = (ImageButton) v.findViewById(R.id.fb_share_button);
        shareButton.setTag(position);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap chartImage = mCharts.get((Integer) v.getTag()).getChartBitmap();

                if (chartImage != null) {
                    ShareDialog shareDialog = new ShareDialog((Activity) mContext);
                    SharePhoto photo = new SharePhoto.Builder().setBitmap(chartImage).build();
                    SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
                    shareDialog.show(content);
                }
            }
        });

        //returns view
        return v;

    }

    private void navigateToVoterView(Poll aPoll, String optionNumber) {

        if (aPoll instanceof LocalPoll) {
            Intent intent = new Intent(mContext, VoterViewActivity.class);
            intent.putExtra("Poll", aPoll.getObjectId());
            intent.putExtra("option number", optionNumber);
            intent.putExtra("type", "local");
            mContext.startActivity(intent);
        }

        else if (aPoll instanceof GroupPoll) {
            Intent intent = new Intent(mContext, VoterViewActivity.class);
            intent.putExtra("Poll", aPoll.getObjectId());
            intent.putExtra("option number", optionNumber);
            intent.putExtra("type", "group");
            mContext.startActivity(intent);
        }

        else {

        }
    }


    /*
    addVote takes in the poll which is being voted and i, which represents the option which was chosen.
    The option that was voted on gets incremented and if the user has already voted on the poll, any of their previous votes
    are deleted and decremented from their respective option count
     */
    private void addVote(Poll thePoll, int i) {
        //Check to see if the current user has already voted on this poll
        votedPoll = thePoll;
        votedOption = i;

        //determinePositionVoted does our null checking.
        //int pastOption = determinePositionVoted(activity);
        boolean flag = removePreviousVotes(votedPoll, votedPoll.getTotalOptions(), votedOption);
        if (flag) {
            votedPoll.setOptionCount(votedOption, ParseUser.getCurrentUser());
        }

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ;
            }
        });

        votedPoll.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                updateData();
            }
        });

    }

    private boolean removePreviousVotes(Poll votedPoll, int optionCount, int ignore) {
        boolean found = false;
        for (int i = 0; i < optionCount; i++) { // loop through to see if any option contains current user
            if (votedPoll.getOptionVoters(i) != null) { // if the list of users who voted on the particular option is not null
                if (votedPoll.getOptionVoters(i).contains(ParseUser.getCurrentUser())) {
                    if (i == ignore) {
                        return false;
                    } else {
                        found = true;
                        votedPoll.removeUser(i, ParseUser.getCurrentUser());
                        Log.d(TAG,  "i == " + i + ", and user: " + ParseUser.getCurrentUser());
                    }
                }
            }
        }

        if(!found){
            ParseUser.getCurrentUser().increment("score");
            ParseUser pu = poll.getUser();
            try{
                pu.fetchIfNeeded();
                pu.increment("score");
                pu.saveEventually();
            }catch(Exception e){
                return true;
            }

            try{
                votedPoll.fetchIfNeeded();
                ParsePush push = new ParsePush();
                push.setChannel(votedPoll.getObjectId());
                push.setMessage("Users have voted on your poll: " + votedPoll.getTitle() + "!");
                push.sendInBackground();
            }catch(Exception e2){
                Log.e("Here","--> error happened during push");
            }
        }

        return true;
    }

    private void updateData() {
        currentUser = ParseUser.getCurrentUser();
        try{
            currentUser.fetchIfNeeded();
        }catch(Exception e){

        }
        notifyDataSetChanged();
    }
}

class TitleClickListener implements View.OnClickListener{
    private ParseUser curr;
    private Context c;
    public TitleClickListener(ParseUser pu,Context c){
        curr = pu;
        this.c = c;
    }

    @Override
    public void onClick(View v) {
        try {
            curr.fetchIfNeeded();
            Intent intent = new Intent(c, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle b = new Bundle();
            intent.putExtra("target", curr.getUsername());
            c.startActivity(intent);
        }catch(Exception e){
            return;
        }
    }
}

/*
This class is just to get rid of the underlined title for a poll

Matt: 11/24/15
 */
class MyClickableSpan extends ClickableSpan {// extend ClickableSpan

    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
    }

    String clicked;

    public MyClickableSpan(String string) {
        super();
        clicked = string;
    }

    public void onClick(View tv) {
        return;
    }

}
