package com.warmerhammer.personalnotes.Utils

import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.*

class TypingListener(private val onTyping: (Boolean) -> Unit) : TextWatcher {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var typingJob: Job? = null
    private var isNotTyping = true

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (isNotTyping) {
           onTyping(isNotTyping)
           isNotTyping = false
        }
    }

    override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
        onTyping(true)
    }

    override fun afterTextChanged(s: Editable?) {
        typingJob?.cancel()
        s?.let{
            typingJob = coroutineScope.launch {
                delay(DEBOUNCE_PERIOD)
                onTyping(isNotTyping)
                isNotTyping = true
            }
        }
    }

    companion object {
        private const val DEBOUNCE_PERIOD = 1000L
    }
}