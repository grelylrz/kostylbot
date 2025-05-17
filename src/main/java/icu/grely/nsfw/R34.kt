package icu.grely.nsfw

import arc.util.Log
import icu.grely.Vars.Okclient
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.InputSource;

class R34 {
    companion object {
        fun fetchR34(tags: String, limit: Int): List<String> {
            val results = mutableListOf<String>()
            try {
                val url = "https://rule34.xxx/index.php?page=dapi&s=post&q=index&json=0&limit=$limit&tags=${tags.replace(" ", "+")}"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                Okclient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful || response.body == null) return results.toList() as java.util.List<String>

                    val xml = response.body!!.string()
                    val factory = DocumentBuilderFactory.newInstance()
                    val builder = factory.newDocumentBuilder()
                    val doc: Document = builder.parse(InputSource(StringReader(xml)))
                    doc.documentElement.normalize()

                    val posts = doc.getElementsByTagName("post")
                    for (i in 0 until posts.length) {
                        val element = posts.item(i) as? Element ?: continue
                        val fileUrl = element.getAttribute("file_url")
                        if (fileUrl.isNotEmpty()) {
                            results.add(fileUrl)
                        }
                    }
                }
            } catch (e_: Exception) {
                Log.err(e_)
            }

            return results.toList() as java.util.List<String>
        }
    }
}