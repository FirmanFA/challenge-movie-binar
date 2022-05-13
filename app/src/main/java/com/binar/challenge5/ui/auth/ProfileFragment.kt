package com.binar.challenge5.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.binar.challenge5.MainActivity
import com.binar.challenge5.R
import com.binar.challenge5.data.local.MyDatabase
import com.binar.challenge5.data.local.model.User
import com.binar.challenge5.databinding.FragmentProfileBinding
import com.binar.challenge5.datastore.UserDataStoreManager
import com.binar.challenge5.repository.AuthRepository
import com.binar.challenge5.utils.AESEncryption
import com.binar.challenge5.utils.PermissionUtils
import com.binar.challenge5.utils.StorageUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class ProfileFragment : Fragment() {

    private var imageUri: Uri? = null
    private var selectedImage = false
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepository(
            MyDatabase.getInstance(requireContext())!!.userDao(),
            UserDataStoreManager(requireContext())
        ))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val sharedPreference = context?.getSharedPreferences(MainActivity.SHARED_FILE, Context.MODE_PRIVATE)


//        binding.ivPhotoProfile.setOnClickListener {
//            openGallery()
//        }
        binding.btnCamera.setOnClickListener {
            if (PermissionUtils.isPermissionsGranted(requireActivity(), getRequiredPermission())){
                openCamera()
            }
        }

        binding.btnGallery.setOnClickListener {
            if (PermissionUtils.isPermissionsGranted(requireActivity(), getRequiredPermission())){
                openGallery()
            }
        }

//        val email = sharedPreference?.getString("islogin","")
        var email = ""

        authViewModel.emailPreference.observe(viewLifecycleOwner){
            email = it
            authViewModel.getUser(email)
        }

        var iduser: Int? = -1
        authViewModel.user.observe(viewLifecycleOwner){
            binding.apply {
                etName.setText(it?.name)
                etPassword.setText(AESEncryption.decrypt(it?.password))
                if (it?.avatarPath!=""){
                    val imageUri = it?.avatarPath?.toUri()
                    ivPhotoProfile.setImageURI(imageUri)
                }
            }
            iduser = it?.id
        }


        binding.btnUpdate.setOnClickListener {
            val name = binding.etName.text.toString()
            val rawPassword = binding.etPassword.text.toString()
            val password = AESEncryption.encrypt(rawPassword).toString()
            val avatar = if (imageUri==null){
                ""
            }else{
                imageUri.toString()
            }
            val user = User(iduser,name,email,password, avatarPath = avatar)


            lifecycleScope.launch(Dispatchers.IO){
                val updateUser = authViewModel.updateUser(user)

                activity?.runOnUiThread {
                    if (updateUser!=0){
                        authViewModel.getUser(email)
                        Toast.makeText(requireContext(), "update berhasil", Toast.LENGTH_SHORT).show()
//                        val editor = sharedPreference!!.edit()
//                        editor.putString("name",name)
//                        editor.apply()
                        authViewModel.setNamaPreference(name)
                    }
                }
            }
            if (imageUri==null){
                Toast.makeText(context, "Default avatar", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, imageUri.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogout.setOnClickListener {
            authViewModel.deletePref()
            it.findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }



    private var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            imageUri?.let { loadImage(it) }
            selectedImage = true
        }
    }

    private val cameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                val uri = StorageUtils.savePhotoToExternalStorage(
                    context?.contentResolver,
                    UUID.randomUUID().toString(),
                    bitmap
                )
                imageUri = uri
                uri?.let {
                    loadImage(it)
                }
            }
        }

    private fun loadImage(uri: Uri) {
        binding.ivPhotoProfile.setImageURI(uri)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResult.launch(cameraIntent)
    }

    private fun openGallery() {
        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        galleryLauncher.launch(intentGallery)
    }

    private fun getRequiredPermission(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}