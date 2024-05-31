import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("schedule/{id}")
    suspend fun getScheduleById(@Path("id") id: String): ApiResponse<Schedule>
}
