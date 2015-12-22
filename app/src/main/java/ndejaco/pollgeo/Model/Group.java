package ndejaco.pollgeo.Model;

//import java.text.ParseException;
import java.util.ArrayList;
import com.parse.ParseException;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 10/12/2015.
 * This class represents a Group object.
 * A group object can have multiple viewers who are able to see and vote on the poll
 * A group poll can only be seen in The Group view, only visible to those invited or apart
 * of the group
 */

@ParseClassName("Group")
public class Group extends ParseObject {



    // set the title name of the group
    public void setName(String title) {
        put("GroupName", title);
    }

    // return the title name of the group
    public String getName() {
        return getString("GroupName");
    }

    // addMember adds a user to the group and increments the memberCount
    public void addMember(ParseUser newMember) {
        add("members", newMember);
    }

    // getMembers returns a list of parse users who are members of the group
    public List<ParseUser> getMembers() {
        return getList("members");
    }


    /*
    removeMember removes a user from the group
     */
    public void removeMember(ParseUser user) {
        List<ParseUser> toRemove = new ArrayList<ParseUser>();
        toRemove.add(user);
        removeAll("members", toRemove);
    }

    public void addPhoto(ParseFile photo) {
        put("photo", photo);
    }

    public ParseFile getPhoto() {
        return getParseFile("photo");
    }


}
