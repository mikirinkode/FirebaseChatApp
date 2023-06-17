package com.mikirinkode.firebasechatapp.feature.selectuser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentSelectUserBinding
import com.mikirinkode.firebasechatapp.feature.chat.ConversationActivity
import com.mikirinkode.firebasechatapp.feature.userlist.UserListPresenter
import com.mikirinkode.firebasechatapp.feature.userlist.UserListView

class SelectUserFragment : Fragment(), SelectUserView, SelectUserAdapter.UserClickListener,
    SelectedUserAdapter.UserClickListener {

    private var _binding: FragmentSelectUserBinding? = null
    private val binding get() = _binding!!

    private val userListAdapter: SelectUserAdapter by lazy {
        SelectUserAdapter()
    }

    private val selectedUserAdapter: SelectedUserAdapter by lazy {
        SelectedUserAdapter()
    }

    private val selectedUserList = ArrayList<UserAccount>()

    private lateinit var presenter: SelectUserPresenter

    private val args: SelectUserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSelectUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initView()
        setupPresenter()
        onActionClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initAdapter() {
        userListAdapter.userClickListener = this
        selectedUserAdapter.userClickListener = this
    }

    private fun initView() {
        binding.apply {

            rvUser.layoutManager = LinearLayoutManager(requireContext())
            rvUser.adapter = userListAdapter

            rvSelectedUser.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvSelectedUser.adapter = selectedUserAdapter
        }
    }

    private fun setupPresenter() {
        presenter = SelectUserPresenter()
        presenter.attachView(this)
        presenter.getUserList()
    }

    private fun updateViews() {
        binding.apply {
            if (selectedUserList.isNotEmpty()) {
                btnNext.visibility = View.VISIBLE
            } else {
                btnNext.visibility = View.GONE
            }
        }
    }

    override fun setDataToRecyclerView(users: List<UserAccount>) {
        val participants = args.participantsId?.toList()
        Log.e("SelectUserFragment", "args: ${args}")
        Log.e("SelectUserFragment", "args: ${args.conversationId}")
        Log.e("SelectUserFragment", "args: ${args.participantsId}")
        if (participants?.isNotEmpty() == true) {
            val filteredList = users.filter { user -> user.userId !in participants }
            userListAdapter.setData(filteredList)
        } else {
            userListAdapter.setData(users)
        }
    }


    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onUserClick(user: UserAccount) {
//        when(args.conversationType){
//            ConversationType.PERSONAL.toString() -> {
//                startActivity(
//                    Intent(requireContext(), ConversationActivity::class.java)
//                        .putExtra(ConversationActivity.EXTRA_INTENT_INTERLOCUTOR_ID, user.userId)
//                        .putExtra(ConversationActivity.EXTRA_INTENT_CONVERSATION_TYPE, ConversationType.PERSONAL.toString())
//                )
//            }
//            ConversationType.GROUP.toString() -> {
//            }
//        }
        val isUserInList = selectedUserList.firstOrNull { it.userId == user.userId }
        if (isUserInList == null) {
            selectedUserList.add(user)
        } else {
            selectedUserList.remove(user)
        }
        selectedUserAdapter.setData(selectedUserList)
        updateViews()
    }

    override fun onRemoveSelected(user: UserAccount) {
        selectedUserList.remove(user)
        selectedUserAdapter.setData(selectedUserList)
    }

    override fun onSuccessAddParticipantsToGroup() {
        Toast.makeText(requireContext(), "Success Adding new Participant", Toast.LENGTH_SHORT).show()
        navigateBack()
    }

    private fun navigateBack(){
        val navController = Navigation.findNavController(binding.root)
        val canNavigateUp =
            navController.currentDestination?.id != navController.graph.startDestinationId

        if (canNavigateUp) {
            Navigation.findNavController(binding.root).navigateUp()
        } else {
            requireActivity().finish()
        }
    }

    private fun onActionClicked() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                navigateBack()
            }

            btnNext.setOnClickListener {
                val conversationId = args.conversationId

                Log.e("SelectUser", "conversationId: $conversationId")
                if (conversationId != null) {
                    val participantsId: List<String> = selectedUserList.map { user ->
                        user.userId ?: ""
                    }
                    presenter.addParticipantsToGroup(conversationId, participantsId)
                } else {
                    val action = SelectUserFragmentDirections.actionContinue(
                        selectedUserList.toTypedArray()
                    )
                    Navigation.findNavController(binding.root).navigate(action)

                }
            }
        }
    }
}