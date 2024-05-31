import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.readJson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScheduleUpdateWorker(private val appContext : Context,
                           private val params: WorkerParameters
) : CoroutineWorker(appContext,params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val url = inputData.getString(KEY_URL)
            val id = inputData.getString(KEY_URL_ID)

            var result = if (id != null && url != null) {
                    callApi(url, id)
            }
            else{
                ""
            }
            Result.Success(
                workDataOf(
                    KEY_RESULT_JSON to result
                )
            )
        }
    }

    companion object{
        const val KEY_URL="KEY_URL"
        const val KEY_URL_ID = "KEY_URL_ID"
        const val KEY_RESULT_JSON="KEY_RESULT_JSON"
    }

    private suspend fun callApi(url:String, id:String): String {
        // Use a library like Retrofit to make the API call
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        var response: ApiResponse<Schedule>? = null
        // Make the API call and get the JSON string
        withContext(Dispatchers.IO){
            response = apiService.getScheduleById(id)

        }
        return Json.encodeToString(response?.data)
    }
}