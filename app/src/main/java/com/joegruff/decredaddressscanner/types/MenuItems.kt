package com.joegruff.decredaddressscanner.types

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.joegruff.decredaddressscanner.R


class MenuItems(activity: AppCompatActivity){
    private val act = activity
    fun doIt(item: Int): Boolean {
        when (item) {
            //R.id.menu_choose_explorer -> chooseExplorer()
            R.id.menu_about -> about()
            else -> return false
        }
        return true
    }

    private fun chooseExplorer() {
    }

    private fun about() {
        var version = ""
        try {
            val pInfo: PackageInfo =
                this.act.packageManager.getPackageInfo(this.act.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        AlertDialog.Builder(this.act).setTitle(R.string.menu_about)
            .setMessage(this.act.getString(R.string.menu_about_details, version))
            .setCancelable(true)
            .show()
    }
}
