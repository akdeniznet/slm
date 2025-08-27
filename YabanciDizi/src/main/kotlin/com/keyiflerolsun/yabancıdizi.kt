// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class yabancıdizi : MainAPI() {
    override var mainUrl              = "https://yabancidizi.so"
    override var name                 = "Yabancı Dizi"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val hasChromecastSupport = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie)

    override val mainPage = mainPageOf(
        "${mainUrl}/film/tur/aile-izle/"				to "Aile",	
		"${mainUrl}/film/tur/aksiyon-izle-1/"			to "Aksiyon",	
		"${mainUrl}/film/tur/animasyon-izle/"			to "Animasyon",
		"${mainUrl}/film/tur/bilim-kurgu-izle-1/"		to "Bilim",
		"${mainUrl}/film/tur/biyografi-izle/"			to "Biyografi",
		"${mainUrl}/film/tur/dram-izle/"				to "Dram",
		"${mainUrl}/film/tur/fantastik-izle/"			to "Fantastik",
		"${mainUrl}/film/tur/gerilim-izle/"				to "Gerilim",	
		"${mainUrl}/film/tur/gizem-izle/"				to "Gizem",
		"${mainUrl}/film/tur/komedi-izle/"				to "Komedi",
		"${mainUrl}/film/tur/korku-izle/"				to "Korku",
		"${mainUrl}/film/tur/macera-izle/"				to "Macera",	
		"${mainUrl}/film/tur/muzikal-izle/"				to "Müzikal",
		"${mainUrl}/film/tur/polisiye-izle/"		    to "Polisiye",
		"${mainUrl}/film/tur/romantik-izle-1/"			to "Romantik",
		"${mainUrl}/film/tur/savas-izle/"				to "Savaş",
		"${mainUrl}/film/tur/spor-izle/"			    to "Spor",
		"${mainUrl}/film/tur/suc/"						to "Kriminal",
		"${mainUrl}/film/tur/tarih-izle/"               to "Tarih",
		"${mainUrl}/film/tur/western-izle/"				to "Western"
    
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}").document
        val home     = document.select("li.mofy-moviesli").mapNotNull { it.toMainPageResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val title     = this.selectFirst("li.mofy-moviesli span>a")?.text() ?: return null
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

        val title           = document.selectFirst("h1")?.text()?.trim() ?: return null
        //val poster          = fixUrlNull(document.selectFirst("div.poster img")?.attr("src"))
        val poster          = fixUrlNull(document.selectFirst("[property='og:image']")?.attr("content"))
        val description     = document.selectFirst("div.series-summary-wrapper p")?.text()?.trim()
        val year            = document.selectFirst("div.truncate")?.text()?.trim()?.toIntOrNull()
        val tags            = document.select("a[data-navigo]").select("a[title]").map { it.text() }
        val rating          = document.selectFirst("div.color-imdb")?.text()?.trim()?.toRatingInt()
        val duration        = document.selectFirst("span.runtime")?.text()?.split(" ")?.first()?.trim()?.toIntOrNull()
        val recommendations = document.select("div.srelacionados article").mapNotNull { it.toRecommendationResult() }
        val actors          = document.select("span.valor a").map { Actor(it.text()) }
        val trailer         = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)?.let { "https://www.youtube.com/embed/$it" }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl       = poster
            this.plot            = description
            this.year            = year
            this.tags            = tags
            this.rating          = rating
            this.duration        = duration
            this.recommendations = recommendations
            addActors(actors)
            addTrailer(trailer)
        }
    }

    private fun Element.toRecommendationResult(): SearchResponse? {
        val title     = this.selectFirst("a img")?.attr("alt") ?: return null
        val href      = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("a img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        Log.d("STF", "data » ${data}")
        val document = app.get(data).document

        // TODO:
        // loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)

        return true
    }
}