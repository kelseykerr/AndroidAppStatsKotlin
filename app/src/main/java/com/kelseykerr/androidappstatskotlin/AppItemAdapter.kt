package com.kelseykerr.androidappstatskotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.kelseykerr.androidappstatskotlin.Models.AppItem
import java.util.ArrayList

/**
 * Created by kelseykerr on 9/25/17.
 */
class AppItemAdapter(val mContext: Context, val resource: Int, val appItems:ArrayList<AppItem>) :
        ArrayAdapter<AppItem>(mContext, resource, appItems), View.OnClickListener {

    companion object {
        const val TAG = "AppItemAdapter"
    }

    inner class ViewHolder {
        var appIcon:ImageView? = null
        var appName: TextView? = null
        var packageName:TextView? = null
    }

    override fun onClick(v:View) {
        val position = v.getTag() as Int
        val obj = getItem(position)
    }

    var lastPosition = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val appItem = getItem(position)
        var viewHolder:ViewHolder
        viewHolder = ViewHolder()
        val inflater = LayoutInflater.from(parent.context)
        val result = inflater.inflate(R.layout.app_item, parent, false)
        viewHolder.appIcon = result.findViewById(R.id.app_icon)
        viewHolder.appName = result.findViewById(R.id.app_name)
        viewHolder.packageName = result.findViewById(R.id.package_name)
        result.setTag(viewHolder)
        val animation = AnimationUtils.loadAnimation(mContext, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top)
        result.startAnimation(animation)
        lastPosition = position
        val appName = viewHolder.appName as TextView
        appName.text = appItem.appName
        val appIcon = viewHolder.appIcon as ImageView
        appIcon.setImageDrawable(appItem.appIcon)
        val packageName = viewHolder.packageName as TextView
        packageName.text = appItem.packageName
        return result
    }
}