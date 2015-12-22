package ndejaco.pollgeo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

import ndejaco.pollgeo.Model.Poll;

/**
 * Created by Zac on 10/25/2015.
 */
public class DrawerAdapter extends ArrayAdapter<String> {
    protected String mOpts[];
    protected Context mContext;

    public DrawerAdapter(Context c, String opts[]) {
        super(c, R.layout.drawer_list_item, opts);
        mOpts = opts;
        mContext = c;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        // If the view passed is null it inflates the home list item view to create a new one
        if (v == null) {
            v = View.inflate(getContext(), R.layout.drawer_list_item, null);
        }

        TextView buttonText = (TextView) v.findViewById(R.id.drawerText);
        buttonText.setText(mOpts[position]);
        buttonText.setTag(position);
        buttonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                String cmd = mOpts[pos];
                doAction(pos);
            }
        });


        return v;
    }

    private void doAction(int position) {
        switch (position) {
            case 0:
                Intent intent0 = new Intent(mContext,LocalHomeListActivity.class);
                intent0.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent0);
                break;
            case 1:
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                intent.putExtra("target", ParseUser.getCurrentUser().getUsername());
                mContext.startActivity(intent);
                break;
            case 2:
                Intent intent2 = new Intent(mContext, GroupActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent2);
                break;
            case 3:
                //Settings go here.
                Intent intent3 = new Intent(mContext, SettingsActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent3);
                break;
        }
    }
}