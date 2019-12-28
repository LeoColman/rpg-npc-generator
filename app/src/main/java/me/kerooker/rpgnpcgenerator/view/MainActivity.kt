package me.kerooker.rpgnpcgenerator.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.activity_main.bottom_ad_container
import kotlinx.android.synthetic.main.activity_main.bottom_navigation_view
import kotlinx.android.synthetic.main.activity_main.toolbar
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.databinding.ActivityMainBinding
import me.kerooker.rpgnpcgenerator.viewmodel.admob.AdmobViewModel
import org.koin.android.ext.android.inject
import splitties.alertdialog.alertDialog
import splitties.alertdialog.messageResource
import splitties.alertdialog.negativeButton
import splitties.alertdialog.neutralButton
import splitties.alertdialog.onShow
import splitties.alertdialog.positiveButton
import splitties.alertdialog.titleResource

class MainActivity : AppCompatActivity() {

    private val admobViewModel by inject<AdmobViewModel>()
    private lateinit var rewardedAd: RewardedAd
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@MainActivity
            shouldShowAd = admobViewModel.shouldShowAd
        }
        
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment)
        bottom_navigation_view.setupWithNavController(navController)
        
        setSupportActionBar(toolbar)
    }
    
    override fun onResume() {
        super.onResume()
        setupBottomAd()
        createRewardedAd()
    }
    
    private fun setupBottomAd() {
        if(admobViewModel.shouldShowAd.value == false) return
        val view = AdView(this)
        view.adSize = AdSize.SMART_BANNER
        view.adUnitId = admobViewModel.bannerAdId
        
        bottom_ad_container.removeAllViews()
        bottom_ad_container.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        
        view.loadAd(AdRequest.Builder().build())
    }
    
    
    private fun createRewardedAd() {
        rewardedAd = RewardedAd(this, admobViewModel.rewardedAdId).apply {
            loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
    
                override fun onRewardedAdLoaded() {
                    setupDismissAdsDialog()
                }
            })
        }
    }
    
    private fun setupDismissAdsDialog() {
        if(!admobViewModel.shouldSuggestRemovingAds()) return
        suggestRemovingAds()
        admobViewModel.suggestedRemovingAds()
    }
    
    private fun suggestRemovingAds() {
        alertDialog {
            titleResource = R.string.disable_ads_title
            messageResource = R.string.disable_ads_message
            
            positiveButton(R.string.go_pro) { goPro() }
            negativeButton(R.string.watch_ad_remove_ad) { watchAd() }
            neutralButton(R.string.not_now) { it.dismiss() }
        }.onShow {
            positiveButton.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
            negativeButton.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
            neutralButton.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
            
            positiveButton.setTextColor(context.resources.getColor(R.color.colorPrimary))
            negativeButton.setTextColor(context.resources.getColor(R.color.colorAccent))
            neutralButton.setTextColor(context.resources.getColor(R.color.colorPrimary))
        }.show()
    }
    
    private fun watchAd() {
        rewardedAd.show(this, object : RewardedAdCallback() {
            override fun onUserEarnedReward(item: RewardItem) {
                admobViewModel.watchedRewardedAd()
            }
        })
    }
    
    private fun goPro() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, 
                Uri.parse("https://play.google.com/store/apps/details?id=me.kerooker.rpgcharactergeneratorpro")
            )
        )
    }
}
