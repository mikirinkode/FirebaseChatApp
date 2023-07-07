package com.mikirinkode.firebasechatapp.feature.createchat.setupgroup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.commonhelper.ImageHelper
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentSetupGroupChatBinding
import com.mikirinkode.firebasechatapp.feature.createchat.CreateChatPresenter
import com.mikirinkode.firebasechatapp.feature.createchat.CreateChatView
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.commonhelper.PermissionHelper

class SetupProfileGroupChatFragment : Fragment(), CreateChatView {

    private var _binding: FragmentSetupGroupChatBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }

    private val args: SetupProfileGroupChatFragmentArgs by navArgs()

    private lateinit var presenter: CreateChatPresenter
    private var capturedImage: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetupGroupChatBinding.inflate(inflater, container, false)
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

    private fun initView(args: SetupProfileGroupChatFragmentArgs) {
        binding.apply {
            val total = args.selectedUsers.size + 1
            tvTotalMember.text = getString(R.string.txt_total_member_value, total)
        }
    }

    private fun setupPresenter() {
        presenter = CreateChatPresenter()
        presenter.attachView(this)
    }

    override fun onSuccessCreateGroupChat(conversationId: String) {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finishAffinity()
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

            cardAvatar.setOnClickListener {
                if (PermissionHelper.isCameraPermissionGranted(requireContext())) {
                    presenter.takePicture(requireActivity())
                } else {
                    PermissionHelper.requestCameraPermission(requireActivity())
                }
            }

            btnCreateGroup.setOnClickListener {
                val groupName = etGroupName.text.toString().trim()
                if (groupName.isBlank()) {
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