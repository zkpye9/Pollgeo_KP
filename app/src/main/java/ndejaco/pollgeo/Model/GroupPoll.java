package ndejaco.pollgeo.Model;


import com.parse.ParseClassName;

/**
 * Created by Nicholas on 10/31/2015.
 */

@ParseClassName("GroupPoll")
public class GroupPoll extends Poll {

    public GroupPoll() {
        super();
    }

    public void setGroup(String groupId) {
        put("group", groupId);
    }

    public String getGroup() {
        return getString("group");
    }

}
