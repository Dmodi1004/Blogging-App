package com.example.blog.views.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blog.databinding.ActivitySignInAndRegistrationBinding
import com.example.blog.models.UserData
import com.example.blog.views.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInAndRegistrationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }

    private lateinit var registerName: String
    private lateinit var registerEmail: String
    private lateinit var registerPassword: String

    private lateinit var loginEmail: String
    private lateinit var loginPassword: String

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        val action = intent.getStringExtra("action")

        binding.apply {

            if (action == "login") {
                loginBtn.visibility = View.VISIBLE
                loginEmailEdt.visibility = View.VISIBLE
                loginPasswordEdt.visibility = View.VISIBLE

                registerNameEdt.visibility = View.GONE
                registerEmailEdt.visibility = View.GONE
                registerPasswordEdt.visibility = View.GONE
                materialCardView.visibility = View.GONE


                registerBtn.isEnabled = false
                registerBtn.alpha = 0.5f
                registerNewHere.isEnabled = false
                registerNewHere.alpha = 0.5f

                loginBtn.setOnClickListener {
                    loginEmail = loginEmailEdt.text.toString()
                    loginPassword = loginPasswordEdt.text.toString()

                    if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                        Toast.makeText(
                            this@SignInAndRegistrationActivity,
                            "Please fill all the details",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        loginUser()
                    }
                }

            } else if (action == "register") {
                loginBtn.isEnabled = false
                loginBtn.alpha = 0.5f

                registerBtn.setOnClickListener {
                    registerName = registerNameEdt.text.toString()
                    registerEmail = registerEmailEdt.text.toString()
                    registerPassword = registerPasswordEdt.text.toString()

                    if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                        Toast.makeText(
                            this@SignInAndRegistrationActivity,
                            "Please fill all the details",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        registerUser()
                    }
                }
            }

            materialCardView.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    PICK_IMAGE_REQUEST
                )
            }

        }
    }

    private fun loginUser() {

        binding.loading.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(loginEmail, loginPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successfully 🎉", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Login Failed, Please enter correct details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.loading.visibility = View.GONE
            }
    }

    private fun registerUser() {

        binding.loading.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    auth.signOut()

                    user?.let {
                        val userReference = database.getReference("users")
                        val userId = user.uid
                        val userData = UserData(
                            registerName,
                            registerEmail
                        )
                        userReference.child(userId).setValue(userData)

                        val storageReference =
                            storage.reference.child("profile_image/$userId.jpg")
                        storageReference.putFile(imageUri!!).addOnCompleteListener { task ->
                            storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                val imageUrl = imageUri.result.toString()

                                userReference.child(userId).child("profileImage").setValue(imageUrl)

                            }
                        }

                        Toast.makeText(this, "Register Successfully", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, WelcomeActivity::class.java))
                        finish()
                    }

                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
                binding.loading.visibility = View.GONE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {

            imageUri = data.data

            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.registerUserImage)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}