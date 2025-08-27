# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli    import konsol
import requests
import re, base64
import warnings
from requests.packages.urllib3.exceptions import InsecureRequestWarning

# SSL uyarılarını gizle
warnings.filterwarnings("ignore", category=InsecureRequestWarning)

class IframeKodlayici:
    @staticmethod
    def ters_cevir(metin: str) -> str:
        return metin[::-1]

    @staticmethod
    def base64_coz(encoded_string: str) -> str:
        return base64.b64decode(encoded_string).decode("utf-8")

    @staticmethod
    def iframe_parse(html_icerik: str) -> str:
        iframe_pattern = r'<iframe[^>]+src=["\']([^"\']+)["\'][^>]*>'
        match = re.search(iframe_pattern, html_icerik)
        if match:
            return match.group(1)
        return "Iframe bulunamadı"

    def iframe_coz(self, veri: str) -> str:
        if not veri.startswith("PGltZyB3aWR0aD0iMTAwJSIgaGVpZ2"):
            veri = self.ters_cevir("BSZtFmcmlGP") + veri

        try:
            iframe = self.base64_coz(veri)
            return self.iframe_parse(iframe)
        except Exception as e:
            return f"Çözümleme hatası: {e}"

# SSL doğrulamasını devre dışı bırakan oturum oluştur
oturum = requests.Session()
oturum.verify = False  # SSL doğrulamasını devre dışı bırak

# User-Agent ekle (cloudflare bypass için)
oturum.headers.update({
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
})

try:
    konsol.print("[yellow]Sayfa isteği yapılıyor...[/yellow]")
    istek = oturum.get("https://dizilla.club/dizi/the-gypsy-bride/", timeout=30)
    
    if istek.status_code != 200:
        konsol.print(f"[red]Hata: Sayfa yüklenemedi. Status Code: {istek.status_code}[/red]")
        exit()
    
    konsol.print("[green]Sayfa başarıyla yüklendi![/green]")
    
    # pdata'ları bul
    partlar = re.findall(r"pdata\[\'(.*?)'\] = \'(.*?)\';", istek.text)
    
    if not partlar:
        konsol.print("[red]pdata bulunamadı![/red]")
        konsol.print(f"Sayfa içeriği uzunluğu: {len(istek.text)}")
        # Hata ayıklama için HTML içeriğini kaydet
        with open("debug_page.html", "w", encoding="utf-8") as f:
            f.write(istek.text)
        konsol.print("[yellow]Sayfa içeriği 'debug_page.html' dosyasına kaydedildi.[/yellow]")
    else:
        konsol.print(f"[green]{len(partlar)} adet pdata bulundu![/green]")
        
        kodlayici = IframeKodlayici()
        for parca_id, parca_veri in partlar:
            try:
                iframe = kodlayici.iframe_coz(parca_veri)
                konsol.print(f"{parca_id:<6} » {iframe}")
            except Exception as parca_hata:
                konsol.print(f"[red]{parca_id:<6} » Hata: {parca_hata}[/red]")

except requests.exceptions.SSLError as ssl_hata:
    konsol.print(f"[bold red]SSL Hatası: {ssl_hata}[/bold red]")
    konsol.print("[yellow]Alternatif çözüm deneniyor...[/yellow]")
    
    # Alternatif: SSL context'i tamamen devre dışı bırak
    import ssl
    ssl._create_default_https_context = ssl._create_unverified_context
    
    # Yeniden dene
    try:
        istek = requests.get("https://dizilla.club/dizi/the-gypsy-bride/", timeout=30, verify=False)
        # ... önceki işlemleri tekrarla ...
    except Exception as alt_hata:
        konsol.print(f"[bold red]Alternatif çözüm de başarısız: {alt_hata}[/bold red]")

except requests.exceptions.Timeout:
    konsol.print("[bold red]İstek zaman aşımına uğradı![/bold red]")
    
except requests.exceptions.ConnectionError:
    konsol.print("[bold red]Bağlantı hatası! İnternet bağlantınızı kontrol edin.[/bold red]")
    
except Exception as ana_hata:
    konsol.print(f"[bold red]Beklenmeyen hata: {ana_hata}[/bold red]")