package com.example.yogi.i_fb_login;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.service.textservice.SpellCheckerService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Profile_Activity extends AppCompatActivity {

    private Session session = null;
    private String mail_content = null;
    private static final String mail_recipient = "kg@wa-tt.com";
    //private static final String mail_recipient = "singhyogendra200@gmail.com";

    private AccessToken token;
    private ImageView profile_pic;
    private TextView profile_name , profile_location , profile_email;
    private CardView fb_logout_card;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference currentUserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //firebase database
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference =mDatabase.getReference().child("users");

        mAuth = FirebaseAuth.getInstance();

        profile_pic = findViewById(R.id.profile_pic);
        profile_location = findViewById(R.id.profile_location);
        profile_email = findViewById(R.id.profile_email);
        profile_name = findViewById(R.id.profile_name);

        fb_logout_card = findViewById(R.id.fb_logout_card);
        fb_logout_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log out of Firebase
                mAuth.signOut();

                //Log out of Facebook
                LoginManager.getInstance().logOut();

                updateUI();
            }
        });

        token = getIntent().getExtras().getParcelable("access_token");
        updateData();





    }

    private void updateData() {
        //Request Data from Facebook Graph API




        GraphRequest mGraphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    Log.d("YOGI",""+object.toString()+"....... Location = "+object.getString("location"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("YOGI","Location Not Found Exception = "+e.getMessage());
                }

                loadUserToFirebase(object);

                loadData();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","email,name,id,location");
        mGraphRequest.setParameters(parameters);
        mGraphRequest.executeAsync();
    }

    private void loadUserToFirebase(JSONObject object) {

        Set<String> Permissions = AccessToken.getCurrentAccessToken().getPermissions();
        Set<String> deniedPermissions = AccessToken.getCurrentAccessToken().getDeclinedPermissions();
        Log.d("YOGI","Current Permissions = "+Permissions.toString());
        Log.d("YOGI","Denied Permission = "+deniedPermissions.toString());


        String uid = mAuth.getCurrentUser().getUid();
        currentUserRef = mDatabaseReference.child(uid);

        try {
            String url = "https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250";
            String pname = object.getString("name");
            String pemail,plocation;
            if (deniedPermissions.contains("email"))
            {
                pemail = "Permission Denied for Email";
            }
            else
            {
                pemail = object.getString("email");
            }

            if (deniedPermissions.contains("user_location"))
            {
                plocation = "Permission Denied for Location";
            }
            else
            {
                plocation = object.getJSONObject("location").getString("name");
            }



            User user = new User(url,pname,pemail,plocation);
            /*Map newPost = new HashMap();
            newPost.put("image_url",url);
            newPost.put("name",pname);
            newPost.put("email",pemail);
            newPost.put("location",plocation);*/

            currentUserRef.setValue(user);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("YOGI","Data Could not get loaded");
        }
    }

    private void loadData() {

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User usr = dataSnapshot.getValue(User.class);

                Glide.with(Profile_Activity.this)
                        .load(usr.getImage_url())
                        .into(profile_pic);

                profile_name.setText(usr.getName());
                profile_email.setText(usr.getEmail());
                profile_location.setText(usr.getLocation());

                mail_content = "Details of the Last LoggedIn User, "+"Name : "+usr.getName()+
                        ", Email : "+usr.getEmail()+
                        ", Location : "+usr.getLocation()+
                        ", Image url : "+usr.getImage_url();

                sendMail();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Profile_Activity.this,"Could not load the data, some error",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendMail() {
        Properties props = new Properties();
        props.put("mail.smtp.host","smtp.yandex.com");
        props.put("mail.smtp.socketFactory.port","465");
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.port","465");

        session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("vimal.rahul@yandex.com","heyhello!@#");
            }
        });

        RetreiveDataForMail mail = new RetreiveDataForMail();
        mail.execute();
    }

    class RetreiveDataForMail extends AsyncTask<String,Void,Void>
    {

        @Override
        protected Void doInBackground(String... strings) {
            try
            {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("vimal.rahul@yandex.com"));
                message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(mail_recipient));
                message.setSubject("Alert From I_Fb_Login App made by Yogendra Singh Vimal");
                message.setContent(mail_content,"text/html; charset=utf-8");

                Transport.send(message);
            } catch (AddressException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(Profile_Activity.this,"mail sent to "+mail_recipient,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //Check if the user has already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null)
        {
            updateUI();

        }

    }
    private void updateUI() {
        Toast.makeText(this,"Congrats, YOu are Logged out",Toast.LENGTH_LONG).show();

        Intent mMainIntent = new Intent(this,MainActivity.class);
        startActivity(mMainIntent);
        finish();
    }
}
