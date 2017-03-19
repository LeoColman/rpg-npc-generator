package me.kerooker.advertiser;

import android.app.Activity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Calendar;
import java.util.Date;

import me.kerooker.rpgcharactergenerator.MainActivity;
import me.kerooker.rpgcharactergenerator.R;

/**
 * Class that handles an Advertisement together with AdInfoFiler.
 * It verifies all the stablished conditions for an advertisement to be played,
 * and does it if the conditions are met.
 * Created by Kerooker on 15/03/2017
 */
public class Advertiser {

    /* Minutes to wait for a next ad to play */
    private static final int minutesToWaitForAd = 60;

    private MainActivity parentActivity;

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
     * @param beforeDate The date that happened before the verified date
     * @param minuteDifference The difference in minutes to check for
     * @param afterDate The date to check if beforeDate happened minuteDifference minutes before this
     * @return true if the afterDate is at at least minuteDifference after beforeDate, false otherwise
     */
    private boolean isTimeAfter(Date beforeDate, int minuteDifference, Date afterDate) {
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTime(beforeDate);

        Calendar afterCal = Calendar.getInstance();
        afterCal.setTime(afterDate);

        lastCal.add(Calendar.MINUTE, minuteDifference);

        return afterCal.after(lastCal);

    }

    private void openAdvertisement() {
        createAdAndLoad();

        saveLastAdInfo();
    }

    private void createAdAndLoad() {
        final InterstitialAd ad = new InterstitialAd(parentActivity.getApplicationContext());
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("E13C2E83535549B29E3A07FBE4059DAD").build();

        String adUnitId = getParentActivity().getString(R.string.ad_unit_id);
        ad.setAdUnitId(adUnitId);

        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                ad.show();
            }

            @Override
            public void onAdClosed() {
                finishLoadingAd();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                finishLoadingAd();
            }

            @Override
            public void onAdLeftApplication() {
                finishLoadingAd();
            }
        });

        ad.loadAd(adRequest);


    }

    private void finishLoadingAd() {
        parentActivity.finishLoadingAd();
    }

    private void saveLastAdInfo() {
        AdInfoFiler aif = new AdInfoFiler(getParentActivity());
        Date now = new Date();
        aif.setLastAdInfo(now);
    }

    private void setParentActivity(MainActivity ac) {
        this.parentActivity = ac;
    }

    private Activity getParentActivity() {
        return parentActivity;
    }

    public static void attemptAdvertisement(MainActivity activity) {
        Advertiser a = new Advertiser();
        a.setParentActivity(activity);

        if (a.shouldAdvertise()) {
            a.openAdvertisement();
        }else {
            activity.finishLoadingAd();
        }
    }

}
