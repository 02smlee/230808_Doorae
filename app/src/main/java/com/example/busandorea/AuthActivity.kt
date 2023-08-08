package com.example.busandorea

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.busandorea.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity(){
    lateinit var binding: ActivityAuthBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // MyApplication -> checkAuth => 로그인이 되었다면,
        if(MyApplication.checkAuth()){
            // 아래에 changeVisibility 함수로 정의가 되었고, 각 모드마다 보여지는 뷰가 다르다.
            changeVisibility("login")
        }else {
            changeVisibility("logout")
        }

        // 인텐트로 후처리 하는 코드.
        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            //구글 로그인 결과 처리...........................
            // 구글 로그인 후 처리.
            // it.data 구글로 인증된 정보가 들어 있음
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                // 여기서 MyApplication.auth 인증 객체, 인증 수단( 등록된 이메일, 구글 인증...)
                MyApplication.auth.signInWithCredential(credential)
                    // 구글 인증으로 성공 후 실행할 로직.
                    .addOnCompleteListener(this){ task ->
                        if(task.isSuccessful){
                            // 구글인증이 된 이메일의 현재앱의  로그인된 email 재할당하는 부분.
                            MyApplication.email = account.email
                            // changeVisibility , 로그임 모드 맞게끔 뷰 구성을 변경.
                            changeVisibility("login")
                        }else {
                            changeVisibility("logout")
                        }
                    }
            }catch (e: ApiException){
                changeVisibility("logout")
            }
        }

        binding.logoutBtn.setOnClickListener {
            //로그아웃...........
            // 인증 객체에서 사용하는 함수. 로그아웃기능.
            MyApplication.auth.signOut()
            // 이메일 널로 할당.
            MyApplication.email = null
            changeVisibility("logout")
        }

        binding.goSignInBtn.setOnClickListener{
            changeVisibility("signin")
        }

        binding.googleLoginBtn.setOnClickListener {
            //구글 로그인....................
            // 구글 로그인 관련 함수. 옵션 부분을 설정.
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //R.string.default_web_client_id : 처음 빌드 때, 컴파일 오류가 발견.
                // 인증 후, 안보임. 왜? 인증된 아이디를 가져와서 사용해서.
                .requestIdToken(getString(R.string.default_web_client_id))
                // DEFAULT_SIGN_IN -> 파이어베이스 콘솔에서, 지정함.
                .requestEmail()
                // 옵션 객체에 담아두면.
                .build()
            // gso GoogleSignIn.getClient 함수에 매개변수로 사용하면 됩니다.
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            // 실제 후처리 인텐트 호출하는 함수.
            requestLauncher.launch(signInIntent)
        }

        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입........................
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                // 이메일 잘 등록된 후 수행되는 코드.
                .addOnCompleteListener(this){task ->
                    // 뷰에 비워주기.
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener{ sendTask ->
                                // 전송이 잘 되었다면 수행되는 코드.
                                if(sendTask.isSuccessful){
                                    // 인증된 이메일이 존재한다면, 그리고 나서, 인증 메일을 등록할 메일로 전송.
                                    Toast.makeText(baseContext, "회원가입에서 성공, 전송된 메일을 확인해 주세요",
                                        Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }else {
                                    Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }
                            }
                    }else {
                        Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }
                }

        }

        binding.loginBtn.setOnClickListener {
            //이메일, 비밀번호 로그인.......................
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            // 인증 객체 함수중에서, 로그인 처리하는 함수
            // signInWithEmailAndPassword
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    // 유효성 체크가 성공하면 수행할 로직.
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        if(MyApplication.checkAuth()){
                            // 앱이 실행시 체크하는 인증 함수
                            // 인증된 이메일에 시스템에 등록.(세션 사용하는 것 처럼.)
                            MyApplication.email = email
                            changeVisibility("login")
                        }else {
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show()

                        }
                    }else {
                        Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }


    }

    // 매개변수를 모드라는 변수명, 문자열 타입.
    fun changeVisibility(mode: String){
        if(mode === "login"){
            binding.run {
                // 인증된 이메일 부분
                authMainTextView.text = "${MyApplication.email} 님 반갑습니다."
                // 뷰를 show & hide
                // 로그아웃 버튼은 보이게
                logoutBtn.visibility= View.VISIBLE
                // 회원가입 버튼 안보이게
                goSignInBtn.visibility= View.GONE
                googleLoginBtn.visibility= View.GONE
                authEmailEditView.visibility= View.GONE
                authPasswordEditView.visibility= View.GONE
                signBtn.visibility= View.GONE
                loginBtn.visibility= View.GONE
            }

        }else if(mode === "logout"){
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
            }
        }else if(mode === "signin"){
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
            }
        }
    }
}