package com.med.domain.models

class UserLoginParam(
    var login: String = "",
    var password: String = "",
    var departament: String? = null,
    var biometricIn: Boolean = false,
    var autoIn: Boolean = false,
    var notificationOn: Boolean = true
)