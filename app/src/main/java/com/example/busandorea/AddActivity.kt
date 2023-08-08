package com.android_firebase_test0802

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.busandorea.MyApplication
import com.example.busandorea.R
import com.example.busandorea.adapter.dateToString
import com.example.busandorea.databinding.ActivityAddBinding
import java.io.File
import java.util.Date

class AddActivity : AppCompatActivity() {
    // 전역으로 , 기본 바인딩 , 뷰 객체 모두 모음.
    lateinit var binding: ActivityAddBinding
    // 파일 경로를 전역으로 설정해서, 갤러리에서 사진을 선택 후, 해당 파일의 절대 경로를 저장하는 파일.
    lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩 변환 후, 인플레이트를 이용해서, 출력 객체 초기화.
        binding= ActivityAddBinding.inflate(layoutInflater)
        // 실제 화면에 출력하는 함수. 레이아웃을 그려줌.
        setContentView(binding.root)

    }

    // 인텐트를 이용해서, 후처리를 하는 코드 -> ActivityResultContracts.StartActivityForResult()
    // 사진 선택후 돌와 왔을 때, 후처리를 하는 코드.
    val requestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    {
        // it 해당 정보를 담은 객체.
        // 안드로이드 버전의 ok, Http status 200  과 동일한 기능
        if(it.resultCode === android.app.Activity.RESULT_OK){
            Glide
                .with(getApplicationContext())
                // 선택한 이미지를 불러오는 역할
                .load(it.data?.data)
                // 출력 사진의 크기
                .apply(RequestOptions().override(250, 200))
                // 사진의 크기를 조정해준다.
                .centerCrop()
                // 불러온 이미지를 결과뷰에 출력.
                .into(binding.addImageView)

            // 커서 부분은 해당, 이미지의 URI 경로로 위치를 파악하는 구문.
            // 이미지의 위치가 있는 URI 주소,
            // MediaStore.Images.Media.DATA : 이미지의 정보
            val cursor = contentResolver.query(it.data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null);
            cursor?.moveToFirst().let {
                //cursor?.getString(0) as String -> 경로 주소.
                // 만약, 궁금하면, 이부분을 로그캣에 찍어서 확인 가능함.
                filePath=cursor?.getString(0) as String
            }
        }
    }
    // 액션바의 메뉴 구성 옵션.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }


    // 액션바의 메뉴 이벤트 리스너
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId === R.id.menu_add_gallery){
            //Intent.ACTION_PICK -> 갤러리 사진 선택으로 이동.
            val intent = Intent(Intent.ACTION_PICK)
            // 인텐트 옵션에서, 액션 문자열은 , 이미지를 선택 후, URI 를 가져오는
            // 데이터 타입, MiME Type , 모든 이미지.
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            // 인텐트 옵션에서, 액션 문자열은 , 이미지를 선택 후, URI 를 가져오는
            // 데이터 타입, MiME Type , 모든 이미지.
            requestLauncher.launch(intent)
        }else if(item.itemId === R.id.menu_add_save){
            if(binding.addImageView.drawable !== null && binding.addEditView.text.isNotEmpty()){
                //store 에 먼저 데이터를 저장후 document id 값으로 업로드 파일 이름 지정
                saveStore()
            }else {
                Toast.makeText(this, "데이터가 모두 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }
    // // 파이어베이스 스토어에 저장하는 기능의 함수.....................
    private fun saveStore(){
        //맵 객체에 키, 값의 형태로 데이터를 data 변수에 담았음.
        val data = mapOf(
            // 인증된 유저의 이메일를 의미.
            // 앱이 시작시 인증을 체크하는 MyApplication의 checkAuth() 확인함.
            "email" to MyApplication.email,
            // 뷰에서 입력된 값
            "content" to binding.addEditView.text.toString(),
            "date" to dateToString(Date())
        )
        // MyApplication -> db -> 파이어 스토어를 사용하기위한 객체.
        // collection -> 컬렉션을 생성하는 함수 매개변수로 컬렉션 명,(임의로 지정가능.)
        MyApplication.db.collection("news")
            // add 부분에 , 임의로 만든 data 를 추가.
            .add(data)
            // 파이어 스토어에 데이터를 저장을 잘 했을 시 , 동작하는 함수.
            // 일반 데이터(문자열) 파이어 스토어 저장이 잘되었을 때만.
            // 이미지를 스토리지에 저장하는 구조.
            .addOnSuccessListener {
                // 데이터 추가 실패시 , 실행되는 로직.
                uploadImage(it.id)
            }
            .addOnFailureListener{
                Log.d("kkang", "data save error", it)
            }

    }
    // 스토리지 기능 중. 업로드.
    private fun uploadImage(docId: String){
        //add............................
        // MyApplication -> 스토리지 사용하기 위한 객체.
        val storage = MyApplication.storage
        // 스토리지 객체에서 reference 를 이용해서, 해당 객체를 바인딩.
        val storageRef = storage.reference
        // imgRef 라는 객체로 업로드 및 다운로드를 실행하는데, 여기서는 업로드 부분.
        //child -> 상위 폴더, images 하위에 이미지 파일이 저장되는 구조.
        val imgRef = storageRef.child("images/${docId}.jpg")
        // 후처리 코드에서, 선택된 사진의 절대경로를 file라고 하는 참조형 변수에 할당.
        val file = Uri.fromFile(File(filePath))
        // imgRef 의 기능중, putFile 경로의 파일을 업로드 하는 기능.
        imgRef.putFile(file)
            // 이미지 업로드가 성공 했다면 수행되는 로직.
            .addOnSuccessListener {
                Toast.makeText(this, "save ok..", Toast.LENGTH_SHORT).show()
                // AddActivity 수동으로 종료. 생명주기로 치면, onDestroy()
                finish()
            }
            // 실패시 수행할 로직.
            .addOnFailureListener{
                Log.d("kkang", "file save error", it)
            }

    }
}