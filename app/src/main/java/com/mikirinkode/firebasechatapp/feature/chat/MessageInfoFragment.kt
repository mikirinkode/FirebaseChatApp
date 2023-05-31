package com.mikirinkode.firebasechatapp.feature.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.FragmentMessageInfoBinding
import com.mikirinkode.firebasechatapp.helper.DateHelper

class MessageInfoFragment : Fragment() {

    private var _binding: FragmentMessageInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMessageInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: MessageInfoFragmentArgs by navArgs()

        initView(args)

        onActionClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initView(args: MessageInfoFragmentArgs) {
        binding.apply {
            val message = args.chatMessage

            if (message != null){
                if (message.deliveredTimestamp != 0L){
                    tvDeliveredTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.deliveredTimestamp)
                } else {
                    tvDeliveredTimestamp.text = "-"
                }

                if (message.readTimestamp != 0L){
                    tvReadTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.readTimestamp)
                } else {
                    tvReadTimestamp.text = "-"
                }

                if (message.senderId == args.loggedUserId){
                    layoutItemMessage.layoutInterlocutorMessage.visibility = View.GONE
                    layoutItemMessage.layoutLoggedUserMessage.visibility = View.VISIBLE
                    layoutItemMessage.apply {
                        tvloggedUserMessage.text = message.message
                        tvloggedUserTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.timestamp)
                    }
                } else {
                    layoutItemMessage.layoutInterlocutorMessage.visibility = View.VISIBLE
                    layoutItemMessage.layoutLoggedUserMessage.visibility = View.GONE
                    layoutItemMessage.apply {
                        tvInterlocutorMessage.text = message.message
                        tvInterlocutorTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.timestamp)
                    }
                }
            }
        }
    }

    private fun onActionClick(){
        binding.apply {
            btnBack.setOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}