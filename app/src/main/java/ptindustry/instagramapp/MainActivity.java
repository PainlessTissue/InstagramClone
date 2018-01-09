package ptindustry.instagramapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener
{
    Button signUpButton;
    EditText username;
    EditText password;
    TextView changeLoginSignin;
    RelativeLayout backgroundLayout;
    ImageView instagramImageview;

    Boolean signupBool = true;

    public void signUpUser(View view)
    {

        //if either text field is empty
        if (username.getText().toString().equals("") || password.getText().toString().equals(""))
            Toast.makeText(this, "You need to enter a username and password", Toast.LENGTH_SHORT).show();

        else //add user
        {
            final Intent intent = new Intent(this, userActivity.class);

            if (signupBool) // is set to signup
            {
                ParseUser newUser = new ParseUser();

                newUser.setUsername(username.getText().toString());
                newUser.setPassword(password.getText().toString());

                newUser.signUpInBackground(new SignUpCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if (e == null)
                            startActivity(intent); //if successful signin, take to next activity
                        else
                            Toast.makeText(MainActivity.this, "Username already taken, try another", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            else // is set to login
            {
                ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback()
                {
                    @Override
                    public void done(ParseUser user, ParseException e)
                    {
                        if(e == null)
                            startActivity(intent); //if successful login, take to next activity

                        else
                            Toast.makeText(MainActivity.this, "Login unsucessful, check your username or password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent)
    {
        //i is each key being typed into the keyboard
        //so if the enter key is called, we go to signup

        //the && is so it doesnt get called twice (action up and action down)
        if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP)
            signUpUser(view);

        return false;
    }


    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.changeLoginSignin)
        {
            if (signupBool) //is set to sign up
            {
                signUpButton.setText(R.string.login);
                changeLoginSignin.setText(R.string.orSignUp);
                signupBool = false;
            }

            else //is set to login
            {
                signUpButton.setText(R.string.signUp);
                changeLoginSignin.setText(R.string.orLogin);
                signupBool = true;
            }
        }

        else if(view.getId() == R.id.backgroundLayout || view.getId() == R.id.instagramImageview)
        {
            //this is hiding the keyboard when pressed somewhere around the screen
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpButton = (Button) findViewById(R.id.signup);
        changeLoginSignin = (TextView) findViewById(R.id.changeLoginSignin);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        instagramImageview = (ImageView) findViewById(R.id.instagramImageview);
        backgroundLayout = (RelativeLayout) findViewById(R.id.backgroundLayout);

        changeLoginSignin.setOnClickListener(this);
        password.setOnKeyListener(this);
        backgroundLayout.setOnClickListener(this);
        instagramImageview.setOnClickListener(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}
