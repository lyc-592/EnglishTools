package com.lyc.englishtools

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lyc.englishtools.ui.auth.AuthManager
import com.lyc.englishtools.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取 NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 初始化导航图
        val navGraph = navController.navInflater.inflate(R.navigation.main_nav)

        // 检查登录状态 - 直接从内存中获取
        if (AuthManager.isLoggedIn()) {
            // 已登录：将起始目的地设置为 main_flow
            navGraph.setStartDestination(R.id.main_flow)
        }

        // 设置导航图
        navController.graph = navGraph

        // 配置底部导航栏
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 仅当在主流程时显示底部导航栏
            binding.bottomNav.visibility = if (destination.parent?.id == R.id.main_flow) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 在登录页面按下返回键时最小化应用
        if (navController.currentDestination?.id == R.id.loginFragment) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }
}