package com.example.tracker.ui

sealed class Route {
    data object Home : Route()
    data class SessionDetail(val id: String) : Route()
    data class Recipient(val id: String) : Route()
}
