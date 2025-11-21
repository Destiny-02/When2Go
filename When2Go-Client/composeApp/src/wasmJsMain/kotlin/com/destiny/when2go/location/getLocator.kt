package com.destiny.when2go.location

import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.browser.browser

actual fun getLocator(): Locator = Locator.browser()