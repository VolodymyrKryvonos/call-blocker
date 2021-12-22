package com.call_blocke.app.screen.black_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.repository.RepositoryImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlackListViewModel : ViewModel() {

    private val settingsRepository by lazy {
        RepositoryImp.settingsRepository
    }

    val blackList = MutableLiveData<List<String>>()

    fun loadBlackList(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        blackList.postValue(settingsRepository.blackList(context))
    }

    fun removeItem(context: Context, phoneNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        settingsRepository.removeFromBlackList(context, phoneNumber)
        loadBlackList(context)
    }

}