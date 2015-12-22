package ndejaco.pollgeo.Model;

import android.util.Log;

import com.parse.ParseGeoPoint;

/**
 * Created by Nicholas on 10/31/2015.
 */
public class LocalPoll extends Poll {

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public void setUserId(String id){
        put("userID",id);
    }
}
