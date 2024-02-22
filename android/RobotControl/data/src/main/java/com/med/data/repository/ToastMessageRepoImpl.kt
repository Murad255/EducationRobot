package com.med.data.repository

import android.content.Context
import android.widget.Toast
import com.med.domain.repository.IToastMessageRepo

class ToastMessageRepoImpl(val context: Context): IToastMessageRepo {
    override fun ShowToastMesage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}