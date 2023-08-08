package com.example.busandorea

import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MyApplication: MultiDexApplication() {
    //dex , 코틀린으로 컴파일 할 때, 추가된 파일 구조.
    // java, -> class -> dex , jvm, ART(Android Run Time)
    companion object {
        // 클래스 변수로 사용하겠다. 자바로 치면 static 비슷함.
        // lateinit 형식으로 선언이 되어있어서, 선언만 되었고,
        // 실제로 사용할려면, 초기화하는 로직이 반드시 필요함.
        // 아랫부분에 onCreate 함수에서 초기화를 진행하고 있음.
        lateinit var auth: FirebaseAuth
        var email: String? = null
        lateinit var db: FirebaseFirestore
        lateinit var storage: FirebaseStorage
        fun checkAuth(): Boolean {
            // 파이어베이스 인증의 기능을 이용하는 로직.
            // auth.currentUser -> 파이어베이스로 인증된 사용자를 가리킴.
            // 로그인 후 -> 인증된 유저를 가리킴.
            var currentUser = auth.currentUser
            return currentUser?.let {
                email = currentUser.email
                // 이메일로 유효성 체크. (존재여부.)
                currentUser.isEmailVerified
            } ?: let {
                false
            }
        }
    }

    // 위에 선언된 개체의 변수 초기화
    override fun onCreate() {
        super.onCreate()
        // 실제로, 선언된 객체에 할당을 하는 구조.
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage
    }
}