package com.joegruff.decredaddressscanner.types

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.joegruff.decredaddressscanner.R


class MenuItems(private val act: AppCompatActivity) {
    fun prepareOptionsMenu(menu: Menu?, ctx: Context) {
        if (menu != null) {
            val mainNet: MenuItem = menu.findItem(R.id.menu_dcrdata_mainnet)
            mainNet.isChecked = false
            val testNet: MenuItem = menu.findItem(R.id.menu_dcrdata_testnet)
            testNet.isChecked = false
            val other: MenuItem = menu.findItem(R.id.menu_dcrdata_other)
            other.isChecked = false
            when (UserSettings.get(ctx).url()) {
                dcrdataMainNet -> {
                    mainNet.isChecked = true
                }
                dcrdataTestNet -> {
                    testNet.isChecked = true
                }
                else -> {
                    other.isChecked = true
                }
            }
        }
    }

    fun optionsItemSelected(item: Int, ctx: Context): Boolean {
        when (item) {
            R.id.menu_dcrdata_mainnet -> UserSettings.get(act.applicationContext).setUrl(
                dcrdataMainNet
            )
            R.id.menu_dcrdata_testnet -> UserSettings.get(act.applicationContext).setUrl(
                dcrdataTestNet
            )
            R.id.menu_dcrdata_other -> other(ctx)
            R.id.menu_about -> about()
            else -> return false
        }
        return true
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
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    private fun other(ctx: Context) {
        val url = UserSettings.get(ctx).url()
        val editText = EditText(ctx)
        editText.setText(url)
        AlertDialog.Builder(this.act)
            .setTitle(R.string.menu_other_description)
            .setView(editText)
            .setPositiveButton(R.string.ok) { _, _ ->
                var input = editText.text.toString()
                if (input == "") return@setPositiveButton
                // TODO: Validate better.
                if (input[input.length - 1] != '/') {
                    input += "/"
                }
                UserSettings.get(ctx).setUrl(input)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setCancelable(false)
            .show()
    }
}
