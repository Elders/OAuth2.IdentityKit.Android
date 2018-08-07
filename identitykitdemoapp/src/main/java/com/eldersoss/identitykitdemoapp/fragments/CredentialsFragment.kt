package com.eldersoss.identitykitdemoapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

import com.eldersoss.identitykitdemoapp.R

class CredentialsFragment : Fragment() {

    var userNameEditText: EditText? = null
    var passwordEditText: EditText? = null

    var callback: ((username: String, password: String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_credentials, container, false)

        userNameEditText = rootView.findViewById<EditText>(R.id.username_edit_text)
        passwordEditText = rootView.findViewById<EditText>(R.id.password_edit_text)

        val confirmButton = rootView.findViewById<Button>(R.id.confirm_button)

        confirmButton.setOnClickListener {
            callback?.invoke(userNameEditText?.text.toString(), passwordEditText?.text.toString())
        }

        return rootView
    }

    companion object {
        fun newInstance(callback: (username: String, password: String) -> Unit): CredentialsFragment {
            val fragment = CredentialsFragment()
            fragment.callback = callback
            return fragment
        }
    }
}
