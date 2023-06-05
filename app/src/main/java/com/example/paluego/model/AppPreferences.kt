package com.example.paluego.model

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun getString(context: Context, key: String, defaultValue: String): String {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun setString(context: Context, key: String, value: String) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun setInt(context: Context, key: String, value: Int) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun hasPreferences(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.all.isNotEmpty()
    }

    fun removePreference(context: Context, key: String) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun clearAllPreferences(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

}