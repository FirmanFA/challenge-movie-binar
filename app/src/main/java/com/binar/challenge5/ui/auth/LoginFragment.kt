package com.binar.challenge5.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.binar.challenge5.utils.AESEncryption
import com.binar.challenge4.utils.ValidationForm.isValid
import com.binar.challenge5.data.local.MyDatabase
import com.binar.challenge5.databinding.FragmentLoginBinding
import com.binar.challenge5.datastore.UserDataStoreManager
import com.binar.challenge5.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
//    private val authViewModel by viewModels<AuthViewModel> {
//        AuthViewModelFactory(AuthRepository(
//            MyDatabase.getInstance(requireContext())!!.userDao(),
//            UserDataStoreManager(requireContext())
//        ))
//    }

    private val authViewModel: AuthViewModel by viewModel()

//    private var myDatabase: MyDatabase? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {

            if (binding.etEmail.isValid() and binding.etPassword.isValid()){
                val email = binding.etEmail.text.toString()
                val rawPassword = binding.etPassword.text.toString()
                val password = AESEncryption.encrypt(rawPassword).toString()


                lifecycleScope.launch(Dispatchers.IO) {
                    val isLogin = authViewModel.login(email, password)

                    activity?.runOnUiThread {
                        if (isLogin == null){
                            Toast.makeText(context, "Pastikan email dan password benar", Toast.LENGTH_SHORT).show()
                        }else{
                            lifecycleScope.launch(Dispatchers.IO){
                                authViewModel.setEmailPreference(email)
                                authViewModel.setNamaPreference(isLogin.name)

                                runBlocking(Dispatchers.Main) {
//                                    val action = LoginFragmentDirections
//                                        .actionLoginFragmentToHomeFragment()
//                                    it.findNavController().navigate(action)
                                }
                            }


                        }
                    }

                }
            }


        }

        binding.btnRegister.setOnClickListener {
//            val action = LoginFragmentDirections
//                .actionLoginFragmentToRegisterFragment()
//            it.findNavController().navigate(action)
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}