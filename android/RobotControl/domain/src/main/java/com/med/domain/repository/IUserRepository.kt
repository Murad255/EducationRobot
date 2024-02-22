package com.med.domain.repository

import com.med.domain.models.*
import kotlin.reflect.KFunction1

interface IUserRepository {
    suspend fun findUser(userParam: UserLoginParam): UserLoginAnswer?

    /**
     * sent request for user logins. Return false if don`t sent request
     */
    fun sentLoginRequest(userParam: UserLoginParam): Boolean?

    //   suspend fun updateUserData(userParam: UserLoginParam): User?
    // fun subscribeUserDataUpdate( dataUpdateHandler: KFunction1<User, Unit>)//: MutableLiveData<UserLoginAnswer>?
    fun isConnect():Boolean
}