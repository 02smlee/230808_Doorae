package com.example.busandorea.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.busandorea.MyApplication
import com.example.busandorea.databinding.ItemRecyclerviewBinding
import com.example.busandorea.model.ItemTour

//뷰홀더 -> 뷰 객체들의 모음
//해당 매개변수로 전체 뷰 객체에 접근이 가능
class MyViewHolder(val binding: ItemRecyclerviewBinding): RecyclerView.ViewHolder(binding.root)

//어댑터 : 데이터 <-> 뷰의 연결
//Context -> 액티비티, 또는 프래그먼트 형식
//datas : 실제 데이터명 (공공데이터, 임의의 개발자가 정의한 데이터)
//리사이클러뷰 구성 클래스들의 공통으로 RecyclerView 관련 부모 클래스를 상속
class MyAdapter(val context: Context, val datas: MutableList<ItemTour>): RecyclerView.Adapter<MyViewHolder>(){

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
            = MyViewHolder(ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //val binding=(holder as MyViewHolder).binding
        //binding.itemData.text= datas[position]
        val data = datas.get(position)

        holder.binding.run {
            itemMainTitle.text = data.main_title
            itemTitle.text = data.title
        }

        //매개변수로 정의된 images 상위폴더는 임의로 만든 폴더
        //'com.firebaseui:firebase-ui-storage:8.0.0' : 사용
        val imgRef = MyApplication.storage.reference.child("images/${data.us_seq}.jpg")
        imgRef.downloadUrl.addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Glide.with(context)
                    // 이미지를 불러오는 역할.
                    .load(task.result)
                    // 불러온 이미지를 ,결과 뷰에 출력하는 코드.
                    .into(holder.binding.itemImageView)
            }
        }

    }
}

class MyDecoration(val context: Context): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val index = parent.getChildAdapterPosition(view) + 1

        if (index % 3 == 0) //left, top, right, bottom
            outRect.set(10, 10, 10, 60)
        else
            outRect.set(10, 10, 10, 0)

        view.setBackgroundColor(Color.parseColor("#28A0FF"))
        ViewCompat.setElevation(view, 20.0f)

    }
}