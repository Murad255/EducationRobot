package com.med.data.repository

import androidx.lifecycle.MutableLiveData
import com.med.data.storage.PreferenceStorage
import com.med.data.storage.model.PreferenceData
import com.med.domain.models.PreferenceDataAnswer
import com.med.domain.models.User
import com.med.domain.models.UserLoginParam
import com.med.domain.repository.IPreferenceRepo

class PreferenceRepoImpl(val preferenceStorage: PreferenceStorage) :IPreferenceRepo{

//    val userLoginParam: MutableLiveData<UserLoginParam> = MutableLiveData()
//    val userData: MutableLiveData<User> = MutableLiveData()
    val preferenceDataAnswer: MutableLiveData<PreferenceDataAnswer> = MutableLiveData()

    init {
        LoadPreference()
    }

    override fun GetPreference(): PreferenceDataAnswer {
        return preferenceDataAnswer.value!!
    }

    override fun SetPreference(preferenceDataAnswer: PreferenceDataAnswer) {
        if (preferenceDataAnswer?.user == null || preferenceDataAnswer.userLoginParam == null)
            return
        val preferenceData = PreferenceData(
            preferenceDataAnswer.user!!.userId,
            preferenceDataAnswer.userLoginParam!!.login,
            preferenceDataAnswer.userLoginParam!!.password,
            preferenceDataAnswer.user!!.firstName,
            preferenceDataAnswer.user!!.lastName,
            ArrayList(),
            ArrayList(),
            preferenceDataAnswer.userLoginParam!!.biometricIn,
            preferenceDataAnswer.userLoginParam!!.autoIn,
            preferenceDataAnswer.userLoginParam!!.notificationOn,
            )
        preferenceStorage.SetPreference(preferenceData)
    }

    fun LoadPreference() {
        val pref = preferenceStorage.GetPreference()
        if (pref==null || pref.login.isEmpty() || pref.password.isEmpty())
            preferenceDataAnswer.value = PreferenceDataAnswer(false,UserLoginParam(),null)
        else {
            val userLoginParam = UserLoginParam(
                pref.login,
                pref.password,
                "",
                pref.biometricIn,
                pref.autoIn,
                pref.notificationOn
            )
            val userData =  User(pref.userId, pref.firstName, pref.lastName)
            preferenceDataAnswer.value =  PreferenceDataAnswer( true, userLoginParam, userData)
        }
    }
}