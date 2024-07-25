package com.example.sits_player_android

import FileDownloader
import Schedule
import ScheduleUpdateWorker
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.scale
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var workManager: WorkManager
    private lateinit var schedule: Schedule
    private lateinit var imageViewer: ImageView
    private lateinit var videoViewer: VideoView
    private lateinit var libPath :String
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
                ScheduleUpdateWorker.KEY_URL to "http://10.50.10.231:9876",
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
                //TODO: Start AFTER syncing is done.
                start()
            }
        }
    }

    private fun start() {    //TODO: Change to Media3 Player or ExoPlayer: See: https://www.youtube.com/watch?v=JX1fwti2LI4


        if (mediaCounter == (schedule.playlistList[playlistCounter].mediaList.size)){
                playlistCounter = (playlistCounter + 1) % schedule.playlistList.size
                mediaCounter = 0
            }
            playNext()
            mediaCounter = (mediaCounter + 1) % schedule.playlistList[playlistCounter].mediaList.size

    }
    private fun playNext(){
        val handler = android.os.Handler(Looper.getMainLooper())
        val currentMedia = schedule.playlistList[playlistCounter].mediaList[mediaCounter]
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
