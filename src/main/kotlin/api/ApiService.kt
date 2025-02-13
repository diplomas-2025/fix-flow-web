package api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// API Service interface
interface ApiService {
    @POST("users/security/sign-up")
    suspend fun signUp(@Body params: SignUpParams): Response<JwtResponseDto>

    @POST("users/security/sign-in")
    suspend fun signIn(@Body params: SignInParams): Response<JwtResponseDto>

    @GET("main/requests")
    suspend fun getAllRequests(@Header("Authorization") token: String): List<RequestEntityDto>

    @POST("main/requests")
    suspend fun createRequest(
        @Header("Authorization") token: String,
        @Query("title") title: String,
        @Query("desc") desc: String
    ): Response<Unit?>

    @GET("main/requests/{id}/commands")
    suspend fun getAllCommands(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<CommentEntityDto>

    @POST("main/requests/{id}/commands")
    suspend fun createCommand(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Query("command") command: String
    ): CommentEntityDto

    @PATCH("main/requests/{id}/status")
    suspend fun updateStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Query("status") status: RequestStatus
    )

    @GET("main/requests/{id}")
    suspend fun getRequestById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): RequestEntityDto

    @GET("main/requests/user")
    suspend fun getAllRequestsForUser(@Header("Authorization") token: String): List<RequestEntityDto>

    @GET("main/user")
    suspend fun getUser(@Header("Authorization") token: String): UserEntityDto
}

// Retrofit instance
object RetrofitClient {
    private const val BASE_URL = "https://spotdiff.ru/tech-flow-api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// Data classes
data class SignUpParams(val username: String, val email: String, val password: String)
data class SignInParams(val email: String, val password: String)
data class JwtResponseDto(val userId: Int, val accessToken: String, val refreshToken: String)
data class RequestEntityDto(
    val id: Int,
    val user: UserEntityDto,
    val title: String,
    val description: String,
    val status: RequestStatus,
    val createdAt: String,
    val updatedAt: String
)
data class CommentEntityDto(val id: Int, val comment: String, val user: UserEntityDto, val createdAt: String)
data class UserEntityDto(val id: Int, val username: String, val role: UserRole, val email: String, val createdAt: String)

enum class UserRole(val title: String) {
    Employee("Сотрудник"),
    ItSupport("IT Отдел")
}

enum class RequestStatus(val title: String) {
    Open("Открыта"),
    InProgress("В процессе"),
    Closed("Закрыта")
}
