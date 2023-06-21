package com.mikirinkode.firebasechatapp.feature.messageinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentMessageInfoBinding

class MessageInfoFragment : Fragment(), MessageInfoView {

    private var _binding: FragmentMessageInfoBinding? = null
    private val binding get() = _binding!!
    private val args: MessageInfoFragmentArgs by navArgs()
    private var presenter: MessageInfoPresenter? = null

    private val adapter: UserReadStatusAdapter by lazy {
        UserReadStatusAdapter()
    }

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


        initPresenter()
        initView()
        onActionClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        presenter?.detachView()
    }


    private fun initPresenter(){
        presenter = MessageInfoPresenter()
        presenter?.attachView(this)
    }

    private fun initView() {
        binding.apply {
            rvMessageReadBy.layoutManager = LinearLayoutManager(requireContext())
            rvMessageReadBy.adapter = adapter


            val conversationType = args.conversationType
            val message = args.chatMessage

            // show message read by status
            when(conversationType) {
                ConversationType.PERSONAL.toString() -> {
                    layoutPersonalMessageInfo.visibility = View.VISIBLE
                    layoutGroupMessageInfo.visibility = View.GONE
                    if (message.beenDeliveredTo.isNotEmpty()){
                        val deliveredTimestamp = message.beenDeliveredTo.entries.first().value
                        tvDeliveredTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(deliveredTimestamp)
                    } else {
                        tvDeliveredTimestamp.text = "-"
                    }

                    if (message.beenReadBy.isNotEmpty()){
                        val readTimestamp = message.beenReadBy.entries.first().value
                        tvReadTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(readTimestamp)
                    } else {
                        tvReadTimestamp.text = "-"
                    }
                }
                ConversationType.GROUP.toString() -> {
                    val participantIdList = args.participantsId.toList()
                    layoutPersonalMessageInfo.visibility = View.GONE
                    layoutGroupMessageInfo.visibility = View.VISIBLE
                    presenter?.getParticipantList(participantIdList)
                }
            }

            // show message card
            if (message != null){
                if (message.senderId == args.loggedUserId){
                    layoutItemMessage.layoutInterlocutorMessage.visibility = View.GONE
                    layoutItemMessage.layoutLoggedUserMessage.visibility = View.VISIBLE
                    layoutItemMessage.apply {
                        tvloggedUserMessage.text = message.message
                        tvloggedUserTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.sendTimestamp)
                    }
                } else {
                    layoutItemMessage.layoutInterlocutorMessage.visibility = View.VISIBLE
                    layoutItemMessage.layoutLoggedUserMessage.visibility = View.GONE
                    layoutItemMessage.apply {
                        tvInterlocutorMessage.text = message.message
                        tvInterlocutorTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.sendTimestamp)
                    }
                }

                if (message.type == MessageType.IMAGE.toString() && message.imageUrl != null && message.imageUrl != ""){
                    if (message.senderId == args.loggedUserId){
                        layoutItemMessage.ivloggedUserExtraImage.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(message.imageUrl)
                            .into(layoutItemMessage.ivloggedUserExtraImage)
                    } else {
                        layoutItemMessage.ivInterlocutorExtraImage.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(message.imageUrl)
                            .into(layoutItemMessage.ivInterlocutorExtraImage)
                    }
                }
            }
        }
    }

    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        adapter.setMessage(args.chatMessage)
        adapter.setUserList(participants)
    }

    private fun onActionClick(){
        binding.apply {
            binding.topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}