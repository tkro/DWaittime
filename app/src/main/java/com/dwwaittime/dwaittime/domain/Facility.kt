package com.dwwaittime.dwaittime.domain

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Facility(
        val code: String,
        val name: String
)