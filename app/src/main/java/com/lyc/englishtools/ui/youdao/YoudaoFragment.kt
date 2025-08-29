package com.lyc.englishtools.ui.youdao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.R
import com.lyc.englishtools.databinding.FragmentYoudaoBinding

class YoudaoFragment : Fragment() {

    private var _binding: FragmentYoudaoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYoudaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 只保留两个功能按钮的点击事件
        binding.btnTextTranslation.setOnClickListener {
            findNavController().navigate(R.id.action_youdao_to_text)
        }

        binding.btnImageTranslation.setOnClickListener {
            findNavController().navigate(R.id.action_youdao_to_image)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}