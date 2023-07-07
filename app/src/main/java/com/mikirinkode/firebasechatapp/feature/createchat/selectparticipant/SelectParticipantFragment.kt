package com.mikirinkode.firebasechatapp.feature.createchat.selectparticipant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentSelectUserBinding

class SelectParticipantFragment : Fragment(), SelectParticipantView, ParticipantAdapter.UserClickListener,
    SelectedParticipantAdapter.UserClickListener {

    private var _binding: FragmentSelectUserBinding? = null
    private val binding get() = _binding!!

    private val userListAdapter: ParticipantAdapter by lazy {
        ParticipantAdapter()
    }

    private val selectedParticipantAdapter: SelectedParticipantAdapter by lazy {
        SelectedParticipantAdapter()
    }

    private val selectedUserList = ArrayList<UserAccount>()

    private lateinit var presenter: SelectParticipantPresenter

    private val args: SelectParticipantFragmentArgs by navArgs()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter() {
        userListAdapter.userClickListener = this
        selectedParticipantAdapter.userClickListener = this
    }

    private fun initView() {
        binding.apply {

            rvUser.layoutManager = LinearLayoutManager(requireContext())
            rvUser.adapter = userListAdapter

            rvSelectedUser.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvSelectedUser.adapter = selectedParticipantAdapter
        }
    }

    private fun setupPresenter() {
        presenter = SelectParticipantPresenter()
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
        selectedParticipantAdapter.setData(selectedUserList)
        updateViews()
    }

    override fun onRemoveSelected(user: UserAccount) {
        selectedUserList.remove(user)
        selectedParticipantAdapter.setData(selectedUserList)
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

                if (conversationId != null) {
                    val participantsId: List<String> = selectedUserList.map { user ->
                        user.userId ?: ""
                    }
                    presenter.addParticipantsToGroup(conversationId, participantsId)
                } else {
                    val action = SelectParticipantFragmentDirections.actionContinue(
                        selectedUserList.toTypedArray()
                    )
                    Navigation.findNavController(binding.root).navigate(action)

                }
            }
        }
    }
}