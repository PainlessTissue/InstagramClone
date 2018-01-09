package ptindustry.instagramapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class userActivity extends AppCompatActivity
{
    //for readability purposes
    static final int OTHERUSER = 1;
    static final int CURRENTUSER = 0;

    static ArrayList<String> arrayList = new ArrayList<>();

    public void getPhoto()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //getting access to external files
        startActivityForResult(intent, 1); //1 being the request code
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) //create the menus
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.share) //if we clicked the share button
        {
            if(Build.VERSION.SDK_INT >= 23) //check the sdk
            {
                //request permission
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                else //otherwise get the photo
                    getPhoto();
            }

            else //if its less than marshmallow, just get photo
                getPhoto();
        }

        //log out user and send back to main menu
        else if(item.getItemId() == R.id.logOut)
        {
            ParseUser.getCurrentUser().logOut();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        else if(item.getItemId() == R.id.myFeed)
        {
            Intent intent = new Intent(getApplicationContext(), feedActivity.class);
            intent.putExtra("User", CURRENTUSER);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //this is getting the photo itself when confirming the permission is granted
        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getPhoto();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData(); //make an image

            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage); //make a bitmap of the image

                //this is what turns the image into something that parse can use
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 40, stream); //compress it with the new stream

                byte[] byteArray = stream.toByteArray(); //turn it into the data for parse
                ParseFile file = new ParseFile("image.png", byteArray); //and add it

                ParseObject object = new ParseObject("Image"); //create a new class in Parse
                object.put("image", file); //add the image and the object to the usernames info
                object.put("username", ParseUser.getCurrentUser().getUsername()); //assign the image to the user

                object.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if(e == null)
                            Toast.makeText(userActivity.this, "Image shared", Toast.LENGTH_SHORT).show();

                        else
                            Toast.makeText(userActivity.this, "Image failed to share, try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            catch (Exception e) { e.printStackTrace(); }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ListView listView = (ListView) findViewById(R.id.listView);


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
        listView.setAdapter(arrayAdapter);

        //to access user objects, you use ParseUser.getQuery() instead of objects
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());

        arrayList.clear(); //makes sure the list doesnt concatinate

        query.findInBackground(new FindCallback<ParseUser>()
        {
            @Override
            public void done(List<ParseUser> objects, ParseException e)
            {
                if(e == null || objects.size() > 0)
                {
                    for (ParseUser obj : objects)
                        arrayList.add(obj.getUsername());
                }

                else
                    Log.i("Error", "No users in database");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), feedActivity.class);
                intent.putExtra("username", arrayList.get(i));
                intent.putExtra("User", OTHERUSER); //to the intent that we are viewing a different user
                startActivity(intent);
            }
        });
    }
}
