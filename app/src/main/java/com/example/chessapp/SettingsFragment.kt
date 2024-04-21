package com.example.chessapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

        var depth = viewModel.depth
        var mode = viewModel.mode

        if (viewModel.depth==1) {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
        }
        else if (viewModel.depth==5) {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
        }
        else if (viewModel.depth==12) {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ff0000")) // red
        }


        if (viewModel.mode.equals("player")) {
            binding.modePVP.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.modeCPU.setBackgroundColor(Color.parseColor("#ffffff")) // whitee
        }
        else if (viewModel.mode.equals("cpu")) {
            binding.modePVP.setBackgroundColor(Color.parseColor("#ffffff"))
            binding.modeCPU.setBackgroundColor(Color.parseColor("#ff0000"))
        }

        binding.modePVP.setOnClickListener {
            binding.modePVP.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.modeCPU.setBackgroundColor(Color.parseColor("#ffffff")) // whitee
            mode = "player"
        }

        binding.modeCPU.setOnClickListener {
            binding.modePVP.setBackgroundColor(Color.parseColor("#ffffff"))
            binding.modeCPU.setBackgroundColor(Color.parseColor("#ff0000"))
            mode = "cpu"
        }


        binding.difficultyEasy.setOnClickListener {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
            depth =1
        }

        binding.difficultyMed.setOnClickListener {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ff0000")) // red
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ffffff")) // white
            depth =5
        }

        binding.difficultyHard.setOnClickListener {
            binding.difficultyEasy.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyMed.setBackgroundColor(Color.parseColor("#ffffff")) // white
            binding.difficultyHard.setBackgroundColor(Color.parseColor("#ff0000")) // red
            depth =12
        }

        binding.cancelButton.setOnClickListener {
            viewModel.returningFromSettings = true
            findNavController().popBackStack()
        }

        binding.confirmButton.setOnClickListener {
            viewModel.depth = depth
            viewModel.mode = mode
            val wt = binding.whiteTimerET.text.toString()
            val bt = binding.blackTimerET.text.toString()

            if (wt.isNotEmpty()) {
                val wtl = wt.toLong() * 1000
                viewModel.whiteTime = wtl
            }
            if (bt.isNotEmpty()) {
                val btl = bt.toLong() * 1000
                viewModel.blackTime  = btl
            }
            viewModel.returningFromSettings = true
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}