package com.anibalventura.flagsquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.anibalventura.flagsquiz.R
import com.anibalventura.flagsquiz.databinding.FragmentRulesBinding

class RulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * Inflate the layout for this fragment.
         */
        // Use DataBindingUtil.inflate to inflate and return the Fragment in onCreateView.
        val binding: FragmentRulesBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_rules, container, false)
        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates.
        binding.lifecycleOwner = viewLifecycleOwner

        binding.retour.setOnClickListener {
            findNavController()
                .navigate(RulesFragmentDirections.actionRulesFragmentToHomeFragment())
        }

        return binding.root
    }
}
