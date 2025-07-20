package com.streaker.controller.user.dto

import java.util.UUID

class UserDto(
    val uuid: UUID,
    val username: String,
    val email: String
) {}