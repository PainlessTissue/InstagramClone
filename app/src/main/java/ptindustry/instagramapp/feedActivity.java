package ptindustry.instagramapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class feedActivity extends AppCompatActivity
{

    LinearLayout linearLayout;

    //AsyncTask is necessary for this because otherwise, inputstreams break the app.
    //The parameters for this is a string (the URL of the image being downloaded
    //nothing in the middle (idk what that even does
    //and it returns a bitmap when the image is correctly downloaded
    public class downloadUrl extends AsyncTask<String, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... strings)
        {
            try
            {
                URL url = new URL(strings[0]); //get the image url
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //open the connection of the url
                connection.connect(); //get the url
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input); //create the bitmap being returned from the inputstream

                return myBitmap;
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            super.onPostExecute(bitmap);

            ImageView img = new ImageView(getApplicationContext());

            img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            img.setPadding(0, 30, 0, 0);
            img.setImageBitmap(bitmap);

            linearLayout.addView(img);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        //getStringExtra is necessary because it gets the correct sent in intent
        //this intent is sending over the username clicked on
        String username = getIntent().getStringExtra("username");
        setTitle(username + "'s feed");

        //get the images that users have sent in
        ParseQuery<ParseObject> query = new ParseQuery<>("Image");
        query.whereEqualTo("username", username); //get this specific users images
        query.setLimit(50); //dont wanna overload the phone with a million pictures
        query.addDescendingOrder("createdAt");
/*
        LinearLayout linearLayout = new LinearLayout(this); //make a linear layout on this activity
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); //set the size
*/
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                //make sure there exists anything in their feed
                if(objects != null && objects.size() > 0 && e == null)
                {
                    for(ParseObject pics : objects) //search through 4 of their images and display them in order
                    {
                        try
                        {
                            downloadUrl bit = new downloadUrl();
                            bit.execute(pics.getParseFile("image").getUrl());
                        }

                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }

                else
                    Toast.makeText(feedActivity.this, "Users feed is empty", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
