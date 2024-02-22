package com.med.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.med.data.storage.model.PreferenceData

/**
 * [PreferenceStorage] сохраняет и загружает данные пользователя
 */
class PreferenceStorage(var preferences: SharedPreferences) {
    private var KEY = "PERSON_DATA"

    fun SetPreference( data: PreferenceData){
        // Редактируем настройки
        val editor = preferences.edit()
        // Сериализуем объект в виде строки
        val gson = Gson()
        val personJson = gson.toJson(data)
        // Редактируем настройки
        editor.putString(KEY, personJson)
        editor.apply()
    }

    fun GetPreference(): PreferenceData? {
        // Редактируем настройки
        val personJson = preferences.getString(KEY, "")

       try {
           val gson = Gson()
           val person = gson.fromJson(personJson, PreferenceData::class.java)
           return person
       }
       catch (ex:Exception){
           return null
       }
    }


}
