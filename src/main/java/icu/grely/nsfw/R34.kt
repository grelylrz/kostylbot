package icu.grely.nsfw

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class R34 {
    companion object {
        @Throws(Exception::class)
        fun fetchRule34Links(tags: String, limit: Int): MutableList<String?> {
            val links: MutableList<String?> = ArrayList<String?>()

            val apiUrl = "https://rule34.xxx/index.php?page=dapi&s=post&q=index&limit=" + limit +
                    "&tags=" + tags.replace(" ", "+")

            val doc: Document = Jsoup.connect(apiUrl).userAgent("Mozilla").timeout(10000).get()
            val posts: Elements = doc.select("post")

            for (post in posts) {
                val fileUrl: String = post.attr("file_url")
                if (!fileUrl.isEmpty()) {
                    links.add(fileUrl)
                }
            }

            return links
        }
    }
}