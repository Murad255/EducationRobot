package com.med.data.storage.model


class PreferenceData  (
    val userId: Int,

    var login: String,
    var password: String,

    val firstName: String,
    val lastName: String,
    var departments: List<String>,
    var groups: List<String>,
    var biometricIn: Boolean,
    var autoIn: Boolean,
    var notificationOn: Boolean

)
