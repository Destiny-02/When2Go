package com.destiny.when2go

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import registerToastHost

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App()
        registerToastHost()
    }
}