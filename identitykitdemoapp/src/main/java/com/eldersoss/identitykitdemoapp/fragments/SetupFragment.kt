package com.eldersoss.identitykitdemoapp.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.eldersoss.identitykit.CredentialsProvider
import com.eldersoss.identitykit.IdentityKit
import com.eldersoss.identitykit.authorization.BasicAuthorizer
import com.eldersoss.identitykit.authorization.BearerAuthorizer
import com.eldersoss.identitykit.network.volley.VolleyNetworkClient
import com.eldersoss.identitykit.oauth2.DefaultTokenRefresher
import com.eldersoss.identitykit.oauth2.flows.ResourceOwnerFlow
import com.eldersoss.identitykit.storage.DefaultTokenStorage
import com.eldersoss.identitykitdemoapp.R

class SetupFragment : Fragment() {

    var callback: ((IdentityKit) -> Unit)? = null
    var credentialsProvider: CredentialsProvider? = null

    var tokenUrlEditText: EditText? = null
    var clientEditText: EditText? = null
    var secretEditText: EditText? = null
    var scopeEditText: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_setup, container, false)

        tokenUrlEditText = rootView.findViewById<EditText>(R.id.token_url_edit_text)
        clientEditText = rootView.findViewById<EditText>(R.id.client_edit_text)
        secretEditText = rootView.findViewById<EditText>(R.id.secret_edit_text)
        scopeEditText = rootView.findViewById<EditText>(R.id.scope_edit_text)

        val confirmButton = rootView.findViewById<Button>(R.id.confirm_button)

        confirmButton.setOnClickListener {
            saveToSharedPreferences()
            callback?.invoke(initIdentityKit())
        }

        loadFromSharedPreferences()

        return rootView
    }

    private fun saveToSharedPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("tokenUrlEditText", tokenUrlEditText?.text.toString())
            putString("clientEditText", clientEditText?.text.toString())
            putString("secretEditText", secretEditText?.text.toString())
            putString("scopeEditText", scopeEditText?.text.toString())
            commit()
        }
    }

    private fun loadFromSharedPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        tokenUrlEditText?.setText(sharedPref.getString("tokenUrlEditText", null))
        clientEditText?.setText(sharedPref.getString("clientEditText", null))
        secretEditText?.setText(sharedPref.getString("secretEditText", null))
        scopeEditText?.setText(sharedPref.getString("scopeEditText", null))
    }

    private fun initIdentityKit(): IdentityKit {
        val client = VolleyNetworkClient(context!!, null, 0, 12)
        val authorizer = BasicAuthorizer(clientEditText?.text.toString(), secretEditText?.text.toString())
        val flow = ResourceOwnerFlow(tokenUrlEditText?.text.toString(), credentialsProvider!!, scopeEditText?.text.toString(), authorizer, client)
        return IdentityKit(flow, BearerAuthorizer.Method.HEADER, DefaultTokenRefresher(tokenUrlEditText?.text.toString(), client, authorizer), DefaultTokenStorage(context!!), client)
    }

    companion object {
        fun newInstance(credentialsProvider: CredentialsProvider, callback: (IdentityKit) -> Unit): SetupFragment {
            val fragment = SetupFragment()
            fragment.callback = callback
            fragment.credentialsProvider = credentialsProvider

            return fragment
        }
    }
}
