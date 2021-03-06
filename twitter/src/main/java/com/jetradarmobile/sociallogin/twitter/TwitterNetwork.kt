package com.jetradarmobile.sociallogin.twitter

import android.app.Activity
import android.content.Intent
import com.jetradarmobile.sociallogin.SocialAccount
import com.jetradarmobile.sociallogin.SocialAuthCallback
import com.jetradarmobile.sociallogin.SocialAuthError
import com.jetradarmobile.sociallogin.SocialNetwork
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient

class TwitterNetwork : Callback<TwitterSession>(), SocialNetwork {
  override val code: String = CODE

  private lateinit var authClient: TwitterAuthClient
  private var loginCallback: SocialAuthCallback? = null

  override fun login(activity: Activity, callback: SocialAuthCallback) {
    loginCallback = callback
    authClient = TwitterAuthClient()
    authClient.authorize(activity, this)
  }

  override fun logout(activity: Activity, callback: SocialAuthCallback) {
    TwitterCore.getInstance().sessionManager.takeIf { it.activeSession != null }?.clearActiveSession()
    callback.onLogoutSuccess(this)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
      authClient.onActivityResult(requestCode, resultCode, data)

  override fun success(result: Result<TwitterSession>?) {
    loginCallback?.onLoginSuccess(this, createSocialToken(result?.data))
  }

  override fun failure(exception: TwitterException?) {
    val message = exception?.message ?: ""
    loginCallback?.onAuthError(this,
        if (message.isNotEmpty()) SocialAuthError(message) else SocialAuthError.CANCELLED)
  }

  private fun createSocialToken(session: TwitterSession?) = SocialAccount(
      token = session?.authToken?.token ?: "",
      networkCode = CODE,
      secret = session?.authToken?.secret ?: "",
      userId = session?.userId?.toString() ?: "",
      userName = session?.userName ?: ""
  )

  companion object {
    const val CODE = "twitter"
  }

}