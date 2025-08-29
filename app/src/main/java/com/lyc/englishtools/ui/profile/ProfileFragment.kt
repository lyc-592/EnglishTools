package com.lyc.englishtools.ui.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.lyc.englishtools.R
import com.lyc.englishtools.ui.auth.AuthManager
import com.lyc.englishtools.databinding.FragmentProfileBinding
import com.lyc.englishtools.utils.FileUtils
import com.lyc.englishtools.utils.SharedPreferencesUtils

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // 头像文件名前缀
    private val AVATAR_PREFIX = "avatar_"

    // 用户头像文件名
    private var avatarFileName: String? = null
    private var currentAvatarUri: Uri? = null

    // 使用新的 Activity Result API
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentAvatarUri = it
            saveAvatar(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAvatarFileName()
        displayCurrentUser()
        setupLogoutButton()
        setupAvatarClickListener()
        loadSavedAvatar()
    }

    private fun setupAvatarFileName() {
        val username = AuthManager.getCurrentUsername() ?: return
        avatarFileName = "$AVATAR_PREFIX${username.hashCode()}.jpg"
    }

    private fun displayCurrentUser() {
        val username = AuthManager.getCurrentUsername()
        binding.tvUsername.text = username ?: "未登录"
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("确认退出")
                setMessage("确定要退出当前账号吗？")
                setPositiveButton("确定") { _, _ ->
                    AuthManager.logout(requireContext())
                    navigateToLogin()
                }
                setNegativeButton("取消", null)
                create().show()
            }
        }
    }

    private fun setupAvatarClickListener() {
        binding.ivAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }
    }

    private fun showAvatarSelectionDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("上传头像")
            setMessage("请选择头像来源")
            setPositiveButton("相册") { _, _ ->
                openGalleryForImage()
            }
            setNegativeButton("取消", null)
            create().show()
        }
    }

    private fun openGalleryForImage() {
        galleryLauncher.launch("image/*")
    }

    private fun saveAvatar(uri: Uri) {
        // 如果用户名不可用，则不保存
        if (avatarFileName == null) return

        try {
            // 保存到SharedPreferences
            saveAvatarUri(uri.toString())

            // 加载选择的图片
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(binding.ivAvatar)

            // 保存头像到文件
            saveAvatarToFile(uri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "头像保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveAvatarUri(uriString: String) {
        val username = AuthManager.getCurrentUsername() ?: return
        val key = "avatar_uri_${username}"
        SharedPreferencesUtils.saveString(requireContext(), key, uriString)
    }

    private fun loadSavedAvatarUri(): String? {
        val username = AuthManager.getCurrentUsername() ?: return null
        val key = "avatar_uri_${username}"
        return SharedPreferencesUtils.getString(requireContext(), key)
    }

    private fun saveAvatarToFile(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                avatarFileName?.let { fileName ->
                    FileUtils.saveStreamToFile(requireContext(), fileName, inputStream)

                    // 确保文件保存后立即更新显示
                    loadSavedAvatar()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "头像保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun loadSavedAvatar() {
        try {
            // 优先尝试从URI加载（更高效）
            loadSavedAvatarUri()?.let { uriString ->
                currentAvatarUri = Uri.parse(uriString)
                Glide.with(this)
                    .load(currentAvatarUri)
                    .circleCrop()
                    .error(R.drawable.ic_default_avatar)
                    .into(binding.ivAvatar)
                return
            }

            // 如果URI不可用，则从文件加载
            avatarFileName?.let { fileName ->
                val avatarFile = FileUtils.getFile(requireContext(), fileName)
                if (avatarFile.exists()) {
                    Glide.with(this)
                        .load(avatarFile)
                        .circleCrop()
                        .error(R.drawable.ic_default_avatar)
                        .into(binding.ivAvatar)
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "加载头像失败", e)
            Toast.makeText(requireContext(), "加载头像失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_global_logout)
    }

    override fun onResume() {
        super.onResume()
        // 每次回到该Fragment时刷新头像
        loadSavedAvatar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}