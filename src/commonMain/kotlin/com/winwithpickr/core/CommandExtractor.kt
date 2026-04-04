package com.winwithpickr.core

import com.winwithpickr.core.models.ParsedCommand

interface CommandExtractor {
    suspend fun extract(text: String, botHandle: String): ParsedCommand?
}
