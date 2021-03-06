package com.dwwaittime.dwaittime.domain

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Theme(
        val id: String,
        val name: String,
        val facilities: Array<Facility>
)