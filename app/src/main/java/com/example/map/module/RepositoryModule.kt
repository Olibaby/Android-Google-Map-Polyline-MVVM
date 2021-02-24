package com.example.map.module

import com.example.map.SetLocationUpdate
import com.example.map.repository.HomeRepository
import com.example.map.repository.HomeRepositoryImpl
import com.qucoon.rubiescircle.utils.SingleLiveEvent
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val repoModule = module {

    single { SetLocationUpdate(SingleLiveEvent(),SingleLiveEvent(), String(), String(), SingleLiveEvent(), SingleLiveEvent()) }

    single<HomeRepository> { HomeRepositoryImpl(directionsAPI = get()) }

}