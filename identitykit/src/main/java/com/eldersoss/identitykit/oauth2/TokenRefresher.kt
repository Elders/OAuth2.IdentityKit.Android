package com.eldersoss.identitykit.oauth2

import com.eldersoss.identitykit.network.NetworkRequest

/**
 * Created by IvanVatov on 8/30/2017.
 */
interface TokenRefresher{
    fun refresh(refreshToken: String, scope: String?, handler:(Token?, TokenError?) -> Unit)
}