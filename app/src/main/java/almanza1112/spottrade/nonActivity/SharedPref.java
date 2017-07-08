package almanza1112.spottrade.nonActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by almanza1112 on 6/24/17.
 */

public class SharedPref {

    private static final String PREF_USER_ID = "uid";
    private static final String PREF_USER_PASSWORD = "password";
    private static final String PREF_USER_FIRSTNAME = "firstName";
    private static final String PREF_USER_LASTNAME = "lastName";
    private static final String PREF_USER_EMAIL = "email";
    private static final String PREF_USER_PHONE_NUMBER = "phoneNumber";
    private static final String PREF_USER_TOTAL_RATINGS = "totalRatings";
    private static final String PREF_USER_OVERALL_RATING = "overallRating";


    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setID(Context ctx, String uid)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_ID, uid);
        editor.apply();
    }

    public static String getID(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_ID, "");
    }


    public static void clearAll(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.apply();
    }

    public static void setPhoneNumber(Context ctx, String phoneNumber){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    public static String getPhoneNumber(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_PHONE_NUMBER, "");
    }

    public static void clearPhoneNumber(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_PHONE_NUMBER);
        editor.apply();
    }

    /* First name */
    public static void setFirstName(Context ctx, String firstName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_FIRSTNAME, firstName);
        editor.apply();
    }

    public static String getFirstName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_FIRSTNAME, "");
    }

    public static void clearFirstName(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_FIRSTNAME);
        editor.apply();
    }

    public static void setLastName(Context ctx, String lastName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_LASTNAME, lastName);
        editor.apply();
    }

    public static String getLastName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_LASTNAME, "");
    }

    public static void clearLastName(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_LASTNAME);
        editor.apply();
    }


    public static void setEmail(Context ctx, String email){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_EMAIL, email);
        editor.apply();
    }

    public static String getEmail(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_EMAIL, "");
    }

    public static void clearEmail(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_EMAIL);
        editor.apply();
    }

    public static void setPassword(Context ctx, String password){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_PASSWORD, password);
        editor.apply();
    }

    public static String getPassword(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_PASSWORD, "");
    }

    public static void clearPassword(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_PASSWORD);
        editor.apply();
    }


    public static void setTotalRatings(Context ctx, String password){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_TOTAL_RATINGS, password);
        editor.apply();
    }

    public static String getTotalRatings(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_TOTAL_RATINGS, "");
    }

    public static void clearTotalRatings(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_TOTAL_RATINGS);
        editor.apply();
    }

    public static void setOverallRating(Context ctx, String password){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_OVERALL_RATING, password);
        editor.apply();
    }

    public static String getOverallRating(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_OVERALL_RATING, "");
    }

    public static void clearOverallRating(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_OVERALL_RATING);
        editor.apply();
    }



}
