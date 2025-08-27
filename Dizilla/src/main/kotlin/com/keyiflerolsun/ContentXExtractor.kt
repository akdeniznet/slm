// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*

open class ContentX : ExtractorApi() {
    override val name            = "ContentX"
    override val mainUrl         = "https://contentx.me"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: ""
        Log.d("Kekik_${this.name}", "url » $url")

        val iSource = app.get(url, referer = extRef).text
        
        // Ana video URL'sini çıkarma
        val iExtract = Regex("""window\.openPlayer\('([^']+)'""")
            .find(iSource)
            ?.groups?.get(1)?.value 
            ?: throw ErrorLoadingException("iExtract bulunamadı")

        // Altyazıları işleme
        processSubtitles(iSource, subtitleCallback)

        // Ana video akışını işleme
        processVideoStream("${mainUrl}/source2.php?v=${iExtract}", extRef, url, callback)

        // Türkçe dublaj varsa işleme
        processDublajTrack(iSource, extRef, url, callback)
    }

    /**
     * Altyazıları işler ve callback'e gönderir
     */
    private fun processSubtitles(
        source: String, 
        subtitleCallback: (SubtitleFile) -> Unit
    ) {
        val subUrls = mutableSetOf<String>()
        
        Regex(""""file":"((?:\\\\\"|[^"])+)","label":"((?:\\\\\"|[^"])+)"""")
            .findAll(source)
            .forEach { match ->
                val (subUrlExt, subLangExt) = match.destructured

                val subUrl = subUrlExt
                    .replace("\\/", "/")
                    .replace("\\u0026", "&")
                    .replace("\\", "")
                
                val subLang = subLangExt
                    .replace("\\u0131", "ı")
                    .replace("\\u0130", "İ")
                    .replace("\\u00fc", "ü")
                    .replace("\\u00e7", "ç")
                    .replace("\\u011f", "ğ")
                    .replace("\\u015f", "ş")

                if (subUrl in subUrls) return@forEach
                subUrls.add(subUrl)

                subtitleCallback.invoke(
                    SubtitleFile(
                        lang = subLang,
                        url = fixUrl(subUrl)
                    )
                )
            }
    }

    /**
     * Video akışını işler ve ExtractorLink oluşturur
     */
    private suspend fun processVideoStream(
        streamUrl: String,
        referer: String,
        originalUrl: String,
        callback: (ExtractorLink) -> Unit
    ) {
        val vidSource = app.get(streamUrl, referer = referer).text
        
        val vidExtract = Regex("""file":"([^"]+)""")
            .find(vidSource)
            ?.groups?.get(1)?.value 
            ?: throw ErrorLoadingException("vidExtract bulunamadı")
        
        val m3uLink = vidExtract.replace("\\", "")

        createExtractorLink(m3uLink, originalUrl, callback)
    }

    /**
     * Türkçe dublaj track'ini işler
     */
    private suspend fun processDublajTrack(
        source: String,
        referer: String,
        originalUrl: String,
        callback: (ExtractorLink) -> Unit
    ) {
        val iDublaj = Regex(""","([^']+)","Türkçe"""")
            .find(source)
            ?.groups?.get(1)?.value

        iDublaj?.let { dublajId ->
            val dublajSource = app.get("${mainUrl}/source2.php?v=${dublajId}", referer = referer).text
            
            val dublajExtract = Regex("""file":"([^"]+)""")
                .find(dublajSource)
                ?.groups?.get(1)?.value 
                ?: throw ErrorLoadingException("dublajExtract bulunamadı")
            
            val dublajLink = dublajExtract.replace("\\", "")

            createExtractorLink(dublajLink, originalUrl, callback)
        }
    }

    /**
     * ExtractorLink oluşturur ve callback'e gönderir
     */
    private fun createExtractorLink(
        url: String,
        refererUrl: String,
        callback: (ExtractorLink) -> Unit
    ) {
        callback.invoke(
            ExtractorLink(
                source = this.name,
                name = this.name,
                url = url,
                referer = refererUrl,
                quality = Qualities.Unknown.value,
                isM3u8 = true,
                headers = mapOf(
                    "Referer" to refererUrl,
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Norton/124.0.0.0"
                )
            )
        )
    }
}