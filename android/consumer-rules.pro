# Sealed class hierarchy: BoardPhase
-keep class io.agora.flat.common.board.BoardPhase { *; }
-keep class io.agora.flat.common.board.BoardPhase$* { *; }

# Sealed class hierarchy: BoardError
-keep class io.agora.flat.common.board.BoardError { *; }
-keep class io.agora.flat.common.board.BoardError$* { *; }

# Metadata and constructor reflection safety
-keepclassmembers class * {
    @kotlin.Metadata *;
}
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod