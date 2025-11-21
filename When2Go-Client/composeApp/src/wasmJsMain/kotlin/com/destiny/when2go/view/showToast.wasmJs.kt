package com.destiny.when2go.view

import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast

actual fun showToast(content: String) {
    showToast(message = content, duration = ToastDuration.Short, gravity = ToastGravity.Top)
}