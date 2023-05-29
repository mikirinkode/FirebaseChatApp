package com.mikirinkode.firebasechatapp.feature.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.FragmentChatRoomBinding
import com.mikirinkode.firebasechatapp.databinding.FragmentFullScreenImageBinding
import com.mikirinkode.firebasechatapp.helper.DateHelper


class FullScreenImageFragment : Fragment() {


    private var _binding: FragmentFullScreenImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFullScreenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: FullScreenImageFragmentArgs by navArgs()

        initView(args)

        onActionClick()
    }

    private fun initView(args: FullScreenImageFragmentArgs) {
        binding.apply {
            tvMessageOnDetailImage.text = args.messageOnImage

            tvUserName.text = args.senderName
            tvDate.text =
                DateHelper.getRegularFormattedDateTimeFromTimestamp(args.dateSent)

            Glide.with(requireContext())
                .load(args.imageUrl)
                .into(ivDetailImage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onActionClick(){
        binding.apply {
            btnBack.setOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}