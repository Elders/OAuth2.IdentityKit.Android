package com.eldersoss.identitykit

import com.android.volley.NetworkError
import com.android.volley.ServerError
import com.eldersoss.identitykit.network.NetworkClient
import com.eldersoss.identitykit.network.NetworkRequest
import com.eldersoss.identitykit.network.NetworkResponse
import java.nio.charset.Charset

class MockNetworkClient : NetworkClient {

    private var responseCase = ResponseCase.NO_INTERNET

    enum class ResponseCase {
        CC200OK, OK200, REFRESH200, INVALID_GRANT, NO_INTERNET,
    }

    fun setCase(case: ResponseCase) {

        responseCase = case
    }

    override suspend fun execute(request: NetworkRequest): NetworkResponse {

        val bodyString = request.body?.toString(Charset.defaultCharset())

        if (request.method === NetworkRequest.Method.POST && bodyString.equals(
                "grant_type=password&username=gg@eldersoss.com&password=ggPass123&scope=read write openid email profile offline_access owner",
                ignoreCase = true
            )
            && request.headers["Authorization"].equals(
                "Basic Y2xpZW50OnNlY3JldA==",
                ignoreCase = true
            )
        ) {
            return when (responseCase) {
                ResponseCase.OK200 -> {
                    response200()
                }
                ResponseCase.INVALID_GRANT -> {
                    invalidGrand()
                }
                else -> {
                    internalServerError()
                }
            }
            //refresh token response
        } else if (request.method === NetworkRequest.Method.POST && bodyString.equals(
                "grant_type=refresh_token&refresh_token=4f2aw4gf5ge0c3aa3as2e4f8a958c6",
                ignoreCase = true
            )
            && request.headers["Authorization"].equals(
                "Basic Y2xpZW50OnNlY3JldA==",
                ignoreCase = true
            )
        ) {
            return when (responseCase) {
                ResponseCase.OK200 -> {
                    response200()
                }
                ResponseCase.REFRESH200 -> {
                    response200refresh()
                }
                ResponseCase.INVALID_GRANT -> {
                    invalidGrand()
                }
                ResponseCase.NO_INTERNET -> {
                    noInternet()
                }
                else -> {
                    internalServerError()
                }
            }
            //client credentials response
        } else if (request.method === NetworkRequest.Method.POST && bodyString.equals(
                "grant_type=client_credentials&scope=read write openid email profile offline_access owner",
                ignoreCase = true
            )
            && request.headers["Authorization"].equals(
                "Basic Y2xpZW50OnNlY3JldA==",
                ignoreCase = true
            )
        ) {
            when (responseCase) {
                ResponseCase.CC200OK -> {
                    return response200CC()
                }
                ResponseCase.OK200 -> {
                    return response200()
                }
                ResponseCase.REFRESH200 -> {
                    return response200refresh()
                }
                ResponseCase.INVALID_GRANT -> {
                    return invalidGrand()
                }
                else -> {
                    return internalServerError()
                }
            }
            // Profile response
        } else if (request.method === NetworkRequest.Method.POST
            && request.headers["Authorization"].equals(
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9",
                ignoreCase = true
            )
        ) {
            return responseProfile()
            // Network error response
        } else {
            return internalServerError()
        }
    }

    private fun response200(): NetworkResponse {
        val response = NetworkResponse()
        response.statusCode = 200
        val headers: MutableMap<String, String> = mutableMapOf()
        putStandardHeaders(headers)
        response.headers = headers
        val body =
            "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".toByteArray()
        response.data = body
        return response
    }

    private fun response200CC(): NetworkResponse {
        val response = NetworkResponse()
        response.statusCode = 200
        val headers: MutableMap<String, String> = mutableMapOf()
        putStandardHeaders(headers)
        response.headers = headers
        val body =
            "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9\",\"expires_in\":3600,\"token_type\":\"Bearer\"}".toByteArray()
        response.data = body
        return response
    }

    private fun response200refresh(): NetworkResponse {
        val response = NetworkResponse()
        response.statusCode = 200
        val headers: MutableMap<String, String> = mutableMapOf()
        putStandardHeaders(headers)
        response.headers = headers
        val body =
            "{\"access_token\":\"TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ\",\"expires_in\":3600,\"token_type\":\"Bearer\",\"refresh_token\":\"4f2aw4gf5ge0c3aa3as2e4f8a958c6\"}".toByteArray()
        response.data = body
        return response
    }

    private fun invalidGrand(): NetworkResponse {
        val response = NetworkResponse()
        response.statusCode = 400
        val headers: MutableMap<String, String> = mutableMapOf()
        putStandardHeaders(headers)
        response.headers = headers
        val body = "{\"error\":\"invalid_grant\"}".toByteArray()
        response.data = body
        return response
    }

    private fun responseProfile(): NetworkResponse {
        val response = NetworkResponse()
        response.statusCode = 200
        val headers: MutableMap<String, String> = mutableMapOf()
        putStandardHeaders(headers)
        response.headers = headers
        val body =
            "{\"result\": [{\"type\": \"profileid\",\"value\": \"123\"},{\"type\": \"name\",\"value\": \"Identity Kit\"}]}".toByteArray()
        response.data = body
        return response
    }

    private fun internalServerError(): NetworkResponse {
        val response = NetworkResponse()
        response.error = Error(ServerError())
        response.statusCode = 500
        val headers: MutableMap<String, String> = mutableMapOf()
        putStandardHeaders(headers)
        response.headers = headers
        val body = "Internal Server Error".toByteArray()
        response.data = body
        return response
    }

    private fun noInternet(): NetworkResponse {
        val response = NetworkResponse()
        response.error = Error(NetworkError())
        response.statusCode = null
        response.headers = null
        response.data = null
        return response
    }

    companion object {
        fun putStandardHeaders(headers: MutableMap<String, String>): MutableMap<String, String> {
            headers["Cache-Control"] = "no-store, no-cache, max-age=0, private"
            headers["Pragma"] = "no-cache"
            headers["Content-Length"] = "1000"
            headers["Content-Type"] = "application/json; charset=utf-8"
            headers["Server"] = "Microsoft-IIS/10.0"
            headers["X-AspNet-Version"] = "4.0.30319"
            headers["X-Powered-By"] = "ASP.NET"
            headers["Date"] = "Tue, 22 Aug 2017 12:00:00 GMT"
            headers["Connection"] = "Keep-alive"
            return headers
        }
    }
}