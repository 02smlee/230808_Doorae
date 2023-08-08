package com.example.busandorea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.busandorea.databinding.ActivityMainBinding
import com.example.busandorea.fragment.PublicFragment
import com.example.busandorea.fragment.StampeFragment
import com.example.busandorea.fragment.TourListFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityMainBinding

    //프레그먼트 정의
    class MyFragmentPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        val fragments: List<Fragment>

        init {
            fragments = listOf(TourListFragment(), PublicFragment(), StampeFragment())
        }

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //view 변수 추가
        val view = binding.root
        setContentView(view)


        //toggle 객체를 이용하여 액션 바를 설정
//        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.drawer_opened, R.string.drawer_closed)
//        binding.drawer.addDrawerListener(toggle)
//        toggle.syncState()

        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.drawer_opened,
            R.string.drawer_closed
        )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        // Fragment 뷰 바인딩
        val adapter = MyFragmentPagerAdapter(this)
        binding.viewpager.adapter = adapter
        //탭의 이름을 각각 다르게 입력하기
        TabLayoutMediator(binding.tabs, binding.viewpager) {tab, position ->
            when(position){
                0 -> tab.text = "추천여행지"
                1 -> tab.text = "공공데이터"
                2 -> tab.text = "스템프"
            }
        }.attach()


        //Tab1, 2, 3으로 번호를 붙여서 출력
//        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
//            tab.text = "Tab${(position + 1)}"
//        }.attach()


        //네비게이션 뷰 사용을 위해 mainDrawerView에 바인딩함.
        //회원가입 바인딩
        binding.mainDrawerView.setNavigationItemSelectedListener {
            when (it.itemId ) {
                R.id.action_signin -> {    // 로그인 버튼을 클릭하면 AuthActivity 엑티비티 화면으로 전환
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                    true}

                else -> {Log.d("smlee","test : item click : ${it.title}")
            true
                }
            }
            // 로그인 바인딩
            when (it.itemId ) {
                R.id.action_login -> {    // 로그인 버튼을 클릭하면 AuthActivity 엑티비티 화면으로 전환
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                    true}

                else -> {Log.d("smlee","test : item click : ${it.title}")
                    true
                }
            }
        }

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_login-> {
                // 로그인 버튼을 클릭하면 AuthActivity 화면으로 전환
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                true
            }
//            R.id.stampfragment -> {
//                replaceFragment(StampeFragment())
//                return true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }
//    private fun replaceFragment(fragment: Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, fragment)
//            .addToBackStack(null)
//            .commit()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 액티비티에서 메뉴를 사용하려면 onCreateOptionsMenu 메서드를 오버라이드
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //이벤트가 toggle 버튼에서 제공된거라면..
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)

    }
}


