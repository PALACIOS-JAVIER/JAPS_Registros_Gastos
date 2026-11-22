package com.gastoclaro

import android.app.Application
import com.gastoclaro.core.notification.AppNotifications
import com.gastoclaro.core.util.DefaultCategories
import com.gastoclaro.domain.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GastoClaroApp : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var appNotifications: AppNotifications

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appNotifications.createChannels()
        seedDefaultCategories()
    }

    private fun seedDefaultCategories() {
        appScope.launch {
            if (categoryRepository.count() == 0) {
                categoryRepository.insertDefaults(DefaultCategories.all())
            }
        }
    }
}
