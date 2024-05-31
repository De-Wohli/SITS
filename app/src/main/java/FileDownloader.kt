import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.concurrent.thread

class FileDownloader(private val context: Context) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    override fun downloadFile(url: String): Long {
        var mimeType = ""
        var fileName = ""
        var threadBlah = Thread{
            mimeType = getMimeTyp(url)
            fileName = getFileName(url)
        }
        threadBlah.start()
        threadBlah.join()
        val request = DownloadManager.Request(Uri.parse(url))
            .setMimeType(mimeType)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context,"library", fileName)
        return downloadManager.enqueue(request)
    }
    private fun getMimeTyp(url:String): String {
        val urlObject = URL(url)
        val urlConnection = urlObject.openConnection()
        urlConnection.connect()
        val mimeTypeFromServer = urlConnection.contentType
        return mimeTypeFromServer
    }

    private fun getFileName(url:String):String{
        val urlObject = URL(url)
        val urlConnection = urlObject.openConnection()
        urlConnection.connect()
        val contentDisposition = urlConnection.getHeaderField("Content-Disposition")
        val filename = contentDisposition.substring(contentDisposition.indexOf("filename=")+9,contentDisposition.length)
        return filename
    }

}
