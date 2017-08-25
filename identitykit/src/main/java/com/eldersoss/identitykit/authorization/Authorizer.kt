package com.eldersoss.identitykit.authorization

import com.eldersoss.identitykit.network.IdRequest

/**
 * Created by IvanVatov on 8/21/2017.
 */
interface Authorizer{
    fun authorize(request : IdRequest)
}