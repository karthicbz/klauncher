package com.karthicbz.klauncher.repository

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.telephony.TelephonyManager
import com.karthicbz.klauncher.data.model.AppDailyUsage
import com.karthicbz.klauncher.data.model.AppWeekTotal
import com.karthicbz.klauncher.data.model.DayUsage
import com.karthicbz.klauncher.data.model.WeekSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val TOP_APPS_LIMIT = 25
private const val MS_PER_DAY = 86_400_000L

@Singleton
class DataUsageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val networkStatsManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    fun isPermissionGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getCurrentWeekDailyUsage(): List<AppDailyUsage> {
        val (weekStart, dayLabels, todayIndex) = currentWeekInfo()

        val dailyUidBytes = Array(7) { HashMap<Int, Long>() }
        for (i in 0..6) {
            val dayStart = weekStart + i * MS_PER_DAY
            val dayEnd = dayStart + MS_PER_DAY
            queryNetworkStats(dayStart, dayEnd, dailyUidBytes[i])
        }

        val allUids = dailyUidBytes.flatMap { it.keys }.toSet()
        val pm = context.packageManager

        return allUids
            .mapNotNull { uid ->
                val pkgName = pm.getPackagesForUid(uid)?.firstOrNull() ?: return@mapNotNull null
                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString()
                } catch (_: Exception) { pkgName }

                val daily = dayLabels.mapIndexed { i, label ->
                    DayUsage(
                        label = label,
                        bytes = dailyUidBytes[i][uid] ?: 0L,
                        isToday = i == todayIndex
                    )
                }
                val total = daily.sumOf { it.bytes }
                if (total == 0L) return@mapNotNull null

                AppDailyUsage(
                    packageName = pkgName,
                    appName = appName,
                    weekTotalBytes = total,
                    dailyBytes = daily
                )
            }
            .sortedByDescending { it.weekTotalBytes }
            .take(TOP_APPS_LIMIT)
    }

    fun getPreviousWeeksSummaries(): List<WeekSummary> {
        val (currentWeekStart, _, _) = currentWeekInfo()
        val pm = context.packageManager

        return (1..3).map { weeksBack ->
            val weekEnd = currentWeekStart - (weeksBack - 1) * 7 * MS_PER_DAY
            val weekStart = weekEnd - 7 * MS_PER_DAY

            val byUid = HashMap<Int, Long>()
            queryNetworkStats(weekStart, weekEnd, byUid)

            val apps = byUid.entries
                .mapNotNull { (uid, bytes) ->
                    if (bytes == 0L) return@mapNotNull null
                    val pkgName = pm.getPackagesForUid(uid)?.firstOrNull()
                        ?: return@mapNotNull null
                    val appName = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString()
                    } catch (_: Exception) { pkgName }
                    AppWeekTotal(packageName = pkgName, appName = appName, totalBytes = bytes)
                }
                .sortedByDescending { it.totalBytes }
                .take(TOP_APPS_LIMIT)

            val label = when (weeksBack) {
                1 -> "Last Week"
                2 -> "2 Weeks Ago"
                else -> "3 Weeks Ago"
            }
            WeekSummary(label = label, startMs = weekStart, endMs = weekEnd, apps = apps)
        }
    }

    fun getWeeklyTotalsForApp(packageName: String): List<Pair<String, Long>> {
        val (currentWeekStart, _, _) = currentWeekInfo()
        val uid = uidForPackage(packageName) ?: return emptyList()
        val pm = context.packageManager
        val now = System.currentTimeMillis()

        return (0..3).map { weeksBack ->
            val weekEnd = if (weeksBack == 0) now else currentWeekStart - (weeksBack - 1) * 7 * MS_PER_DAY
            val weekStart = if (weeksBack == 0) currentWeekStart else weekEnd - 7 * MS_PER_DAY

            val byUid = HashMap<Int, Long>()
            queryNetworkStats(weekStart, weekEnd, byUid)

            val label = when (weeksBack) {
                0 -> "This Week"
                1 -> "Last Wk"
                2 -> "2W Ago"
                else -> "3W Ago"
            }
            label to (byUid[uid] ?: 0L)
        }.reversed()
    }

    // ── internals ──────────────────────────────────────────────────────────────

    private data class WeekInfo(
        val weekStartMs: Long,
        val dayLabels: List<String>,
        val todayIndex: Int
    )

    private fun currentWeekInfo(): WeekInfo {
        val cal = Calendar.getInstance()
        val todayDow = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMon = if (todayDow == Calendar.SUNDAY) 6 else todayDow - Calendar.MONDAY
        val todayIndex = daysFromMon

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMon)
        val weekStart = cal.timeInMillis

        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        val labels = (0..6).map { i ->
            fmt.format(Date(weekStart + i * MS_PER_DAY))
        }

        return WeekInfo(weekStart, labels, todayIndex)
    }

    @Suppress("DEPRECATION")
    private fun queryNetworkStats(startMs: Long, endMs: Long, into: HashMap<Int, Long>) {
        fun consumeStats(stats: NetworkStats) {
            val bucket = NetworkStats.Bucket()
            try {
                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)
                    val uid = bucket.uid
                    if (uid < 1000) continue
                    val bytes = bucket.rxBytes + bucket.txBytes
                    if (bytes > 0) into[uid] = (into[uid] ?: 0L) + bytes
                }
            } finally {
                try { stats.close() } catch (_: Exception) {}
            }
        }

        // WiFi
        try {
            consumeStats(
                networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_WIFI, null, startMs, endMs
                )
            )
        } catch (_: Exception) {}

        // Mobile (optional — TV devices may not have cellular)
        try {
            @Suppress("MissingPermission")
            val subId = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
                    ?.subscriberId
            } else null
            consumeStats(
                networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_MOBILE, subId, startMs, endMs
                )
            )
        } catch (_: Exception) {}
    }

    private fun uidForPackage(packageName: String): Int? = try {
        context.packageManager.getApplicationInfo(packageName, 0).uid
    } catch (_: Exception) { null }
}
