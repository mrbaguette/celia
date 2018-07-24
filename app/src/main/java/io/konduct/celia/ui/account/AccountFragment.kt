package io.konduct.celia.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.konduct.celia.databinding.AccountFragmentBinding

class AccountFragment : Fragment() {

    companion object {
        fun newInstance() = AccountFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = AccountFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = ViewModelProviders.of(activity!!).get(AccountViewModel::class.java)
        return binding.root
    }

}
