# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli    import konsol
# from cloudscraper import CloudScraper as Session
from httpx        import Client as Session
from parsel       import Selector

mainUrl = "https://dizilla.club/trend"
oturum  = Session()

istek   = oturum.get(mainUrl)
secici  = Selector(istek.text)
c_key   = secici.css("input[name=cKey]::attr(value)").get()
c_value = secici.css("input[name=cValue]::attr(value)").get()

# Debug: İstek durumu ve çerezleri kontrol et
konsol.print(f"[bold yellow]İstek Durumu:[/] {istek.status_code}")
konsol.print(f"[bold yellow]Mevcut Çerezler:[/] {dict(istek.cookies)}")
konsol.print(f"[bold yellow]Headers:[/] {dict(istek.headers)}")

oturum.cookies.clear()
oturum.headers.update({
    "Accept"           : "application/json, text/javascript, */*; q=0.01",
    "X-Requested-With" : "XMLHttpRequest",
    "Referer"          : f"{mainUrl}/",
})
oturum.cookies.set("showAllDaFull", "true")

# PHPSESSID kontrolü ile güvenli çerez ayarlama
if "PHPSESSID" in istek.cookies:
    oturum.cookies.set("PHPSESSID", istek.cookies["PHPSESSID"])
    konsol.print(f"[bold green]PHPSESSID ayarlandı:[/] {istek.cookies['PHPSESSID']}")
else:
    konsol.print("[bold red]PHPSESSID çerezi bulunamadı![/]")
    
    # Alternatif session çerezlerini ara
    session_cookie_found = False
    for cookie_name in istek.cookies:
        if any(keyword in cookie_name.lower() for keyword in ['session', 'sess', 'php', 'id']):
            oturum.cookies.set(cookie_name, istek.cookies[cookie_name])
            konsol.print(f"[bold yellow]Alternatif çerez ayarlandı:[/] {cookie_name} = {istek.cookies[cookie_name]}")
            session_cookie_found = True
            break
    
    if not session_cookie_found:
        konsol.print("[bold red]Hiçbir session çerezi bulunamadı![/]")

# Debug: Oturum çerezlerini göster
konsol.print(f"[bold cyan]Oturum Çerezleri:[/] {dict(oturum.cookies)}")

istek = oturum.post(
    f"{mainUrl}/bg/searchcontent",
    data = {
        "cKey"       : c_key,
        "cValue"     : c_value,
        "searchterm" : "the"
    }
)

# Debug: POST isteği sonrası durum
konsol.print(f"[bold magenta]POST İstek Durumu:[/] {istek.status_code}")
konsol.print(f"[bold magenta]POST Response:[/] {istek.text[:200]}...")  # İlk 200 karakter

try:
    json_response = istek.json()
    if "data" in json_response and "result" in json_response["data"]:
        konsol.print(json_response["data"]["result"])
    else:
        konsol.print("[bold red]JSON yanıtında 'data.result' bulunamadı![/]")
        konsol.print(json_response)
except Exception as e:
    konsol.print(f"[bold red]JSON parse hatası:[/] {e}")
    konsol.print(f"[bold red]Ham yanıt:[/] {istek.text}")