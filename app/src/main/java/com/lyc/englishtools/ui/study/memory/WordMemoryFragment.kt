package com.lyc.englishtools.ui.study.memory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.R
import com.lyc.englishtools.databinding.FragmentWordMemoryBinding
import com.lyc.englishtools.utils.PrefUtils
import com.lyc.englishtools.viewmodel.WordMemoryViewModelFactory

class WordMemoryFragment : Fragment() {

    private var _binding: FragmentWordMemoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordMemoryViewModel by viewModels {
        WordMemoryViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮
        binding.btnBack.setOnClickListener {
            // 确保返回 StudyFragment
            if (findNavController().currentDestination?.id == R.id.wordMemoryFragment) {
                findNavController().navigate(R.id.action_wordMemoryFragment_to_studyFragment)
            }
        }

        // 加载单词
        viewModel.loadWords(requireContext())

        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentGroup.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }

        // 观察错误消息
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                binding.tvErrorMessage.text = it
                binding.tvErrorMessage.visibility = View.VISIBLE
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // 观察当前单词
        viewModel.currentWord.observe(viewLifecycleOwner) { word ->
            word?.let {
                binding.tvEnglish.text = it.english
                binding.tvChinese.text = it.chinese
            }
        }

        // 观察中文显示状态
        viewModel.showChinese.observe(viewLifecycleOwner) { show ->
            binding.tvChinese.visibility = if (show) View.VISIBLE else View.INVISIBLE
            binding.btnToggleChinese.text = if (show) "隐藏中文" else "显示中文"
        }

        // 设置按钮事件
        binding.btnToggleChinese.setOnClickListener {
            viewModel.toggleChinese()
        }

        binding.btnPrev.setOnClickListener {
            viewModel.prevWord()
        }

        binding.btnNext.setOnClickListener {
            viewModel.nextWord()
        }

        binding.btnBookmark.setOnClickListener {
            viewModel.saveBookmark(requireContext())
            Toast.makeText(requireContext(), "已添加书签", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddToVocabulary.setOnClickListener {
            val word = viewModel.currentWord.value
            word?.let {
                PrefUtils.addToVocabulary(requireContext(), it.id)
                Toast.makeText(requireContext(), "已添加到我的单词本", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}