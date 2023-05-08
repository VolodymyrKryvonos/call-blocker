package com.call_blocker.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.call_blocker.db.Preference
import com.call_blocker.repository.RepositoryImp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryImp.taskRepository.preference = Preference(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}