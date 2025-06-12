package api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import okhttp3.MediaType.Companion.toMediaType

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
        @Query("desc") desc: String,
        @Query("catId") catId: Int,
        @Query("priority") priority: String
    ): Response<Unit?>

    @POST("main/requests/{id}/feedback")
    suspend fun feedback(
        @Path("id") id: Int,
        @Query("rating") rating: Int,
        @Query("text") text: String,
        @Header("Authorization") token: String,
    ): Response<Unit?>

    @GET("main/categories")
    suspend fun getAllCategories(
        @Header("Authorization") token: String,
    ): Response<List<Category>>

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
    private const val BASE_URL = "https://spotdiff.ru/fix-flow-api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory( "application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}

// Data classes
@Serializable
data class SignUpParams(val username: String, val email: String, val password: String)
@Serializable
data class SignInParams(val email: String, val password: String)
@Serializable
data class JwtResponseDto(val userId: Int, val accessToken: String, val refreshToken: String)
@Serializable
data class RequestEntityDto(
    val id: Int,
    val user: UserEntityDto,
    val title: String,
    val description: String,
    val status: RequestStatus,
    val createdAt: String,
    val updatedAt: String,
    val category: Category,
    val priority: String,
    val satisfactionRating: Int?,
    val feedbackText: String?
)
@Serializable
data class CommentEntityDto(val id: Int, val comment: String, val user: UserEntityDto, val createdAt: String)

@Serializable
data class UserEntityDto(val id: Int, val username: String, val role: UserRole, val email: String, val createdAt: String)

@Serializable
data class Category(
    val id: Int,
    val name: String
)

enum class UserRole(val title: String) {
    Employee("Сотрудник"),
    ItSupport("IT Отдел")
}

enum class RequestStatus(val title: String) {
    Open("Открыта"),
    InProgress("В процессе"),
    Closed("Закрыта")
}
