package com.mikirinkode.firebasechatapp.feature.group

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentCreateGroupChatBinding
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.helper.ImageHelper
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class CreateGroupChatFragment : Fragment(), CreateGroupChatView {

    private var _binding: FragmentCreateGroupChatBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }

    private val args: CreateGroupChatFragmentArgs by navArgs()

    private lateinit var presenter: CreateCreateGroupPresenter
    private var capturedImage: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCreateGroupChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(args)

        setupPresenter()
        onActionClick()
    }

    private fun initView(args: CreateGroupChatFragmentArgs) {
        binding.apply {
            val total = args.selectedUsers.size + 1
            tvTotalMember.text = getString(R.string.txt_total_member_value, total)
        }
    }

    private fun setupPresenter() {
        presenter = CreateCreateGroupPresenter()
        presenter.attachView(this)
    }

    override fun onSuccessCreateGroupChat(conversationId: String) {
//        startActivity(Intent(requireContext(), ChatActivity::class.java))
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finishAffinity()
    }

    override fun setDataToRecyclerView(users: List<UserAccount>) {
//        TODO("Not yet implemented")
    }

    override fun onImageCaptured(capturedImage: Uri?) {
        this.capturedImage = capturedImage

        binding.apply {
            Glide.with(requireContext())
                .load(capturedImage)
                .into(ivGroupPhoto)
        }
    }

    override fun showLoading() {
//        TODO("Not yet implemented")
    }

    override fun hideLoading() {
//        TODO("Not yet implemented")
    }

    private fun onActionClick() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            cardProfile.setOnClickListener {
                if (PermissionManager.isCameraPermissionGranted(requireContext())) {
                    presenter.takePicture(requireActivity())
                } else {
                    PermissionManager.requestCameraPermission(requireActivity())
                }
            }

            btnCreateGroup.setOnClickListener {
                val groupName = etGroupName.text.toString().trim()
                if (groupName.isNullOrBlank()) {
                    etGroupName.error = getString(R.string.txt_empty_group_name)
                } else {
                    if (loggedUser != null && loggedUser?.userId != null){
                        val participants = ArrayList<String>()
                        val createdBy = loggedUser!!.userId!!

                        for (user in args.selectedUsers) {
                            if (user.userId != null) {
                                participants.add(user.userId!!)
                            }
                        }
                        loggedUser?.userId?.let { it1 -> participants.add(it1) }

                        if (capturedImage != null) {
                            val path = ImageHelper.getPathForMessages(
                                requireActivity().contentResolver,
                                capturedImage!!
                            )
                            presenter.createGroupChat(groupName, participants, createdBy, capturedImage, path)
                        } else {
                            presenter.createGroupChat(groupName, participants, createdBy, null, "")
                        }
                    }
                }
            }
        }
    }
}