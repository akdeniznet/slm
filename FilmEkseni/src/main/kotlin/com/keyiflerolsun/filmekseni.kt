// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class filmekseni : MainAPI() {
    override var mainUrl              = "https://filmekseni.net"
    override var name                 = "Film Ekseni"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie)

    override val mainPage = mainPageOf(
        "${mainUrl}/tur/aile-filmleri/"				    to "Aile",	
		"${mainUrl}/tur/aksiyon-filmleri/" 			    to "Aksiyon",	
		"${mainUrl}/tur/animasyon-filmleri/"			to "Animasyon",
		"${mainUrl}/tur/bilim-kurgu-filmleri/" 		    to "Bilim",
		"${mainUrl}/tur/biyografi-filmleri/"			to "Biyografi",
		"${mainUrl}/tur/dram-filmleri/"				    to "Dram",
		"${mainUrl}/tur/fantastik-filmler/" 			to "Fantastik",
		"${mainUrl}/tur/gerilim-filmleri/"				to "Gerilim",	
		"${mainUrl}/tur/gizem-filmleri/"				to "Gizem",
		"${mainUrl}/tur/komedi-filmleri/"				to "Komedi",
		"${mainUrl}/tur/korku-filmleri/"				to "Korku",
		"${mainUrl}/tur/macera-filmleri/"				to "Macera",	
		"${mainUrl}/tur/romantik-filmler/"			    to "Romantik",
		"${mainUrl}/tur/savas-filmleri/"				to "Savaş",
		"${mainUrl}/tur/suc-filmleri/"  	    		to "Kriminal",
		"${mainUrl}/tur/tarih-filmleri/"                to "Tarih"
    
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}").document
        val home     = document.select("div.col-6.col-sm-3.poster-container").mapNotNull { it.toMainPageResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val title     = this.selectFirst("h2")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/search?qr=${query}").document
        return document.select("h2.truncate").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("div.title a")?.text() ?: return null
        val href      = fixUrlNull(this.selectFirst("div.title a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title           = document.selectFirst("h1.me-2")?.html()?.split("<br>")?.getOrNull(0)?.trim() ?: return null
        val poster          = fixUrlNull(document.selectFirst("img")?.attr("src"))
        //val poster          = fixUrlNull(document.selectFirst("[property='og:image']")?.attr("content"))
        val description     = document.selectFirst("article.text-white p")?.text()?.trim()
        val yeartext        = document.select("div.d-flex.flex-column.text-nowrap")
        .find { it.text().contains("Yıl") }
        ?.selectFirst("strong")
        ?.text()
        ?.trim()
        val year: Int?      = yeartext?.filter { it.isDigit() }?.toIntOrNull()
        //val year            = document.selectFirst("div.truncate")?.text()?.trim()?.toIntOrNull()
        val tags            = document.select("div.pb-2 a").map { it.text() }
        val rating          = document.selectFirst("div.rate span")?.text()?.trim()?.toRatingInt()
        val durationText    = document.select("div.d-flex.flex-column.text-nowrap")
        .find { it.text().contains("Süre") }
        ?.selectFirst("strong")
        ?.text()
        ?.trim()
        val duration: Int?  = durationText?.filter { it.isDigit() }?.toIntOrNull()
        //val recommendations = document.select("div.srelacionados article").mapNotNull { it.toRecommendationResult() }
        //val actors          = document.select("span.valor a").map { Actor(it.text()) }
        //val trailer         = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)?.let { "https://www.youtube.com/embed/$it" }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl       = poster
            this.plot            = description
            this.year            = year
            this.tags            = tags
            // this.rating          = rating
            this.duration        = duration
            //this.recommendations = recommendations
            //addActors(actors)
            //addTrailer(trailer)
        }
    }

    private fun Element.toRecommendationResult(): SearchResponse? {
        val title     = this.selectFirst("a img")?.attr("alt") ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("a img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        // Log.d("STF", "data » ${data}")
        // val document = app.get(data).document
        // var iframe   = fixUrlNull(document.selectFirst("div.card-video iframe")?.attr("data-src")) ?: return false
        // Log.d("STF", "iframe » ${iframe}")

        // loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
        
        // return true


        Log.d("FIS", "data » $data")
        val document = app.get(data).document

        document.select("div.card-video iframe").forEach {
            if (it.toString().contains("#source")) {
                val url = it.toString().substringAfter("<iframe src=\"").substringBefore("\"")
                val doci = app.get(
                    url,
                    headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0) Gecko/20100101 Firefox/135.0",
                        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Accept-Language" to " en-US,en;q=0.5",
                    ), referer = "https://www.filmizlesene.pro/"
                )
                var iframe = doci.document.select("iframe").attr("src")
                if (iframe.contains("/vidmo/")) {
                    val doci2 = app.get(
                        iframe,
                        headers = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0) Gecko/20100101 Firefox/135.0",
                            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                            "Accept-Language" to " en-US,en;q=0.5",
                        ), referer = "https://www.filmizlesene.pro/"
                    )
                    var iframe2 = doci2.document.select("iframe").attr("src")
                    loadExtractor(iframe2, iframe, subtitleCallback, callback)
                } else if (iframe.contains("/hdplayer/drive/")) {

                } else {
                    loadExtractor(iframe, url, subtitleCallback, callback)
                }
            }
        }

        return true
    }
}