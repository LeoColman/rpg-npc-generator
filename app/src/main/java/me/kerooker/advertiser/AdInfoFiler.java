package me.kerooker.advertiser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Date;

import me.kerooker.rpgcharactergenerator.R;

/**
 * Class that is used to check the system conditions
 */
    class AdInfoFiler {

    private Activity parentActivity;

    AdInfoFiler(Activity ac) {
      this.parentActivity = ac;
    }

    Date getLastAdInfo() {
        return getLastStoredDate();
    }

    void setLastAdInfo(Date d) {
        long dateLong = d.getTime();
        storeDate(dateLong);
    }

    /**
     * Retrieves the last stored long for the date of the advertisement
     * @return A long value of the last date of ads or 0
     */
    private long getLastStoredDateLong() {
        SharedPreferences adPrefs = getSharedPreferences();
        return adPrefs.getLong(getAdvertiserPreferenceName(), 0);  //Defaults to 0

    }

    private SharedPreferences getSharedPreferences() {
        return parentActivity.getSharedPreferences(getAdvertiserFileName(), Context.MODE_PRIVATE);
    }

    private void storeDate(long l) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(getAdvertiserPreferenceName(), l);
        editor.apply();
    }

    private Date getLastStoredDate() {
        return new Date(getLastStoredDateLong());
    }

    @NonNull
    private String getAdvertiserFileName() {
        return parentActivity.getString(R.string.advertisement_date_file);
    }

    @NonNull
    private String getAdvertiserPreferenceName() {
        return parentActivity.getString(R.string.advertisement_shared_date_long);
    }


}
