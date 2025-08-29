package com.lyc.englishtools.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.data.UserDbHelper
import com.lyc.englishtools.utils.SecurityUtils
import com.lyc.englishtools.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: UserDbHelper
    private val securityUtils = SecurityUtils()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        dbHelper = UserDbHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 检查是否有预填的用户名
        val prefilledUsername = arguments?.getString("prefilled_username")
        if (!prefilledUsername.isNullOrEmpty()) {
            binding.etUsername.setText(prefilledUsername)
            binding.etPassword.requestFocus() // 焦点移到密码框
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showToast("用户名和密码不能为空")
                return@setOnClickListener
            }

            dbHelper.getUserCredentials(username)?.let { credentials ->
                val (storedHash, salt) = credentials
                if (securityUtils.hashWithSalt(password, salt) == storedHash) {
                    // 登录成功 - 保存登录状态
                    AuthManager.login(requireContext(), username)

                    // 导航到主界面
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginToMainFlow()
                    )
                } else {
                    showToast("用户名或密码错误")
                }
            } ?: showToast("用户不存在")
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginToRegister()
            )
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