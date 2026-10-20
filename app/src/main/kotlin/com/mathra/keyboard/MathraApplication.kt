package com.mathra.keyboard

import android.app.Application

/**
 * MathraApplication: application class.
 * Currently minimal — provides a global context hook if needed by singletons.
 */
class MathraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // No global state initialized here.
        // ThemeManager, PrefsManager etc. are stateless singletons.
    }
}
