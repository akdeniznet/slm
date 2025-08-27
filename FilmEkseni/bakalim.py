# ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

from Kekik.cli import konsol
from cloudscraper import CloudScraper
from parsel import Selector

oturum = CloudScraper()
istek = oturum.get("https://filmekseni.net/tamamlamadik-mi-hala/")
secici = Selector(istek.text)

def extract_iframes():
    # Tüm card-video div'lerini bul
    video_divs = secici.css('div.card-video')
    
    if not video_divs:
        konsol.print("[red]Hata: Video div'leri bulunamadı![/red]")
        return []
    
    iframe_list = []
    
    for div in video_divs:
        # Vidmoly iframe'ini kontrol et
        vidmoly = div.css('iframe.vidmoly::attr(data-src)').get()
        if vidmoly:
            iframe_list.append({
                'type': 'vidmoly',
                'url': vidmoly if vidmoly.startswith('http') else f'https:{vidmoly}'
            })
        
        # Eksenload iframe'ini kontrol et
        eksenload = div.css('iframe.vip::attr(data-src)').get()
        if eksenload:
            iframe_list.append({
                'type': 'eksenload',
                'url': eksenload if eksenload.startswith('http') else f'https:{eksenload}'
            })
    
    return iframe_list

iframes = extract_iframes()

if not iframes:
    konsol.print("[red]Hata: Hiç iframe bulunamadı![/red]")
else:
    konsol.print("[bold green]Bulunan İframe'ler:[/bold green]")
    for idx, iframe in enumerate(iframes, 1):
        konsol.print(f"{idx}. [yellow]{iframe['type']}[/yellow]: {iframe['url']}")

    # İlk iframe'i otomatik olarak işleme alalım (isteğe bağlı)
    if iframes:
        konsol.print("\n[bold]İlk iframe işleniyor:[/bold]")
        selected = iframes[0]
        
        oturum.headers.update({"Referer": "https://filmekseni.net/tamamlamadik-mi-hala/"})
        istek = oturum.get(selected['url'])
        
        if not istek.ok:
            konsol.print(f"[red]Hata: İstek başarısız - {istek.status_code}[/red]")
        else:
            # Eksenload için şifre çözme işlemi
            if selected['type'] == 'eksenload':
                try:
                    from re import search
                    from Kekik.Sifreleme import CryptoJS
                    
                    cryptData = search(r"CryptoJS\.AES\.decrypt\(\"(.*)\",\"", istek.text).group(1)
                    cryptPass = search(r"\",\"(.*)\"\);", istek.text).group(1)
                    
                    decrypted = CryptoJS.decrypt(cryptPass, cryptData)
                    video_url = search(r"file: \'(.*)',", decrypted).group(1)
                    konsol.print(f"[green]Video URL:[/green] {video_url}")
                except Exception as e:
                    konsol.print(f"[red]Hata: Şifre çözme sırasında bir sorun oluştu - {str(e)}[/red]")
            # Vidmoly için farklı işlem gerekebilir
            else:
                konsol.print("[yellow]Vidmoly iframe'i bulundu, işlem için özel kod gerekebilir.[/yellow]")
                konsol.print(f"Sayfa içeriği: {istek.text[:200]}...")  # İlk 200 karakteri göster