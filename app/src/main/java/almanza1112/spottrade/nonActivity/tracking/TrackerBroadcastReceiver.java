package almanza1112.spottrade.nonActivity.tracking;

/**
 * Created by almanza1112 on 10/22/17.
 */

/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.util.Log;

        import almanza1112.spottrade.MapsActivity;

public class TrackerBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "BroadCastReceiver";
    public TrackerBroadcastReceiver() {
        Log.e(TAG, "got called");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(TAG, "got called again");
            /*
             Intent start = new Intent(context, MapsActivity.class);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(start);
            */
        }
    }
}