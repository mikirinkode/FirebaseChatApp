package com.mikirinkode.firebasechatapp.feature.group

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentSelectUserBinding

class SelectUserFragment : Fragment(), CreateGroupChatView, SelectUserAdapter.UserClickListener, SelectedUserAdapter.UserClickListener {

    private var _binding: FragmentSelectUserBinding? = null
    private val binding get() = _binding!!

    private val userListAdapter: SelectUserAdapter by lazy {
        SelectUserAdapter()
    }

    private val selectedUserAdapter: SelectedUserAdapter by lazy {
        SelectedUserAdapter()
    }

    private val selectedUserList = ArrayList<UserAccount>()

    private lateinit var presenter: CreateCreateGroupPresenter

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

    private fun initAdapter(){
        userListAdapter.userClickListener = this
        selectedUserAdapter.userClickListener = this
    }

    private fun initView() {
        binding.apply {
            rvUser.layoutManager = LinearLayoutManager(requireContext())
            rvUser.adapter = userListAdapter

            rvSelectedUser.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvSelectedUser.adapter = selectedUserAdapter
        }
    }

    private fun setupPresenter(){
        presenter = CreateCreateGroupPresenter()
        presenter.attachView(this)
        presenter.getUserList()
    }

    private fun updateViews(){
        binding.apply {
            if (selectedUserList.isNotEmpty()){
                btnNext.visibility = View.VISIBLE
            }else {
                btnNext.visibility = View.GONE
            }
        }
    }

    override fun setDataToRecyclerView(users: List<UserAccount>) {
        userListAdapter.setData(users)
    }

    override fun onImageCaptured(capturedImage: Uri?) {
//        TODO("Not yet implemented")
    }

    override fun onSuccessCreateGroupChat(conversationId: String) {
//        TODO("Not yet implemented")
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onUserClick(user: UserAccount) {
        Log.e("CGCA", "onUserClick / onUserSelected")
        val isUserInList = selectedUserList.firstOrNull { it.userId == user.userId }
        Log.e("CGCA", "isUserInList: ${isUserInList}")
        if (isUserInList == null){
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

    private fun onActionClicked() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                requireActivity().finish()
            }

            btnNext.setOnClickListener {
                val action = SelectUserFragmentDirections.actionContinue(
                    selectedUserList.toTypedArray()
                )
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}