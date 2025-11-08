package com.example.angelitord.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitOfWork @Inject constructor(
    val angelitoRepository: AngelitoRepository)