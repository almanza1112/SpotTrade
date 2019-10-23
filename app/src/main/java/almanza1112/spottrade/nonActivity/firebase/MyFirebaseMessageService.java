package almanza1112.spottrade.nonActivity.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import almanza1112.spottrade.nonActivity.SharedPref;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 7/9/17.
 */

public class MyFirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "MessageService";

    @Override
    public void onNewToken(String refreshedToken){
        Log.e("onTokenRefresh", "called");
        //Get hold of the registration token
        //String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //sendRegistrationToServer(refreshedToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Log.e(TAG, "Notification Title : "+ remoteMessage.getNotification().getTitle());
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Log.e(TAG, "Data Message Body: " + remoteMessage.getData().get("message"));
        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationText = remoteMessage.getNotification().getBody();
        String notificationData = remoteMessage.getData().get("message");

        Intent resultIntent = new Intent(this, MapsActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra("message", notificationData);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // The id of the channel.
        String channelID = "my_channel_01";

        // The user-visible name of the channel.
        CharSequence name = "SpotTrade";

        // The user-visible description of the channel.
        String description = "SpotTrade's notification channel";

        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_LOW;
        }

        NotificationChannel mChannel = new NotificationChannel(channelID, name, importance);

        // Configure the notification channel.
        mChannel.setDescription(description);

        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);

        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        mNotificationManager.createNotificationChannel(mChannel);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_account_circle_black_24dp)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setAutoCancel(true)
                        .setChannelId(channelID)
                        .setContentIntent(resultPendingIntent);

        //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /*
        When you issue multiple notifications about the same type of event,
        it’s best practice for your app to try to update an existing notification with
        this new information, rather than immediately creating a new notification.
        If you want to update this notification at a later date, you need to assign it an ID.
        You can then use this ID whenever you issue a subsequent notification.
        if the previous notification is still visible,
        the system will update this existing notification, rather than create a new one.
        In this example, the notification’s ID is 001
        */
        mNotificationManager.notify(0, mBuilder.build());
    }

    //Implement this method if you want to store the token on your server
    public void sendRegistrationToServer(String token) {
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("firebaseTokenID", token);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getResources().getString(R.string.URL) + "/user/update/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("responseUp", response +"");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

}
