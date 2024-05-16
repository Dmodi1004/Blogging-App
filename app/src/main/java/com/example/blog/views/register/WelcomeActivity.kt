package com.example.blog.views.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.blog.databinding.ActivityWelcomeBinding
import com.example.blog.views.MainActivity
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }

    private lateinit var intent: Intent

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(2000)
        installSplashScreen()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.apply {
            loginBtn.setOnClickListener {
                intent = Intent(this@WelcomeActivity, SignInAndRegistrationActivity::class.java)
                intent.putExtra("action", "login")
                startActivity(intent)
            }

            registerBtn.setOnClickListener {
                intent = Intent(this@WelcomeActivity, SignInAndRegistrationActivity::class.java)
                intent.putExtra("action", "register")
                startActivity(intent)
            }
        }

    }

    override fun onStart() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        super.onStart()
    }

}