package icu.grely.nsfw

import icu.grely.Vars.*
import arc.util.Log
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object R34 {
    private val mapper = jacksonObjectMapper()
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GelbooruPost(
        val file_url: String?
    )

    data class GelbooruResponse(
        val post: List<GelbooruPost>?
    )
    @JvmStatic
    fun fetchGelbooru(tags: String, limit: Int): List<String> {
        val results = mutableListOf<String>()
        try {
            val url =
                "https://gelbooru.com/index.php?page=dapi&s=post&q=index&json=1&limit=$limit&tags=${tags.replace(" ", "+")}"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .get()
                .build()

            Okclient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return results.toList()

                val bodyStr = response.body!!.string()

                val parsed: GelbooruResponse = mapper.readValue(bodyStr)

                parsed.post?.forEach {
                    it.file_url?.let { url -> results.add(url) }
                }
            }
        } catch (e_: Exception) {
            Log.err(e_)
        }

        return results.toList()
    }
}