package com.call_blocker.a_repository.dto


import com.call_blocker.a_repository.model.UserDetailInfo
import com.call_blocker.a_repository.model.UserInfo
import com.call_blocker.a_repository.model.UserModel
import com.squareup.moshi.Json

data class UserInfoDto(
    @Json(name = "data")
    val data: Data
) {
    data class Data(
        @Json(name = "first_sim")
        val firstSim: SimInfo,
        @Json(name = "second_sim")
        val secondSim: SimInfo,
        @Json(name = "user")
        val user: User
    ) {

        data class User(
            @Json(name = "bank_id")
            val bankId: String?,
            @Json(name = "calculation")
            val calculation: Float?,
            @Json(name = "details")
            val details: Details,
            @Json(name = "email")
            val email: String?,
            @Json(name = "id")
            val id: Int?,
            @Json(name = "last_name")
            val lastName: String?,
            @Json(name = "name")
            val name: String?,
            @Json(name = "paid")
            val paid: Float?,
            @Json(name = "suspended")
            val suspended: Int?,
            @Json(name = "total")
            val total: String?,
            @Json(name = "type_bank")
            val typeBank: String?
        ) {
            data class Details(
                @Json(name = "delivered_count")
                val deliveredCount: Int?,
                @Json(name = "left_count")
                val leftCount: Int?,
                @Json(name = "undelivered_count")
                val undeliveredCount: Int?
            ) {
                fun toUserDetailsInfo() = UserDetailInfo(
                    leftCount = leftCount ?: 0,
                    deliveredCount = deliveredCount ?: 0,
                    undeliveredCount = undeliveredCount ?: 0
                )
            }
        }
    }

    fun toUserInfo() =
        UserInfo(
            user = UserModel(
                details = data.user.details.toUserDetailsInfo(),
                calculation = data.user.calculation ?: 0f,
                name = data.user.name,
                lastName = data.user.lastName
            )
        )
}