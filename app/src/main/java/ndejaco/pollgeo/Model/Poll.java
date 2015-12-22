package ndejaco.pollgeo.Model;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nicholas on 9/20/2015.
 */

@ParseClassName("Poll")
public class Poll extends ParseObject {

    public Poll() {

    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public String getObjID() {
        return (String) get("userID");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public void setOptions(ArrayList<String> options) {
        for (int i = 0; i < options.size(); i++) {
            put("option" + i, options.get(i));
        }
    }

    /* setOptionCount, takes in an int representing which option is being voted on and a value by how much to increment the vote count
      value will always be 1 or -1 for we only want to increment or decrement by one
    */
    public void setOptionCount(int opt, ParseUser pu) {
        add("option" + opt + "count", pu);
    }

    public int getOptionCount(int opt) {
        if (getList("option" + opt + "count") != null)
            return getList("option" + opt + "count").size();
        else
            return 0;
    }

    // setOptionCount sets the options that the user gave in the poll, can range between 2 - 4
    public void setOptionCount(int count) {
        put("optionCount", count);
    }

    // getOptions returns the number of options a poll has to vote on
    public int getOptions() {
        return getInt("optionCount");
    }

    public void setTotalOptions(int opt) {
        put("totalOptions", opt);
    }

    public int getTotalOptions() {
        return getInt("totalOptions");
    }

    public List<ParseUser> getOptionVoters(int opt) {
        return getList("option" + opt + "count");
    }

    public void removeUser(int opt, ParseUser Pu) {
        List<ParseUser> toRemove = new ArrayList<ParseUser>();
        toRemove.add(Pu);

        if (getOptionVoters(opt).contains(Pu)) {
            removeAll("option" + opt + "count", toRemove);
        }
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getTitle() {
        return getString("title");
    }

    public String getOption(int opt) {
        return getString("option" + opt);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile file) {
        put("image", file);
    }


}
