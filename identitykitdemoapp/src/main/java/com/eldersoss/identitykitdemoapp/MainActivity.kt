package com.eldersoss.identitykitdemoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.eldersoss.identitykit.Credentials
import com.eldersoss.identitykit.CredentialsProvider
import com.eldersoss.identitykit.IdentityKit
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykitdemoapp.fragments.CredentialsFragment
import com.eldersoss.identitykitdemoapp.fragments.SetupFragment

class MainActivity : AppCompatActivity(), CredentialsProvider {

    private var identityKit: IdentityKit? = null

    private var demoLayout: LinearLayout? = null
    private var resultTextView: TextView? = null
    private var getRequestUrlEditText: EditText? = null
    private var clearResultButton: Button? = null
    private var revokeButton: Button? = null
    private var getTokenButton: Button? = null
    private var getRequestButton: Button? = null


    override fun provideCredentials(handler: Credentials) {
        Handler(Looper.getMainLooper()).post {
            demoLayout?.visibility = View.GONE
            setFragment(CredentialsFragment.newInstance { username, password ->
                setFragment(Fragment())
                demoLayout?.visibility = View.VISIBLE
                handler(username, password)
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        demoLayout = findViewById(R.id.demo_layout)
        resultTextView = findViewById(R.id.result_text_view)
        getRequestUrlEditText = findViewById(R.id.get_request_url_edit_text)
        revokeButton = findViewById(R.id.revoke_button)
        clearResultButton = findViewById(R.id.clear_result_button)
        getTokenButton = findViewById(R.id.get_token_button)
        getRequestButton = findViewById(R.id.get_request_button)

        revokeButton?.setOnClickListener { identityKit?.revokeAuthentication() }

        clearResultButton?.setOnClickListener { resultTextView?.text = null }

        getRequestButton?.setOnClickListener {
            identityKit?.authorizeAndExecute(NetworkRequest("GET", NetworkRequest.Priority.HIGH, getRequestUrlEditText?.text.toString(), HashMap(), ByteArray(0))) { networkResponse ->
                Handler(Looper.getMainLooper()).post {
                    if (networkResponse.error != null) {
                        resultTextView?.text = networkResponse.error?.getMessage()
                    } else {
                        resultTextView?.text = networkResponse.getStringData()
                    }
                }
            }
        }

        getTokenButton?.setOnClickListener { getValidToken() }

        demoLayout?.visibility = View.GONE

        setFragment(SetupFragment.newInstance(this) {
            this.identityKit = it
            getValidToken()
        })

    }

    private fun getValidToken() {
        identityKit?.getValidToken { token, error ->
            Handler(Looper.getMainLooper()).post {
                if (demoLayout?.visibility == View.GONE) {
                    setFragment(Fragment())
                    demoLayout?.visibility = View.VISIBLE
                }


                if (token != null) {
                    resultTextView?.text = token.accessToken
                } else if (error != null) {
                    resultTextView?.text = error.getMessage()
                } else {
                    resultTextView?.text = "No token, No error, BUG in the library"
                }
            }
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.replace(R.id.fragment_placeholder, fragment)
        fragmentTransaction.commit()
    }
}
