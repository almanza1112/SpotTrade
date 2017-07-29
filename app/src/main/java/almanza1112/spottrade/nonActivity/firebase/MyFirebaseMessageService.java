package almanza1112.spottrade.nonActivity.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 7/9/17.
 */

public class MyFirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "MessageService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Log data to Log Cat
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Log.e(TAG, "Data Message Body: " + remoteMessage.getData().get("message"));
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //create notification
        createNotification(remoteMessage.getNotification().getBody());
    }


    public void createNotification( String messageBody) {
        Log.e("firebase", "i am calling this ish");
        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_account_circle_black_24dp)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");


// Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//When you issue multiple notifications about the same type of event, it’s best practice for your app to try to update an existing notification with this new information, rather than immediately creating a new notification. If you want to update this notification at a later date, you need to assign it an ID. You can then use this ID whenever you issue a subsequent notification. If the previous notification is still visible, the system will update this existing notification, rather than create a new one. In this example, the notification’s ID is 001//

                mNotificationManager.notify(001, mBuilder.build());
    }

}
