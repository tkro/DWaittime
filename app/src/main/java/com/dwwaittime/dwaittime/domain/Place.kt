package com.dwwaittime.dwaittime.domain

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Place(
        val tdl_A : Array<Theme>,
        val tds_A : Array<Theme>,
        val tdl_R : Array<Theme>,
        val tds_R : Array<Theme>
)