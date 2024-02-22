package com.med.domain.usecase

import com.med.domain.repository.IToastMessageRepo

class ShowToastMessageUseCase(private val toastMessageRepo: IToastMessageRepo)  {

    fun execute(message: String) {
        toastMessageRepo.ShowToastMesage(message)
    }
}