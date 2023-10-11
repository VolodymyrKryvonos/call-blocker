package com.call_blocker.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.call_blocker.app.ui.HolderActivity

class SplashActivity : AppCompatActivity() {
    // Can't understand why with HolderActivity
    // set as launch activity app don't work
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, HolderActivity::class.java))
        finish()
    }
}