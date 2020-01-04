package me.kerooker.rpgnpcgenerator.view.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.viewmodel.settings.SettingsViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat() {
    
    private val settingsViewModel by viewModel<SettingsViewModel>()
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        setupPreferences()
    }
    
    private fun setupPreferences() {
        setupRepositoryTouch()
        setupOpenSourceLibrariesTouch()
        setupReportBugTouch()
        setupGoPro()
    }
    
    private fun setupRepositoryTouch() {
        findPreference<Preference>("project_repository")!!.setOnPreferenceClickListener {
            openUrl("https://github.com/Kerooker/rpg-npc-generator")
            true
        }
    }
    
    private fun setupOpenSourceLibrariesTouch() {
        findPreference<Preference>("open_source_libs")!!.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
            true
        }
    }
    
    private fun setupReportBugTouch() {
        findPreference<Preference>("bug_report")!!.setOnPreferenceClickListener {
            openUrl("https://github.com/Kerooker/rpg-npc-generator/issues")
            true
        } 
    }
    
    private fun setupGoPro() {
        val preference = findPreference<Preference>("go_pro")!! 
        if(settingsViewModel.isPro()) {
            preference.isEnabled = false
        }
        preference.setOnPreferenceClickListener {
            openUrl("https://play.google.com/store/apps/details?id=me.kerooker.rpgcharactergeneratorpro")
            true
        }
    }
    
    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
