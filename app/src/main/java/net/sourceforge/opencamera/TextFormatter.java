package net.sourceforge.opencamera;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Handles various text formatting options, used for photo stamp and video subtitles.
 */
public class TextFormatter {
    private static final String TAG = "TextFormatter";

    private final Context context;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.0");

    TextFormatter(Context context) {
        this.context = context;
    }

    /** Formats the date according to the user preference preference_stamp_dateformat.
     *  Returns "" if preference_stamp_dateformat is "preference_stamp_dateformat_none".
     */
    public static String getDateString(String preference_stamp_dateformat, Date date) {
        String date_stamp = "";
        if( !preference_stamp_dateformat.equals("preference_stamp_dateformat_none") ) {
            switch(preference_stamp_dateformat) {
                case "preference_stamp_dateformat_yyyymmdd":
                    // use dashes instead of slashes - this should follow https://en.wikipedia.org/wiki/ISO_8601
                    date_stamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                    break;
                case "preference_stamp_dateformat_ddmmyyyy":
                    date_stamp = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                    break;
                case "preference_stamp_dateformat_mmddyyyy":
                    date_stamp = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(date);
                    break;
                default:
                    date_stamp = DateFormat.getDateInstance().format(date);
                    break;
            }
        }
        return date_stamp;
    }

    /** Formats the time according to the user preference preference_stamp_timeformat.
     *  Returns "" if preference_stamp_timeformat is "preference_stamp_timeformat_none".
     */
    public static String getTimeString(String preference_stamp_timeformat, Date date) {
        String time_stamp = "";
        if( !preference_stamp_timeformat.equals("preference_stamp_timeformat_none") ) {
            switch(preference_stamp_timeformat) {
                case "preference_stamp_timeformat_12hour":
                    time_stamp = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(date);
                    break;
                case "preference_stamp_timeformat_24hour":
                    time_stamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
                    break;
                default:
                    time_stamp = DateFormat.getTimeInstance().format(date);
                    break;
            }
        }
        return time_stamp;
    }

    private String getDistanceString(double distance, String preference_units_distance) {
        double converted_distance = distance;
        String units = context.getResources().getString(R.string.metres_abbreviation);
        if( preference_units_distance.equals("preference_units_distance_ft") ) {
            converted_distance = 3.28084 * distance;
            units = context.getResources().getString(R.string.feet_abbreviation);
        }
        return decimalFormat.format(converted_distance) + units;
    }

    /** Formats the GPS information according to the user preference_stamp_gpsformat preference_stamp_timeformat.
     *  Returns "" if preference_stamp_gpsformat is "preference_stamp_gpsformat_none", or both store_location and
     *  store_geo_direction are false.
     */
    public String getGPSString(String preference_stamp_gpsformat, String preference_units_distance, boolean store_location, Location location, boolean store_geo_direction, double geo_direction) {
        String gps_stamp = "";
        if( !preference_stamp_gpsformat.equals("preference_stamp_gpsformat_none") ) {
            if( store_location ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "location: " + location);
                if( preference_stamp_gpsformat.equals("preference_stamp_gpsformat_dms") )
                    gps_stamp += LocationSupplier.locationToDMS(location.getLatitude()) + ", " + LocationSupplier.locationToDMS(location.getLongitude());
                else
                    gps_stamp += Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + ", " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                if( location.hasAltitude() ) {
                    gps_stamp += ", " + getDistanceString(location.getAltitude(), preference_units_distance);
                }
            }
            if( store_geo_direction ) {
                float geo_angle = (float)Math.toDegrees(geo_direction);
                if( geo_angle < 0.0f ) {
                    geo_angle += 360.0f;
                }
                if( MyDebug.LOG )
                    Log.d(TAG, "geo_angle: " + geo_angle);
                if( gps_stamp.length() > 0 )
                    gps_stamp += ", ";
                gps_stamp += String.valueOf(Math.round(geo_angle)) + (char)0x00B0;
            }
        }
        // don't log gps_stamp, in case of privacy!
        return gps_stamp;
    }

    public static String formatTimeMS(long time_ms) {
        int ms = (int) (time_ms) % 1000 ;
        int seconds = (int) (time_ms / 1000) % 60 ;
        int minutes = (int) ((time_ms / (1000*60)) % 60);
        int hours   = (int) ((time_ms / (1000*60*60)));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d,%03d", hours, minutes, seconds, ms);
    }

}
