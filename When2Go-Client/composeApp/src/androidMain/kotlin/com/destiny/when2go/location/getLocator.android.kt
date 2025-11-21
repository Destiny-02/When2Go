package com.destiny.when2go.location

import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile

actual fun getLocator(): Locator = Locator.mobile()