package com.lyc.englishtools.ui.study.vocabulary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lyc.englishtools.R
import com.lyc.englishtools.databinding.FragmentMyVocabularyBinding
import com.lyc.englishtools.viewmodel.MyVocabularyViewModelFactory

class MyVocabularyFragment : Fragment() {

    private var _binding: FragmentMyVocabularyBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VocabularyAdapter
    private val viewModel: MyVocabularyViewModel by viewModels {
        MyVocabularyViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyVocabularyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            // 确保返回 StudyFragment
            if (findNavController().currentDestination?.id == R.id.myVocabularyFragment) {
                findNavController().navigate(R.id.action_myVocabularyFragment_to_studyFragment)
            }
        }

        // 初始化适配器 - 处理WordEntity对象

        adapter = VocabularyAdapter { wordId ->
            // 立即更新UI
            viewModel.removeWord(requireContext(), wordId)
            // 显示操作成功的Toast提示
            Toast.makeText(requireContext(), "单词已移除", Toast.LENGTH_SHORT).show()
        }


        binding.rvVocabulary.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVocabulary.adapter = adapter

        // 观察ViewModel状态
        observeViewModel()

        // 加载数据
        viewModel.loadVocabulary(requireContext())
    }

    private fun observeViewModel() {
        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvVocabulary.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            binding.tvErrorMessage.visibility = View.GONE
            binding.tvEmpty.visibility = View.GONE
        }

        // 观察错误消息
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                binding.tvErrorMessage.text = message
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.rvVocabulary.visibility = View.INVISIBLE
                binding.tvEmpty.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }

        // 观察单词列表数据
        viewModel.vocabularyWords.observe(viewLifecycleOwner) { words ->
            if (words.isNotEmpty()) {
                adapter.submitList(words)
                binding.rvVocabulary.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvVocabulary.visibility = View.INVISIBLE
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}