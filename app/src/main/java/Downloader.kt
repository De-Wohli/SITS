import android.content.Context

interface Downloader{
    fun downloadFile(url: String) : Long
}