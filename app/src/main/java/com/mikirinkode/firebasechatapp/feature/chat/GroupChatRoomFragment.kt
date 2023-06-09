package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.databinding.FragmentGroupChatRoomBinding


class GroupChatRoomFragment : Fragment(), GroupChatView {
    private var _binding: FragmentGroupChatRoomBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: GroupChatPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGroupChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        presenter.detachView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleBundleArgs()
    }


    private fun handleBundleArgs() {
        Log.e("GroupChatFragment", "handle args")
        val args: GroupChatRoomFragmentArgs by navArgs()
        val conversationId: String = args.conversationId
        Log.e("GroupChatFragment", "conversationId: $conversationId")
        setupPresenter(conversationId)
    }

    private fun setupPresenter(conversationId: String) {
        presenter = GroupChatPresenter()
        presenter.attachView(this)
        presenter.onInit(conversationId)
        presenter.getGroupData(conversationId)
    }

    override fun onReceiveGroupData(conversation: Conversation) {
        binding.apply {
            tvName.text = conversation.conversationName

            tvConversationParticipants.text = "Tap for group info"
        }
    }

    override fun onMessagesReceived(messages: List<ChatMessage>) {
//        TODO("Not yet implemented")
    }

    override fun onImageCaptured(capturedImage: Uri?) {
//        TODO("Not yet implemented")
    }


    override fun showOnUploadImageProgress(progress: Int) {
//        TODO("Not yet implemented")
    }
}