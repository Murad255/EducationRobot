package com.med.data.repository

import com.med.data.storage.IUserStorage
import com.med.domain.models.UserLoginAnswer
import com.med.domain.models.UserLoginParam
import com.med.domain.repository.IUserRepository

class UserRepositoryImpl ( val userStorage: IUserStorage): IUserRepository {

    override suspend fun findUser(userParam: UserLoginParam): UserLoginAnswer? {
        return userStorage.find(userParam)
    }

    override  fun sentLoginRequest(userParam: UserLoginParam): Boolean? {
        return userStorage.sentLoginRequest(userParam)
    }

    override fun isConnect(): Boolean = userStorage.isConnectToStorage()
}