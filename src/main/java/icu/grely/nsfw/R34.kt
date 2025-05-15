package icu.grely.nsfw

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.*;

class R34 {
    companion object {
        @Throws(java.lang.Exception::class)
        fun fetchRule34Links(tags: String, limit: Int): MutableList<String?> {
            val links: MutableList<String?> = ArrayList<String?>()

            val apiUrl = "https://rule34.xxx/index.php?page=dapi&s=post&q=index&limit=" + limit +
                    "&tags=" + tags.replace(" ", "+")

            val url: URL = URL(apiUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.setRequestMethod("GET")
            connection.setConnectTimeout(10000)
            connection.setReadTimeout(10000)

            val inputStream: InputStream = connection.getInputStream()

            val doc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            doc.getDocumentElement().normalize()

            val posts: NodeList = doc.getElementsByTagName("post")

            for (i in 0..<posts.getLength()) {
                val post = posts.item(i) as Element
                val fileUrl: String = post.getAttribute("file_url")
                if (!fileUrl.isEmpty()) {
                    links.add(fileUrl)
                }
            }

            inputStream.close()
            connection.disconnect()

            return links
        }
    }
}