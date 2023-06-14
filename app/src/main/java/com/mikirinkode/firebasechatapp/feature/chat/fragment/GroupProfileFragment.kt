package com.mikirinkode.firebasechatapp.feature.chat.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.FragmentFullScreenImageBinding
import com.mikirinkode.firebasechatapp.databinding.FragmentGroupProfileBinding


class GroupProfileFragment : Fragment() {

    private var _binding: FragmentGroupProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGroupProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: GroupProfileFragmentArgs by navArgs()

        initView(args)

        onActionClick()
    }

    private fun initView(args: GroupProfileFragmentArgs){
        binding.apply {
            val conversation = args.conversation
            tvGroupName.text = conversation.conversationName
            tvParticipant.text = "Participants (${conversation.participants.size})"

            if (!conversation.conversationAvatar.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(conversation.conversationAvatar).into(ivAvatar)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_group_avatar).into(ivAvatar)
            }
        }
    }

    private fun onActionClick(){
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            btnAddParticipant.setOnClickListener {

            }
        }
    }
}