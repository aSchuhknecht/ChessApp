package com.example.chessapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chessapp.databinding.SettingsFragmentBinding

class SettingsFragment: Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val viewModel: ViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var diff = viewModel.difficulty

        if (viewModel.difficulty.equals("Easy")) {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
        }
        else if (viewModel.difficulty.equals("Med")) {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
        }
        else if (viewModel.difficulty.equals("Hard")) {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ff0000")) // red
        }

        binding.difficultyEasy.setOnClickListener {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
            diff  = "Easy"
        }

        binding.difficultyMed.setOnClickListener {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
            diff = "Med"
        }

        binding.difficultyHard.setOnClickListener {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ff0000")) // red
            diff = "Hard"
        }

        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.confirmButton.setOnClickListener {
            viewModel.difficulty = diff
            val wt = binding.whiteTimerET.text.toString()
            val bt = binding.blackTimerET.text.toString()
            viewModel.whiteduration  = "$wt:00"
            viewModel.blackduration  = "$bt:00"

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}