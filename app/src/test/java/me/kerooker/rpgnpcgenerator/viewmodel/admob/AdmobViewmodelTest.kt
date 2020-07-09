package me.kerooker.rpgnpcgenerator.viewmodel.admob

import io.kotest.IsolationMode.InstancePerTest
import io.kotest.android.InstantLivedataListener
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.shouldBe
import io.kotest.specs.BehaviorSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import me.kerooker.rpgnpcgenerator.repository.model.persistence.admob.AdmobRepository
import java.util.Date

class AdmobViewmodelTest : BehaviorSpec() {
    
    private val admobRepository = mockk<AdmobRepository>()
    private val target by lazy { AdmobViewModel(admobRepository) }
    
    init {
        mockkObject(MyBuildConfig)
        
        Given("The app ID contains PRO (Should show ads test)") {
            
            And("App is in debug mode") {
                every { MyBuildConfig.APPLICATION_ID } returns "me.kerooker.rpgcharactergeneratorpro.debug"
                
                When("stopAdsUntil is before now") {
                    every { admobRepository.stopAdsUntil } returns Date().time - 1L
                    
                    Then("Ads should not be shown") {
                        target.shouldShowAd.value shouldBe false
                    }
                    
                }
                
                When("stopAdsUntil is after now") {
                    every { admobRepository.stopAdsUntil } returns Date().time + 5_000L
                    
                    Then("Ads should not be shown") {
                        target.shouldShowAd.value shouldBe false
                    }
                }
            }
            
            And("App is not in debug mode") {
                every { MyBuildConfig.APPLICATION_ID } returns "me.kerooker.rpgcharactergeneratorpro"
                
                When("stopAdsUntil is before now") {
                    every { admobRepository.stopAdsUntil } returns Date().time - 1L
                    
                    Then("Ads should not be shown") {
                        target.shouldShowAd.value shouldBe false
                    }
                }
                
                When("stopAdsUntil is after now") {
                    every { admobRepository.stopAdsUntil } returns Date().time + 5_000L
                    
                    Then("Ads should not be shown") {
                        target.shouldShowAd.value shouldBe false
                    }
                }
            }
        }
        
        Given("The app ID doesn't contain PRO (Should show ads test)") {
            And("App is in debug mode") {
                every { MyBuildConfig.APPLICATION_ID } returns "me.kerooker.rpgcharactergenerator.debug"
                
                When("stopAdsUntil is before now") {
                    every { admobRepository.stopAdsUntil } returns Date().time - 1L
                    
                    Then("Ads should be shown") {
                        target.shouldShowAd.value shouldBe true
                    }
                    
                    Then("Ad banner ID should be debug banner") {
                        target.bannerAdId shouldBe "ca-app-pub-3940256099942544/6300978111"
                    }
                    
                    Then("Rewarded ad ID should be debug rewarded ad") {
                        target.rewardedAdId shouldBe "ca-app-pub-3940256099942544/5224354917"
                    }
                }
                
                When("stopAdsUntil is after now") {
                    every { admobRepository.stopAdsUntil } returns Date().time + 5_000L
                    
                    Then("Ads should not be shown") {
                        target.shouldShowAd.value shouldBe false
                    }
                }
            }
            
            And("App is not in debug mode") {
                every { MyBuildConfig.APPLICATION_ID } returns "me.kerooker.rpgcharactergenerator"
                
                When("stopAdsUntil is before now") {
                    every { admobRepository.stopAdsUntil } returns Date().time - 1L
                    
                    Then("Ads should be shown") {
                        target.shouldShowAd.value shouldBe true
                    }
                    
                    Then("Ad banner ID should be production banner") {
                        target.bannerAdId shouldBe "ca-app-pub-4066886200642192/4591525789"
                    }
                    
                    Then("Rewarded ad ID should be production id") {
                        target.rewardedAdId shouldBe "ca-app-pub-4066886200642192/4016810716"
                    }
                }
                
                When("stopAdsUntil is after now") {
                    every { admobRepository.stopAdsUntil } returns Date().time + 5_000L
                    
                    Then("Ads should not be shown") {
                        target.shouldShowAd.value shouldBe false
                    }
                }
            }
        }
        
        Given("The app ID contains PRO (Should suggest removal test)") {
            every { MyBuildConfig.APPLICATION_ID } returns "me.kerooker.rpgcharactergeneratorpro"
            
            And("stopAdsUntil is before now") {
                every { admobRepository.stopAdsUntil } returns Date().time - 1L
                
                And("stopSuggestingRemovalUntil is before now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time - 1L
                    
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
                
                And("stopSuggestingRemovalUntil is after now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time + 5_000L
                    
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
            }
            
            And("stopAdsUntil is after now") {
                every { admobRepository.stopAdsUntil } returns Date().time + 5_000L
                
                And("stopSuggestingRemovalUntil is before now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time - 1L
    
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
    
                And("stopSuggestingRemovalUntil is after now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time + 5_000L
    
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
            }
        }
    
        Given("The app ID doesn't contain PRO (Should suggest removal test)") {
            every { MyBuildConfig.APPLICATION_ID } returns "me.kerooker.rpgcharactergenerator"
        
            And("stopAdsUntil is before now") {
                every { admobRepository.stopAdsUntil } returns Date().time - 1L
            
                And("stopSuggestingRemovalUntil is before now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time - 1L
                
                    Then("Should suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe true
                    }
                }
            
                And("stopSuggestingRemovalUntil is after now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time + 5_000L
                
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
            }
        
            And("stopAdsUntil is after now") {
                every { admobRepository.stopAdsUntil } returns Date().time + 5_000L
            
                And("stopSuggestingRemovalUntil is before now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time - 1L
                
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
            
                And("stopSuggestingRemovalUntil is after now") {
                    every { admobRepository.stopSuggestingRemovalUntil } returns Date().time + 5_000L
                
                    Then("Should not suggest removing ads") {
                        target.shouldSuggestRemovingAds() shouldBe false
                    }
                }
            }
        }
        
        Given("The ad removal were suggested") {
            val setValue = slot<Long>()
            every { admobRepository setProperty "stopSuggestingRemovalUntil" value capture(setValue) } just Runs
            
            target.suggestedRemovingAds()
            
            Then("The value should be set to ten minutes from now") {
                val now = Date().time
                setValue.captured shouldBeInRange (now + (60 * 9 * 1000)..now + (60 * 11 * 1000))
            }
        }
        
        Given("The rewarded ad was watched") {
            val stopAdsUntil = slot<Long>()
            val stopSuggestingRemovalUntil = slot<Long>()
            val now = Date().time
            
            every { admobRepository setProperty "stopAdsUntil" value capture(stopAdsUntil) } just Runs
            every { admobRepository setProperty "stopSuggestingRemovalUntil" value capture(stopSuggestingRemovalUntil) } just Runs
            every { admobRepository.stopAdsUntil }.returnsMany(now - 1_000, now + 10_000L)
            
            target.watchedRewardedAd()
            
            Then("There should be no ads for a day") {
                stopAdsUntil.captured shouldBeInRange (now + (24 * 59 * 60 * 1000)..now + (24 * 61 * 60 * 1000))
            }
            
            Then("There should be no suggestion to remove ads for a day") {
                stopSuggestingRemovalUntil.captured shouldBeInRange (now + (24 * 59 * 60 * 1000)..now + (24 * 61 * 60 * 1000))
            }
            
            Then("The should show ads value should be update to false") {
                target.shouldShowAd.value!! shouldBe false
            }
        }
    }
    
    override fun listeners() = listOf(InstantLivedataListener())
    
    override fun isolationMode() = InstancePerTest
    
}
