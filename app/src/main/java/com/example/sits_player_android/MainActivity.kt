package com.example.sits_player_android

import FileDownloader
import Schedule
import ScheduleUpdateWorker
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.scale
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sits_player_android.ui.theme.SITSPlayerAndroidTheme
import kotlinx.serialization.json.Json
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

lateinit var schedule: Schedule

class MainActivity : ComponentActivity() {
    private lateinit var workManager: WorkManager
    private lateinit var schedule: Schedule
    lateinit var imageViewer: ImageView
    lateinit var videoViewer: VideoView
    lateinit var libPath :String
    private var playlistCounter = 0
    private var mediaCounter = 0
    private var width = 0
    private var height = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        imageViewer = findViewById(R.id.image_viewer)
        videoViewer = findViewById(R.id.video_viewer)
        val displayMetrics = DisplayMetrics()
        displayMetrics.setTo(resources.displayMetrics)
        height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels
        libPath = getExternalFilesDir("library")?.path!!
        workManager = WorkManager.getInstance(this)
        val workRequest = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>().setInputData(
            workDataOf(
                ScheduleUpdateWorker.KEY_URL to "http://10.50.10.152:9876",
                ScheduleUpdateWorker.KEY_URL_ID to "6659bf4e47b1baaf41343031"
            )
        )
            .build()
        workManager.enqueue(workRequest)
        val workId = workRequest.id
        val downloader = FileDownloader(this)
        workManager.getWorkInfoByIdLiveData(workId).observe(this) {
            if (it.state.isFinished) {
                val scheduleJson = it.outputData.getString(ScheduleUpdateWorker.KEY_RESULT_JSON)!!
                schedule = Json.decodeFromString<Schedule>(scheduleJson)
                for (playlist in schedule.playlistList) {
                    for (media in playlist.mediaList) {
                        if (!File(libPath, media.filename).exists()) {
                            downloader.downloadFile("http://10.50.10.152:9876/download/${media.fileid}")
                        }
                    }
                }
                start()
            }
        }
    }
    fun start() {

            if (mediaCounter == (schedule.playlistList[playlistCounter].mediaList.size)){
                playlistCounter = (playlistCounter + 1) % schedule.playlistList.size
                mediaCounter = 0
            }
            playNext()
            mediaCounter = (mediaCounter + 1) % schedule.playlistList[playlistCounter].mediaList.size

    }
    private fun playNext(){
        val handler = android.os.Handler(Looper.getMainLooper())
        val currentMedia = schedule.playlistList[playlistCounter].mediaList.get(mediaCounter)
        when (currentMedia.filetype) {
            "image" -> {
                videoViewer.visibility = View.INVISIBLE
                imageViewer.visibility = View.VISIBLE
                val path = File(libPath,currentMedia.filename).path
                var bitmap = BitmapFactory.decodeFile(path)
                bitmap = bitmap.scale(width, height)
                imageViewer.setImageBitmap(bitmap)
                handler.postDelayed(
                    { start() }
                    ,
                    10 * 1000L)
            }
            "video" -> {
                videoViewer.visibility = View.VISIBLE
                imageViewer.visibility = View.INVISIBLE
                val layoutParams = videoViewer.layoutParams
                layoutParams.width = width
                layoutParams.height = height
                videoViewer.layoutParams = layoutParams
                val path = File(libPath,currentMedia.filename).path
                videoViewer.setVideoPath(path)
                videoViewer.start()
                handler.postDelayed(
                    {
                        if (videoViewer.isPlaying){
                            videoViewer.stopPlayback()
                        }
                        start()
                    }
                    ,
                    10* 1000L)
            }
        }
    }
}
