version = 0

cloudstream {
    authors     = listOf("keyiflerolsun")
    language    = "tr"
    description = "Film Ekseni ⚡️ Vizyonda ki, en güncel ve en yeni filmleri full hd kalitesinde türkçe dublaj ve altyazı seçenekleriyle 1080p olarak izleyebileceğiniz adresiniz."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("Movie")
    iconUrl = "https://www.google.com/s2/favicons?domain=www.filmekseni.net.tv&sz=%size%"
}