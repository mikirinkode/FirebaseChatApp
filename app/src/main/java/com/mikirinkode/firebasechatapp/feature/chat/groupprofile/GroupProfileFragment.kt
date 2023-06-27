package com.mikirinkode.firebasechatapp.feature.chat.groupprofile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentGroupProfileBinding
import com.mikirinkode.firebasechatapp.feature.createchat.CreateNewChatActivity
import com.mikirinkode.firebasechatapp.feature.groupprofile.GroupProfileFragmentArgs
import com.mikirinkode.firebasechatapp.feature.createchat.userlist.UserListAdapter


class GroupProfileFragment : Fragment(), GroupProfileView {

    private var _binding: FragmentGroupProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: GroupProfilePresenter

    private val args: GroupProfileFragmentArgs by navArgs()

    private val userAdapter: UserListAdapter by lazy {
        UserListAdapter()
    }

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGroupProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (presenter == null){
            initPresenter()
        } else {
            presenter.getGroupData(args.conversationId)
            presenter.getParticipantList(args.participantsId.toList())
        }
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

        }
    }

    private fun initPresenter() {
        presenter = GroupProfilePresenter()
        presenter.attachView(this)
        presenter.getGroupData(args.conversationId)
        presenter.getParticipantList(args.participantsId.toList())
    }

    override fun onReceiveGroupData(conversation: Conversation) {
        binding.apply {
            if (conversation.createdBy == loggedUser?.userId){
                btnAddParticipant.visibility = View.VISIBLE
            } else {
                btnAddParticipant.visibility = View.GONE
            }
            tvGroupName.text = conversation.conversationName
            tvParticipant.text = "Participants (${conversation.participants.size})"
            tvCreatedAt.text = "at ${DateHelper.getRegularFormattedDateTimeFromTimestamp(conversation.createdAt ?: 0)}"

            if (!conversation.conversationAvatar.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(conversation.conversationAvatar).into(ivAvatar)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_group_avatar).into(ivAvatar)
            }
        }
    }

    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        userAdapter.setData(participants)

        val creatorId = args.creatorId
        val creator = participants.firstOrNull { user -> user.userId == creatorId }

        binding.apply {
            if (loggedUser?.userId == creatorId) {
                tvCreatedBy.text = "Created by You,"
            } else {
                tvCreatedBy.text = "Created by ${creator?.name}"
            }
        }
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
                            args.conversationId
                        ).putStringArrayListExtra(
                            CreateNewChatActivity.EXTRA_INTENT_PARTICIPANTS_ID,
                            ArrayList<String>(args.participantsId.toList())
                        )
                )
            }
        }
    }
}