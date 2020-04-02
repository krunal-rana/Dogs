package com.lionheartapps.rk.dogs.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.lionheartapps.rk.dogs.model.DogBreed
import com.lionheartapps.rk.dogs.model.DogDatabase
import kotlinx.coroutines.launch

class DetailsViewModel(application: Application) : BaseViewModel(application) {

    val dogLiveData = MutableLiveData<DogBreed>()

    fun fetch(uuid: Int) {

        launch {
            val dog = DogDatabase(getApplication()).dogDao().getDog(uuid)

            dogLiveData.value = dog
        }
    }
}