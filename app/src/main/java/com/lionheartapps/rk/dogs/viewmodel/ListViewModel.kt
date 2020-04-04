package com.lionheartapps.rk.dogs.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.lionheartapps.rk.dogs.model.DogBreed
import com.lionheartapps.rk.dogs.model.DogDatabase
import com.lionheartapps.rk.dogs.model.DogsApiService
import com.lionheartapps.rk.dogs.util.NotificationsHelper
import com.lionheartapps.rk.dogs.util.SharedPreferenceHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

class ListViewModel(application: Application):  BaseViewModel(application) {

    private var prefHelper = SharedPreferenceHelper(getApplication())
    private var refreshTime =  5 * 60 * 1000 * 1000 * 1000L

    private val  dogsService = DogsApiService()
    private val disposable = CompositeDisposable()

    val dogs = MutableLiveData<List<DogBreed>>()
    val dogsLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh(){
        checkCacheDuration()
        val updateTime = prefHelper.getUpdateTime()

        if(updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        }else {
            fetchFromRemote()
        }


    }

    private fun checkCacheDuration(){
        val cachePref = prefHelper.getCacheDuration()

        try {
            val cachePrefInt = cachePref?.toInt() ?: 5 * 60
            refreshTime = cachePrefInt.times( 1000 * 1000 * 1000L)

        }catch (e: NumberFormatException){
            e.printStackTrace()
        }
    }

    fun refreshBypassCache(){
        fetchFromRemote()
    }

    private fun fetchFromDatabase(){
        loading.value = true
        launch {
            val dogs: List<DogBreed> = DogDatabase(getApplication()).dogDao().getAllDogs()
            dogsRetrieved(dogs)

            Toast.makeText(getApplication(), "Dogs retrieved from database", Toast.LENGTH_LONG).show()

        }
    }

    private fun fetchFromRemote(){
        loading.value = true
        disposable.add(
            dogsService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<DogBreed>>(){

                    override fun onSuccess(dogList: List<DogBreed>) {
                        storeDogLocally(dogList) // Store data locally
                        Toast.makeText(getApplication(), "Dogs retrieved from endpoint", Toast.LENGTH_LONG).show()
                        NotificationsHelper(getApplication()).createNotification()
                    }

                    override fun onError(e: Throwable) {
                        loading.value = false
                        dogsLoadError.value = true
                        e.printStackTrace()

                    }

                })
        )
    }

    private fun  dogsRetrieved(dogList: List<DogBreed>){
        dogs.value = dogList
        dogsLoadError.value = false
        loading.value = false
    }

    private fun storeDogLocally(list: List<DogBreed>){
        launch {
            val dao = DogDatabase(getApplication()).dogDao()
                dao.deleteAllDogs()
            val result: List<Long> = dao.insertAll(*list.toTypedArray())
            var i = 0
            while(i < list.size){
                list[i].uuid = result[i].toInt()
                ++i
            }
            dogsRetrieved(list)

        }
        prefHelper.saveUpdateTime(System.nanoTime())

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}