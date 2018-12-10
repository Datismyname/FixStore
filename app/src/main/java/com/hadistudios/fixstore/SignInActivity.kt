package com.hadistudios.fixstore

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.iid.FirebaseInstanceId
import com.hadistudios.fixstore.service.MyFirebaseMessagingService
import com.hadistudios.fixstore.util.FirestoreUtil
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1

    private val signInProviders =
            listOf(AuthUI.IdpConfig.EmailBuilder()
                    .setAllowNewAccounts(true)
                    .setRequireName(true)
                    .build()
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        account_sign_in_button.setOnClickListener {
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(signInProviders)
                    .setLogo(R.drawable.double_wrench)
                    .build()

            startActivityForResult(intent, RC_SIGN_IN)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {

                val progressDialog = indeterminateProgressDialog("Setting Up Your Account")

                FirestoreUtil.initCurrentUserIfFirstTime {

                    startActivity(intentFor<MainActivity>().newTask().clearTask())

                    FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->

                        val registrationToken = instanceIdResult.token

                        MyFirebaseMessagingService.addTokenToFireStore(registrationToken)

                    }



                    progressDialog.dismiss()

                }


            } else if (resultCode == Activity.RESULT_CANCELED) {

                if (response == null) return

                when (response.error?.errorCode) {

                    ErrorCodes.NO_NETWORK -> {
                        longSnackbar(constraint_layout, "No Network")
                    }

                    ErrorCodes.UNKNOWN_ERROR -> {
                        longSnackbar(constraint_layout, "Unknown Error")
                    }

                }

            }

        }
    }
}