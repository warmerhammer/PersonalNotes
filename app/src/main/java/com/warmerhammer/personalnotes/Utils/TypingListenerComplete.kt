package com.warmerhammer.personalnotes.Utils

interface TypingListenerComplete {
    fun onTypingComplete(
        idx : Int,
        value: String
    )
}