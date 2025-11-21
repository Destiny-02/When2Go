package com.destiny.when2go.model

data class DepartureInfo(
    val travellingTo: String,
    val departingIn: Long,
    val isRealtime: Boolean = false,
    val leaveIn: Long
)