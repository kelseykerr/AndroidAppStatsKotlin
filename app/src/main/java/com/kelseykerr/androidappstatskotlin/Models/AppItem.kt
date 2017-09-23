package com.kelseykerr.androidappstatskotlin.Models

import android.graphics.drawable.Drawable

/**
 * Created by kelseykerr on 9/23/17.
 */
class AppItem(var appName: String, var packageName: String, var appIcon: Drawable) : Comparable<AppItem> {

    override fun compareTo(appItem: AppItem): Int {
        return this.appName!!.compareTo(appItem.appName!!)
    }

}