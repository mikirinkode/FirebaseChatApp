package com.mikirinkode.firebasechatapp.feature.groupprofile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentGroupProfileBinding
import com.mikirinkode.firebasechatapp.feature.group.CreateNewChatActivity
import com.mikirinkode.firebasechatapp.feature.userlist.UserListAdapter


class GroupProfileFragment : Fragment(), GroupProfileView {

    private var _binding: FragmentGroupProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: GroupProfilePresenter

    private val args: GroupProfileFragmentArgs by navArgs()

    private val userAdapter: UserListAdapter by lazy {
        UserListAdapter()
    }


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

        initView()
        initPresenter()
        onActionClick()
    }

    private fun initView() {
        binding.apply {
            rvUser.layoutManager = LinearLayoutManager(requireContext())
            rvUser.adapter = userAdapter


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

    private fun initPresenter() {
        presenter = GroupProfilePresenter()
        presenter.attachView(this)
        presenter.getAllParticipantsDate(args.conversation.participants)
    }

    private fun onActionClick() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            btnAddParticipant.setOnClickListener {
                startActivity(
                    Intent(requireContext(), CreateNewChatActivity::class.java)
                        .putExtra(
                            CreateNewChatActivity.EXTRA_INTENT_CONVERSATION_TYPE,
                            ConversationType.GROUP.toString()
                        ).putExtra(
                            CreateNewChatActivity.EXTRA_INTENT_CONVERSATION_ID,
                            args.conversation.conversationId
                        ).putStringArrayListExtra(
                            CreateNewChatActivity.EXTRA_INTENT_PARTICIPANTS_ID,
                            ArrayList<String>(args.conversation.participants)
                        )
                )
            }
        }
    }

    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        userAdapter.setData(participants)
    }
}