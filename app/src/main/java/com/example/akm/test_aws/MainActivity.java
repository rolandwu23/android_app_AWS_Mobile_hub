package com.example.akm.test_aws;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.google.gson.Gson;

import java.util.concurrent.locks.Condition;

public class MainActivity extends AppCompatActivity {

    DynamoDBMapper dynamoDBMapper;
    public static PinpointManager pinpointManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        createNews();
        readNews();




        AWSMobileClient.getInstance().initialize(this).execute();

        PinpointConfiguration config = new PinpointConfiguration(
                MainActivity.this,
                AWSMobileClient.getInstance().getCredentialsProvider(),
                AWSMobileClient.getInstance().getConfiguration()
        );
        pinpointManager = new PinpointManager(config);
        pinpointManager.getSessionClient().startSession();
        pinpointManager.getAnalyticsClient().submitEvents();

      AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
           @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
               SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(MainActivity.this, SignInUI.class);
               signin.login(MainActivity.this, Main2Activity.class).execute();
           }
       }).execute();


//        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
//            @Override
//            public void onComplete(final AWSStartupResult awsStartupResult) {
//                AuthUIConfiguration config =
//                        new AuthUIConfiguration.Builder()
//                                .userPools(true)  // true? show the Email and Password UI
//                                .signInButton(FacebookButton.class) // Show Facebook button
//                                .signInButton(GoogleButton.class) // Show Google button
//                                .logoResId(R.drawable.mylogo) // Change the logo
//                                .backgroundColor(Color.BLUE) // Change the backgroundColor
//                                .isBackgroundColorFullScreen(true) // Full screen backgroundColor the backgroundColor full screenff
//                                .fontFamily("sans-serif-light") // Apply sans-serif-light as the global font
//                                .canCancel(true)
//                                .build();
//                SignInUI signinUI = (SignInUI) AWSMobileClient.getInstance().getClient(MainActivity.this, SignInUI.class);
//                signinUI.login(MainActivity.this, Main2Activity.class).authUIConfiguration(config).execute();
//            }
//        }).execute();
    }



    public void createNews() {
        final NewsDO newsItem = new NewsDO();

        newsItem.setUserId("unique-user-id");

        newsItem.setArticleId("Article1");
        newsItem.setContent("This is the article content");

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newsItem);
                Log.d("YourMainActivity","Successfully saved");
                // Item saved
            }
        }).start();
    }

    public void readNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                NewsDO newsItem = dynamoDBMapper.load(
                        NewsDO.class,
                        "unique-user-id",
                        "Article1");

                // Item read
                 Log.d("News Item:", newsItem.toString());
                 Log.d("News Item",newsItem.getContent());
            }
        }).start();
    }

}
