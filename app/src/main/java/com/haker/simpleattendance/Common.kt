package com.haker.simpleattendance

import android.app.Activity

class Common {

    companion object {
        internal val mode = Activity.MODE_PRIVATE
        internal val MyPREFS = "MyPreference"

        fun getDataFromSharedPref(key: String, activity: Activity): Any {
            val mySharedPreference = activity.getSharedPreferences(MyPREFS, mode)
            return mySharedPreference.getString(key, "")!!
        }

        //we can't put any type bcoz myEditor.put----, no any type of put func
        fun putDataToSharedPref(key: String, value: Any, activity: Activity) {
            val mySharedPreference = activity.getSharedPreferences(MyPREFS, mode)
            val myEditor = mySharedPreference.edit()
            myEditor.putString(key, value.toString())
            myEditor.commit()
        }

        fun clearSharedPref(activity: Activity) {
            val mySharedPreference = activity.getSharedPreferences(MyPREFS, mode)
            val myEditor = mySharedPreference.edit()
            myEditor.clear()
            myEditor.commit()
        }
    }
}