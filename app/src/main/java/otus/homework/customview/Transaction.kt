package otus.homework.customview

import kotlinx.serialization.Serializable

@Serializable
internal data class Transaction(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long,
)
