package com.lyc.englishtools.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.R
import com.lyc.englishtools.data.UserDbHelper
import com.lyc.englishtools.utils.SecurityUtils
import com.lyc.englishtools.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: UserDbHelper
    private val securityUtils = SecurityUtils()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        dbHelper = UserDbHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigateUp() // 直接返回上一页（即登录页）
        }

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            when {
                username.isEmpty() -> showToast("用户名不能为空")
                password.isEmpty() -> showToast("密码不能为空")
                password != confirmPassword -> showToast("两次输入密码不一致")
                checkUsernameExists(username) -> showToast("用户名已存在")
                else -> processRegistration(username, password)
            }
        }
    }

    private fun checkUsernameExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            UserDbHelper.TABLE_USERS,
            arrayOf(UserDbHelper.COLUMN_ID),
            "${UserDbHelper.COLUMN_USERNAME} = ?",
            arrayOf(username),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    private fun processRegistration(username: String, password: String) {
        val salt = securityUtils.generateSalt()
        val hashedPassword = securityUtils.hashWithSalt(password, salt)

        if (dbHelper.registerUser(username, hashedPassword, salt)) {
            // 注册成功 - 返回到登录页面
            showToast("注册成功，请登录")

            // 设置要传递到登录页的用户名
            val bundle = bundleOf("prefilled_username" to username)

            // 导航回登录页
            findNavController().navigate(
                R.id.action_register_to_login,
                bundle
            )
        } else {
            showToast("注册失败")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
        _binding = null
    }
}