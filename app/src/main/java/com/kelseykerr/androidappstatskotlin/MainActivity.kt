package com.kelseykerr.androidappstatskotlin

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import com.kelseykerr.androidappstatskotlin.Models.AppItem
import com.kelseykerr.androidappstatskotlin.Models.AppStats
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = this.javaClass.getSimpleName()
    private var appStatsMap: HashMap<String, AppStats>? = null
    private var appItems: ArrayList<AppItem> = ArrayList<AppItem>()
    private var appList: ListView? = null
    private var appItemAdapter: AppItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instantiateAppStatsMap()

        val appListVar = findViewById(R.id.app_list) as ListView
        appItemAdapter = AppItemAdapter(this, R.layout.app_item, appItems)
        appListVar.adapter = appItemAdapter
        appListVar.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val appItem = appItems.get(position)
            val appStats = getBatteryStats(appItem.packageName)
            val newFragment = AppStatsFragment.newInstance(appStats)
            newFragment.show(fragmentManager, "dialog")
        })
        appList = appListVar

    }

    private fun instantiateAppStatsMap() {
        appStatsMap = HashMap<String, AppStats>()
        appItems = ArrayList()
        try {
            val process = Runtime.getRuntime().exec("pm list packages")
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream))
            var line: String
            while ((line = bufferedReader.readLine()) != null) {
                val packageName = line.replace("package:", "")
                val appIcon = packageManager.getApplicationIcon(packageName)
                val ai = packageManager.getApplicationInfo(packageName, 0)
                val appName = if (ai != null) packageManager.getApplicationLabel(ai) as String else null
                appItems.add(AppItem(appName, packageName, appIcon))
                appStatsMap.put(packageName, AppStats())
                Log.d(TAG, "added package [$packageName]")
            }
            Collections.sort(appItems)
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't list packages, got error: " + e.message)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package was not found, skipping")
        }

    }

    fun getBatteryStats(packageName: String): AppStats? {
        try {
            val appStats = AppStats()
            val process = Runtime.getRuntime().exec("dumpsys batterystats $packageName -c")
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream))
            var line: String? = bufferedReader.readLine()
            var uid = ""
            var partialWakeLockCount: Int? = 0
            var fullWakeLockCount: Int? = 0
            while (line != null) {
                val lineData = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val id = lineData[3]
                when (id) {
                    "uid" -> if (lineData[5] == packageName) {
                        uid = lineData[4]
                        Log.d(TAG, "set UID of app to [$uid]")
                    }
                    "wl" -> if (lineData[1] == uid) {
                        Log.d(TAG, "wake lock found")
                        //line: 9,10145,l,wl,*alarm*,0,f,0,0,0,0,148,p,12,0,42,248,248,bp,12,0,42,248,0,w,0,0,0,0
                        //values: 0, 0, 0, 0, wake lock, full time, 'f', full count, partial time, 'p', partial count, window time, 'w', window count
                        if (lineData[5] != "0") {
                            fullWakeLockCount++
                        } else {
                            partialWakeLockCount++
                        }
                    }
                    else -> {
                    }
                }
                line = bufferedReader.readLine()
            }
            Log.d(TAG, "Full wake lock count is [$fullWakeLockCount]")
            Log.d(TAG, "Partial wake lock count is [$partialWakeLockCount]")
            appStats.setFullWakeLocks(fullWakeLockCount)
            appStats.setPartialWakeLocks(partialWakeLockCount)
            return appStats
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't get battery stats, got error: " + e.message)
            return null
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }
}
