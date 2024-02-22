package com.med.data.storage

import com.ctc.wstx.shaded.msv_core.writer.relaxng.Context
import com.med.domain.models.*
import java.util.function.Predicate

interface IUserStorage {

    fun AddHandler(handler: IMessageHandler?)

    suspend fun find(user: UserLoginParam): UserLoginAnswer?
    fun sentLoginRequest(user: UserLoginParam): Boolean?
    fun writeParams(user: UserLoginParam): Boolean
    fun isConnectToStorage():Boolean

    /** Подписаться на обновление данных */
    fun subscribeTodataUpdate(dataType: String,dataId: Long)
    fun subscribeTodataUpdate(dataType: String)

        //fun sentDepartmentDataRequest(department:String)

}