package com.destiny.when2go.view

/**
 * TODO:
 * Currently this is only used for one toast, so it is hardcoded due to
 * An argument for the 'js()' function must be a constant string expression.
 * Either use a toast library that supports js, or display a compose view toast.
 */
actual fun showToast(content: String) {
    js("{ alert(\"Something went wrong. Please try again. \") }")
}