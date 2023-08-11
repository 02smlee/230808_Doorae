package com.example.busandorea.model

data class ItemTour(
    //컬렉션(테이블처럼 사용), 문서(행), use_seq(문서의 번호 -> pk처럼 사용)
    //문서번호, 자동으로 생성해서 사용
    var us_seq : String,
    var main_title : String,
    var lat : Int,
    var lng : Int,
    var title : String,
    var main_img_normal : String,
    var main_img_thumb : String,
    var itemcntnts : String,
    //인증이되면, 해당 이메일이 인증 객체에 등록
    var member_email : String
)
