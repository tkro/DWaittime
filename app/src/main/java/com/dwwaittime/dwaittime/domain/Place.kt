package com.dwwaittime.dwaittime.domain

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Place(
        val tdl : Array<Theme>,
        val tds : Array<Theme>
)