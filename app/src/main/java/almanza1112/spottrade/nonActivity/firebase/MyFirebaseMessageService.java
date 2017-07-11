package almanza1112.spottrade.nonActivity.firebase;

/**
 * Created by almanza1112 on 7/9/17.
 */

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

        import almanza1112.spottrade.R;

public class MyFirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "MessageService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Log data to Log Cat
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        Log.e(TAG, "Data Message Body: " + remoteMessage.getData().get("message"));
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //create notification
        //createNotification(remoteMessage.getNotification().getBody());
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.e(TAG, s);

    }
    /*
    private void createNotification( String messageBody) {
        Intent intent = new Intent( this , ResultActivity. class );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Android Tutorial Point FCM Tutorial")
                .setContentText(messageBody)
                .setAutoCancel( true )
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());
    }
    */
}
