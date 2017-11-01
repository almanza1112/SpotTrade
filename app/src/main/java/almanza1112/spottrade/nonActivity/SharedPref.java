package almanza1112.spottrade.nonActivity;

import android.content.Context;
import android.content.SharedPreferences;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 6/24/17.
 */

public class SharedPref {

    public static void setSharedPreferences(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SpotTradePref), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getSharedPreferences(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SpotTradePref), 0);
        return sharedPreferences.getString(key, null);
    }

    public static void removeSharedPreferences(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SpotTradePref), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void clearSharedPreferences(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SpotTradePref), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
