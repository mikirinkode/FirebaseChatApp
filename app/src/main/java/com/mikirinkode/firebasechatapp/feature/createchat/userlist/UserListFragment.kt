package com.mikirinkode.firebasechatapp.feature.createchat.userlist

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentUserListBinding
import com.mikirinkode.firebasechatapp.feature.chat.chatroom.ConversationActivity


class UserListFragment : Fragment(), UserListView, UserListAdapter.UserClickListener {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: UserListPresenter

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
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initView()
        setupPresenter()
        onActionClicked()
    }

    private fun initAdapter(){
        userAdapter.userClickListener = this
    }

    private fun initView() {
        binding.apply {
            rvUser.layoutManager = LinearLayoutManager(requireContext())
            rvUser.adapter = userAdapter
        }
    }

    private fun setupPresenter() {
        presenter = UserListPresenter()
        presenter.attachView(this)
        presenter.getUserList()
    }

    override fun setDataToRecyclerView(users: List<UserAccount>) {
        userAdapter.setData(users)
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onUserClick(user: UserAccount) {
        val interlocutorId = user.userId
        val loggedUserId = loggedUser?.userId
        if (loggedUserId != null && interlocutorId != null){
            val conversationId = if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
            startActivity(
                Intent(requireContext(), ConversationActivity::class.java)
                    .putExtra(ConversationActivity.EXTRA_INTENT_INTERLOCUTOR_ID, user.userId)
                    .putExtra(ConversationActivity.EXTRA_INTENT_CONVERSATION_ID, conversationId)
                    .putExtra(ConversationActivity.EXTRA_INTENT_CONVERSATION_TYPE, ConversationType.PERSONAL.toString())
            )
        }
    }

    private fun onActionClicked() {
        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.btnNewGroup.setOnClickListener {
            val action = UserListFragmentDirections.actionCreateGroup()
            Navigation.findNavController(binding.root).navigate(action)
        }
    }

}