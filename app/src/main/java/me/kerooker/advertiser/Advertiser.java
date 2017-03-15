package me.kerooker.advertiser;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Leonardo on 15/03/2017.
 */

public class Advertiser {

    private static final int minutesToWaitForAd = 60;

    private Activity parentActivity;

    private boolean shouldAdvertise() {

        AdInfoFiler aif = new AdInfoFiler(parentActivity);

        Date lastAd = aif.getLastAdInfo();
        Date now = new Date();  //Current date

        return isTimeAfter(lastAd, minutesToWaitForAd, now);
    }

    /**
     * Compares beforeDate and afterDate to check if afterDate is at least minuteDifference
     * after it.
     *
     * Example:
     * 15/03/2017, 15:00 beforeDate
     * 60 minuteDifference
     * 15/03/2017 16:00 afterDate
     * Returns true, as afterDate is at least 60 minutes after beforeDate
     *
     * @param beforeDate
     * @param minuteDifference
     * @param afterDate
     * @return true if the afterDate is at at least minuteDifference after beforeDate, false otherwise
     */
    private boolean isTimeAfter(Date beforeDate, int minuteDifference, Date afterDate) {
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTime(beforeDate);

        Calendar afterCal = Calendar.getInstance();
        afterCal.setTime(afterDate);

        lastCal.add(Calendar.MINUTE, minuteDifference);

        if (afterCal.after(lastCal))return true;
        return false;
    }

    private void openAdvertisement() {
        //TODO
    }

    private void saveLastAdInfo() {
        AdInfoFiler aif = new AdInfoFiler(getParentActivity());
        Date now = new Date();
        aif.setLastAdInfo(now);
    }

    private void setParentActivity(Activity ac) {
        this.parentActivity = ac;
    }

    private Activity getParentActivity() {
        return parentActivity;
    }

    public static void attemptAdvertisement(Activity activity) {
        Advertiser a = new Advertiser();
        a.setParentActivity(activity);

        if (a.shouldAdvertise()) {
            a.openAdvertisement();
        }
    }


}
