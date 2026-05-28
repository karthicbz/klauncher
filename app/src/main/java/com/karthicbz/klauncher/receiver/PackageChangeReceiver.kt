package com.karthicbz.klauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.karthicbz.klauncher.repository.AppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appRepository: AppRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                scope.launch {
                    appRepository.refreshApps()
                }
            }
        }
    }
}
