package com.destiny.when2go.idl

import kotlinx.serialization.Serializable

@Serializable
data class NearestLrDeparturesRequest(
    val coord: Coord,
    val stop_coord: Coord
)