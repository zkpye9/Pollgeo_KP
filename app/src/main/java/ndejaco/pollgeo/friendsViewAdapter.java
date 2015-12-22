package ndejaco.pollgeo;

import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.TextView;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ArrayAdapter;


import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ndejaco.pollgeo.Model.Group;

/**
 * Created by Matthew on 10/25/2015.
 * used in MakeGroupActivity for bringing up the friends list of user
 */
public class friendsViewAdapter extends ArrayAdapter<ParseUser> {

    // Private context variable
    private Context mContext;

    // Private List of users, which are fb friends to the user
    private List<ParseUser> friends;

    // variable for the current group being created, where the friends will be added and removed from
    private Group currGroup;

    private static final String TAG = friendsViewAdapter.class.getSimpleName();

    // myViews will hold the views created so we can more quickly access them after they have been created
    private Map<Integer, View> myViews = new HashMap<Integer, View>();

    // Sets instance variables and calls super class constructor
    public friendsViewAdapter(Context context, List<ParseUser> users, Group group) {
        super(context, R.layout.friends_list_item, users);
        this.mContext = context;
        this.friends = users;
        for(ParseUser pu: friends) {
            Log.d(TAG, "Adding " + pu.get("name") + " In ADAPTER");
        }
        this.currGroup = group;
    }


    /* getView will create the individual row view for each friend and store the view in a Hash Map
        where the friends views can be reaccessed through the Map by calling on their row position
    */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder(); // set up a ViewHolder object
        View v = myViews.get(position); // get the view from the Map myViews

        // If the view passed is null it inflates the home list item view to create a new one, and store it in the Map myViews
        if (v == null) {
            v = View.inflate(getContext(), R.layout.friends_list_item, null);
            // set up viewHolder
            Log.d(TAG, "GROUP NAME: " + currGroup.getName());

            // ParseUser variable for current friend
            ParseUser friend;

            // set up elements from layout and attach to viewHolder object
            viewHolder.friendName = (TextView) v.findViewById(R.id.friendName);
            viewHolder.fbPhoto = (ProfilePictureView) v.findViewById(R.id.fbPhoto);
            viewHolder.fbPhoto.setPresetSize(ProfilePictureView.SMALL);

            //set up check box
            viewHolder.checkBox = (CheckBox) v.findViewById(R.id.checkBox);

            // make a final variable for it so we can access it in the OnClickListener()
            final CheckBox checkBox = (CheckBox) viewHolder.checkBox;

            // make sure checkBox is clickable
            checkBox.setEnabled(true);
            checkBox.setClickable(true);


            try {
                // get the ParseUser of this frined by their position in the friends List
                friend = friends.get(position).fetchIfNeeded();
                if (friend != null) {
                    Log.d(TAG, "ADAPTER, friend name: " + friend.getString("name"));
                    if (viewHolder.friendName != null) {
                        viewHolder.friendName.setText((String) friend.getString("name")); // set the name of the friend in the view
                        Log.d(TAG, friend.getString("name") + " is at the position " + position);
                    }
                    String profileId = friend.getString("facebookId");
                    if (profileId != null && viewHolder.fbPhoto != null) { // set the picture of the friend in the view
                        viewHolder.fbPhoto.setProfileId((String) friend.getString("facebookId"));
                    }
                    // set up the checkBox tag position
                    viewHolder.checkBox.setTag(position);
                    // set up the OnClickListener, where if the user checks the CheckBox, that specific friend is added, and if they
                    // uncheck it or dont touch it at all, that friend will not be in the group
                    viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //final int position = getListView().getPositionForView(v);
                            int position = (Integer) v.getTag(); // get where the friend is
                            ParseUser user = friends.get(position); // friend being added

                            // check if button has been checked or not
                            boolean checked = checkBox.isChecked();
                            Log.d(TAG, "checked: " + checked);
                            if (checked) {
                                // add the member to the group
                                insertMember(user); // insert friend to group
                                // let user know friend has been added by changing color of checkbox
                                checkBox.setHighlightColor(Color.GREEN);

                                // let the user know of change by text
                                checkBox.setText(user.getString("name") + " added (uncheck to take out of group)");
                                Log.d(TAG, position + " is the position");
                            } else {
                                //friend has been added to group, now remove friend from group
                                Log.d(TAG, user.getString("name") + " removed from Group " + currGroup.getName());
                                checkBox.setText("Add " + user.getString("name"));
                                checkBox.setHighlightColor(Color.BLACK);
                                // need to remove the user from the group in parse database
                                removeMember(user);

                            }


                        }
                    });
                    myViews.put(position,v); // put the newly created view in the Map myViews so we dont have to create it again

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
        return v; //return the view
    }


    /*
    insertMember adds a ParseUser to the group, taking in a ParseUser and adding it to the private class variable currGroup
     */
    public void insertMember(ParseUser user) {
        Log.d(TAG, user.getString("name") + " added to Group");
        Log.d(TAG, currGroup.getName() + " is the Group");
        currGroup.addMember(user);
        currGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
            }
        });

    }

    /*
    removeMember takes in a ParseUser and removes the ParseUser from the private Group variable currGroup.
     */
    public void removeMember(ParseUser user) {
        currGroup.removeMember(user);
        currGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
            }
        });

    }

    /*
    ViewHolder class, helps maintain list items that are not visible are being functioned on by a different items
    position
     */
    public class ViewHolder  {
        public TextView friendName;
        public ProfilePictureView fbPhoto;
        public CheckBox checkBox;
    }

}


