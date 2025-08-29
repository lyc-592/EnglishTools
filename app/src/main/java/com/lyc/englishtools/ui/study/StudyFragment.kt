package com.lyc.englishtools.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lyc.englishtools.databinding.FragmentStudyBinding
import com.lyc.englishtools.R

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnWordMemory.setOnClickListener {
            findNavController().navigate(R.id.action_studyFragment_to_wordMemoryFragment)
        }

        binding.btnMyVocabulary.setOnClickListener {
            findNavController().navigate(R.id.action_studyFragment_to_myVocabularyFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}