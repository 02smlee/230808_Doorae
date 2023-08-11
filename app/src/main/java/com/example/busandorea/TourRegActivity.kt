package com.example.busandorea


import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.busandorea.databinding.ActivityTourRegBinding
import java.io.File
import java.util.Date

class TourRegActivity : AppCompatActivity() {

    lateinit var binding: ActivityTourRegBinding

    //파일 경로를 전역으로 설정, 갤러리에서 사진을 선택 후, 해당 파일의 절대 경로를 저장
    lateinit var  filePath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTourRegBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            try {
                //갤러리에서 선택된 이미지를 받아서, 처리 합니다.
                Log.d("kkang", "응답은 받음. ")
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize)
                )
                Log.d("kkang","원본의 사진을 얼마나 줄일 지 비율값(calRatio):$calRatio  ")
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio
                // Glide 라는 라이브러리 이미지 처리를 더 많이 할 예정.


                Log.d("kkang", "inputStream 하기전")
                // 파일 입력 출력. 아래 코드.
                // 사진을 바이트 단위로 읽었음. inputStream : 이미지의 바이트 단위의 결과값
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                Log.d("kkang", "inputStream 하기후")
                //decodeStream : 바이트로 읽어서 실제 이미지의 타입으로 변환. 단위 bitmap로 변환.
                // bitmap 안드로이드 사용하는 이미지 단위이고, 보통, 네트워크, 파일 io 할 때 자주 이용됨.
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null
                //사진 -> 바이트 읽어서 -> inputStream -> decodeStream -> bitmap -> 뷰에 출력.
                // 이미지 , 영상 관련 인코딩 관심 있으면,
                // 작업 한 깃 주소 :
                //https://github.com/lsy3709/travel_sample_app_spring_firebase/tree/master/app/src/main/java/com/android4/travel/Memo
                bitmap?.let {
                    Log.d("kkang", "결과 뷰에 적용 전")
                    // 결과 뷰에 갤러리에서 가져온 사진을 할당 부분.
                    binding.addImageView.setImageBitmap(bitmap)
                    Log.d("kkang", "결과 뷰에 적용 후")
                } ?: let{
                    Log.d("kkang", "bitmap null")
                }
            }catch (e: Exception){
                Log.d("kkang", "응답 시작 부터 오류")
                e.printStackTrace()
            }
        }


        binding.btnPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        binding.btnRegTour.setOnClickListener {
            saveStore()
            requestLauncher.launch(intent)
        }
    }

    // 파이어베이스 스토어에 저장하는 기능의 함수.
    private fun saveStore(){
        //add............................
        // 키와 값의 형태로 data 변수에 할당
        val data = mapOf(
            // 인증된 유저의 이메일
            // 앱이 시작시 인증을 체크하는 MyApplication의 checkAuth() 호출
            "email" to MyApplication.email,
            // 뷰에서 입력된 값
            "maintitle" to binding.txtMainTitle.text.toString(),
            "title" to binding.txtTitle.text.toString(),
            "content" to binding.txtContent.text.toString()
            //"date" to dateToString(Date())
        )

        // MyApplication -> db -> 파이어 스토어를 사용하기위한 객체.
        // collection -> 컬렉션을 생성하는 함수 매개변수로 컬렉션 명,(임의로 지정가능.)
        MyApplication.db.collection("news")
            // add 부분에 , 임의로 만든 data 를 추가.
            .add(data)
            // 파이어 스토어에 데이터를 저장을 잘 했을 시 , 동작하는 함수.
            .addOnSuccessListener {
                // 일반 데이터(문자열) 파이어 스토어 저장이 잘되었을 때만.
                // 이미지를 스토리지에 저장하는 구조.
                uploadImage(it.id)
            }
            .addOnFailureListener{
                // 데이터 추가 실패시 , 실행되는 로직.
                Log.d("kkang", "data save error", it)
            }

    }

    // 스토리지 기능 중. 업로드.
    private fun uploadImage(us_seq: String){
        // 매개변수 부분은, 글 작성시, docId 라고, 문서번호(자동생성) 예) 5Ju6dQ9crjs401U9PbkJ

        //add............................
        // MyApplication -> 스토리지 사용하기 위한 객체.
        val storage = MyApplication.storage
        // 스토리지 객체에서 reference 를 이용해서, 해당 객체를 [바인딩]
        val storageRef = storage.reference
        // imgRef 라는 객체로 업로드 및 다운로드를 실행 : 업로드
        //child -> 상위 폴더, images 하위에 이미지 파일이 저장되는 구조.
        val imgRef = storageRef.child("images/${us_seq}.jpg")

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
            .addOnFailureListener{
                Log.d("kkang", "file save error", it)
            }

    }

    //사진 선택 후 후처리하는 코드
    val requestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    {
        // it 해당 정보를 담은 객체.
        // 안드로이드 버전의 ok, Http status 200  과 동일한 기능.
        if(it.resultCode === android.app.Activity.RESULT_OK){
            // 가져온 이미지를 처리를 글라이드를 이용.
            Glide
                .with(getApplicationContext())
                // 선택한 이미지를 불러오는 역할
                .load(it.data?.data)
                .apply(RequestOptions().override(250, 200))
                .centerCrop()
                // 불러온 이미지를 결과뷰에 출력.
                .into(binding.addImageView)

            // 커서 부분은 해당, 이미지의 URI 경로로 위치를 파악하는 구문.
            // 이미지의 위치가 있는 URI 주소,
            // MediaStore.Images.Media.DATA : 이미지의 정보
            val cursor = contentResolver.query(it.data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null);
            cursor?.moveToFirst().let {
                //cursor?.getString(0) as String -> 경로 주소. : 공유 미디어 서버 위치
                filePath=cursor?.getString(0) as String
            }
        }
    }

    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        //비트맵 객체 그대로 사용하면, 사진 원본을 그대로 사용해서 메모리 부족 현상 생김.
        // 그래서, 옵션이라는 속성을 사용.
        val options = BitmapFactory.Options()
        // 실제 비트맵 객체를 생성하는 것 아니고, 옵션 만 설정하겠다라는 의미.
        options.inJustDecodeBounds = true
        try {
            // 실제 원본 사진의 물리 경로에 접근해서, 바이트로 읽음.
            // 사진을 읽은 바이트 단위.
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            // 읽었던 원본의 사진의 메모리 사용은 반납.
            inputStream!!.close()
            // 객체를 null 초기화,
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        //height ,width 원본의 가로 세로 크기.
        // reqHeight, reqWidth 원하는 크기 사이즈,
        // 이것보다 크면 원본의 사이즈를 반으로 줄이는 작업을 계속 진행.
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // 액션바의 메뉴 구성 옵션.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }




}