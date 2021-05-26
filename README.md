IdentityKit 
=======

OAuth2 client library for Android

Download
--------
[![](https://jitpack.io/v/Elders/OAuth2.IdentityKit.Android.svg)](https://jitpack.io/#Elders/OAuth2.IdentityKit.Android)

Add the JitPack repository to your root build.gradle file:
```groovy
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency, change {Tag} with latest version tag
```groovy
	dependencies {
	        implementation 'com.github.Elders:OAuth2.IdentityKit.Android:{Tag}'
	}
```

How to use
--------
Kotlin example
```kotlin
// Implement own network client or use VolleyNetworkClient provided from the library
val client = VolleyNetworkClient(/** Application Context */ context)

// Sample configuration object
val configuration = KitConfiguration(
        retryFlowAuthentication = true, // if flow authentication failed, this property true will retry authentication
        authenticateOnFailedRefresh = true, //if refreshing failed with OAuth2 error this property true will trigger authentication process
        onAuthenticationRetryInvokeCallbackWithFailure = true // invoke callback with failure on retrying authentication, it requires retryFlowAuthentication true
)

// An authorizer used for access token request authorization
val authorizer = BasicAuthorizer("client", "secret")

// Build desired OAuth2 flow
val flow = ResourceOwnerFlow(
        "https://foo.bar/token", // Token end point in string format
        credentialsProvider, // Instance of class that implement CredentialsProvider interface
        "read write", // Scope separate by space
        authorizer, // Token authorizer
        client // network client that implement NetworkClient interface
)

// Use DefaultTokenRefresher or implement your own
val tokenRefresher = DefaultTokenRefresher("https://foo.bar/token", client, authorizer)

// Build instance of IdentityKit, this is what we need to authorize network requests
val kit = IdentityKit(
        configuration,
        flow, 
        BearerAuthorizer.Method.HEADER, 
        tokenRefresher, 
        tokenStorage, // Instance of class that implement TokenStorage interface
        client
)

// Build network request
val request = NetworkRequest(NetworkRequest.Method.GET, "https://foo.bar/api")

// Authorize and execute network request
kit.authorizeAndExecute(request, { networkResponse ->
    // Do something with the response
})

// OR just authorize network request
kit.authorize(request, { networkRequest, error ->
    // Execute authorized request or handle error     
})
```