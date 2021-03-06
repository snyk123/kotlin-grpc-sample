package grpc.service.user

import kotlinx.coroutines.async
import services.GetRequest
import services.KeyValueServiceGrpcKt.KeyValueServiceKtStub
import services.UserRequest
import services.UserResponse
import services.UserServiceGrpcKt.UserServiceImplBase

class UserService(private val keyValue: KeyValueServiceKtStub) : UserServiceImplBase() {

    override suspend fun getUser(request: UserRequest): UserResponse {
        suspend fun getValue(key: String) = keyValue.get(
                GetRequest
                        .newBuilder()
                        .setKey(request.name + key)
                        .build() ?: throw IllegalArgumentException("key not found")
        )

        val email = async { getValue(".email") }
        val country = async { getValue(".country") }
        val active = async { getValue(".active") }

        return UserResponse
                .newBuilder()
                .setName(request.name ?: throw IllegalArgumentException("name can not be null"))
                .setEmailAddress(email.await().value)
                .setCountry(country.await().value)
                .setActive(active.await().value?.toBoolean() ?: false)
                .build()
    }

}
