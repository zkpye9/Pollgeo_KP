package ndejaco.pollgeo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * Shows the user profile. This simple activity can function regardless of whether the user
 * is currently logged in.
 */
public class LoginActivity extends Activity {
    private static final int LOGIN_REQUEST = 0;

    private TextView titleTextView;
    private TextView emailTextView;
    private TextView nameTextView;
    private Button loginOrLogoutButton;

    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        titleTextView = (TextView) findViewById(R.id.profile_title);
        emailTextView = (TextView) findViewById(R.id.profile_email);
        nameTextView = (TextView) findViewById(R.id.profile_name);
        loginOrLogoutButton = (Button) findViewById(R.id.login_or_logout_button);
        titleTextView.setText(R.string.profile_title_logged_in);

        loginOrLogoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    // User clicked to log out.
                    ParseUser.logOut();
                    currentUser = null;
                    showProfileLoggedOut();
                } else {
                    // User clicked to log in.
                    // This example customizes ParseLoginActivity in code.
                    ParseLoginBuilder builder = new ParseLoginBuilder(
                            LoginActivity.this);
                    Intent parseLoginIntent = builder.setParseLoginEnabled(true)
                            .setParseLoginButtonText("Go")
                            .setParseSignupButtonText("Register")
                            .setParseLoginHelpText("Forgot password?")
                            .setParseLoginInvalidCredentialsToastText("You email and/or password is not correct")
                            .setParseLoginEmailAsUsername(true)
                            .setParseSignupSubmitButtonText("Submit registration")
                            .setFacebookLoginEnabled(true)
                            .setFacebookLoginButtonText("Facebook")
                            .setFacebookLoginPermissions(Arrays.asList("user_friends", "public_profile"))
                            .setTwitterLoginEnabled(false)

                            .build();
                    startActivityForResult(parseLoginIntent, LOGIN_REQUEST);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            showProfileLoggedIn();
        } else {
            showProfileLoggedOut();
        }
    }

    /**
     * Shows the profile of the given user.
     */
    private void showProfileLoggedIn() {
        getFacebookPhoto();    }

    private void getFacebookPhoto() {
        Bundle params = new Bundle();
        params.putString("fields", "id");
        GraphRequest request =
                new GraphRequest(AccessToken.getCurrentAccessToken(),
                        "me",
                        params,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    try {
                                        JSONObject data = response.getJSONObject();
                                        String facebookId = data.getString("id");
                                        ParseUser user = ParseUser.getCurrentUser();
                                        user.put("facebookId", facebookId);
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                navigateToHomeList();
                                            }
                                        });


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
        request.executeAsync();

    }

    private void navigateToHomeList() {
        Intent intent = new Intent(LoginActivity.this, LocalHomeListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    /**
     * Show a message asking the user to log in, toggle login/logout button text.
     */
    private void showProfileLoggedOut() {
        titleTextView.setText(R.string.profile_title_logged_out);
        emailTextView.setText("");
        nameTextView.setText("");
        loginOrLogoutButton.setText(R.string.profile_login_button_label);
    }
}