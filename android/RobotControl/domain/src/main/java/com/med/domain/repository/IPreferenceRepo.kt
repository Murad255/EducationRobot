package com.med.domain.repository

import com.med.domain.models.PreferenceDataAnswer

interface IPreferenceRepo {

    fun GetPreference(): PreferenceDataAnswer
    fun SetPreference(preferenceDataAnswer: PreferenceDataAnswer)

}