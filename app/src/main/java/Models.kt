import com.google.gson.annotations.JsonAdapter
import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val _id: String,
    val fileid: String,
    val filename: String,
    val filetype: String
)
@Serializable
data class Playlist(
    val _id: String,
    val playlistName: String,
    val mediaList: List<Media>
)
@Serializable
data class Schedule(
    val _id: String,
    val scheduleName: String,
    val playlistList: List<Playlist>
)
@Serializable
data class ApiResponse<T>(
    val message: String,
    val data: T?,
    val status: String?
)