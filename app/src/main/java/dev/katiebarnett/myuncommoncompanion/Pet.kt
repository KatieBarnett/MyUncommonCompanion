package dev.katiebarnett.myuncommoncompanion
import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val name: String,
    val description: String,
    val photoUrl: String,
)
