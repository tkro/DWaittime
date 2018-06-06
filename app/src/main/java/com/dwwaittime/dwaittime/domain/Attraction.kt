package com.dwwaittime.dwaittime.domain

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Attraction(
        val code: String,
        val name: String
)