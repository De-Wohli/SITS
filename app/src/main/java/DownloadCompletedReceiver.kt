import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadCompletedReceiver: BroadcastReceiver() {
    private  lateinit var downloadManager: DownloadManager
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }
        if(intent?.action == "android.intent.DownloadManager.ACTION_DOWNLOAD_COMPLETE"){
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if(id != -1L){
                println("Download with ID $id finished")
            }
        }
    }
}