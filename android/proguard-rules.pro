# Fully keep BoardPhase and all nested types
-keep class io.agora.flat.common.board.BoardPhase { *; }
-keep class io.agora.flat.common.board.BoardPhase$* { *; }

# Fully keep BoardError and all nested types
-keep class io.agora.flat.common.board.BoardError { *; }
-keep class io.agora.flat.common.board.BoardError$* { *; }

# Keep all constructors and fields used by Kotlin sealed/data classes
-keepclassmembers class io.agora.flat.common.board.BoardPhase$* {
    <init>(...);
    *;
}

-keepclassmembers class io.agora.flat.common.board.BoardError$* {
    <init>(...);
    *;
}

# Prevent obfuscation of these classes
-keepnames class io.agora.flat.common.board.BoardPhase
-keepnames class io.agora.flat.common.board.BoardPhase$*
-keepnames class io.agora.flat.common.board.BoardError
-keepnames class io.agora.flat.common.board.BoardError$*
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE