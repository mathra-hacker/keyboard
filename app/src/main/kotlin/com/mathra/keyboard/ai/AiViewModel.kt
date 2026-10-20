package com.mathra.keyboard.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AiViewModel(app: Application) : AndroidViewModel(app) {

    private val assistant = AiAssistant(app)

    val resultLiveData = MutableLiveData<AiAssistant.AiResult?>()
    val isLoading = MutableLiveData(false)

    fun improve(text: String) = request { assistant.improveWriting(text) }
    fun fixGrammar(text: String) = request { assistant.fixGrammar(text) }
    fun toSinhala(text: String) = request { assistant.translateToSinhala(text) }
    fun toEnglish(text: String) = request { assistant.translateToEnglish(text) }

    private fun request(block: suspend () -> AiAssistant.AiResult) {
        isLoading.value = true
        resultLiveData.value = null
        viewModelScope.launch {
            resultLiveData.value = block()
            isLoading.value = false
        }
    }
}
