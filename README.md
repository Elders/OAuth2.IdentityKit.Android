IdentityKit 
=======

OAuth2 client library for Android

Download
--------
[ ![Download](https://api.bintray.com/packages/logix/IdentityKit/IdentityKit/images/download.svg) ](https://bintray.com/logix/IdentityKit/IdentityKit/_latestVersion)

Download via Gradle:
```groovy
compile 'com.eldersoss:identitykit:0.4.0'
```
or Maven:
```xml
<dependency>
  <groupId>com.eldersoss</groupId>
  <artifactId>identitykit</artifactId>
  <version>0.4.0</version>
  <type>pom</type>
</dependency>
```

How to use
--------
Kotlin example
```kotlin
// Implement own network client or use VolleyNetworkClient provided from the library
val client = VolleyNetworkClient(/** Application Context */ context)

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
        flow, 
        BearerAuthorizer.Method.HEADER, 
        tokenRefresher, 
        tokenStorage, // Instance of class that implement TokenStorage interface
        client
)

// Build network request
val request = NetworkRequest("GET", "https://foo.bar/api", HashMap(), "".toByteArray())

// Authorize and execute network request
kit.authorizeAndExecute(request, { networkResponse ->
    // Do something with the response
})

// OR just authorize network request
kit.authorize(request, { networkRequest, error ->
    // Execute authorized request or handle error     
})
```

Java example is analogic
```java
NetworkClient client = new VolleyNetworkClient(/** Application Context */ context);

Authorizer authorizer = new BasicAuthorizer("client", "secret");

AuthorizationFlow flow = new ResourceOwnerFlow(
        "https://foo.bar/token",
        credentialsProvider,
        "read write",
        authorizer,
        client
);

DefaultTokenRefreshernew tokenRefresher = DefaultTokenRefresher("https://foo.bar/token", client, authorizer);

IdentityKit kit = new IdentityKit(
        flow, 
        BearerAuthorizer.Method.HEADER, 
        tokenRefresher, 
        tokenStorage, // Instance of class that implement TokenStorage interface
        client
);

NetworkRequest request = new NetworkRequest("GET", "https://foo.bar/api", new HashMap<String, String>(), "".getBytes())

kit.authorizeAndExecute(request, new Function1<NetworkResponse, Unit>() {
   @Override
   public Unit invoke(NetworkResponse networkResponse) {
        // Do something with the response
        return null;
   }
});

kit.authorize(request, new Function2<NetworkRequest, Error, Unit>() {
    @Override
    public Unit invoke(NetworkRequest networkRequest, Error error) {
        // Execute authorized request or handle error 
        return null;
    }
});
```
