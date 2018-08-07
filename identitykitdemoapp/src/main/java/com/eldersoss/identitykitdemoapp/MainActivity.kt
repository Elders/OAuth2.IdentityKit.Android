package com.eldersoss.identitykitdemoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.TextView
import com.eldersoss.identitykit.Credentials
import com.eldersoss.identitykit.CredentialsProvider
import com.eldersoss.identitykit.IdentityKit
import com.eldersoss.identitykitdemoapp.fragments.CredentialsFragment
import com.eldersoss.identitykitdemoapp.fragments.SetupFragment

class MainActivity : AppCompatActivity(), CredentialsProvider {

    var identityKit: IdentityKit? = null
    var resultTextView: TextView? = null

    override fun provideCredentials(handler: Credentials) {
        resultTextView?.visibility = View.GONE
        setFragment(CredentialsFragment.newInstance { username, password ->
            setFragment(Fragment())
            resultTextView?.visibility = View.VISIBLE
            handler(username, password)
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result_text_view)

        resultTextView?.visibility = View.GONE
        setFragment(SetupFragment.newInstance(this) {
            this.identityKit = it
            it.getValidToken { token, error ->
                if (token != null) {
                    resultTextView?.text = token.accessToken
                } else if (error != null) {
                    resultTextView?.text = error.getMessage()
                } else {
                    resultTextView?.text = "No token, No error, BUG in the library"
                }

            }
        })

    }


    private fun setFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.replace(R.id.fragment_placeholder, fragment)
        fragmentTransaction.commit()
    }
}
