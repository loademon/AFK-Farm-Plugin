# AFKFarm

Paper 26.3 için AFK farm plugin'i. Farmının başında `/afkfarm add <isim>` yazarsın, plugin oraya görünmez bir sahte oyuncu bırakır. Sen sunucudan çıksan da farm, yanında gerçek bir oyuncu varmış gibi çalışmaya devam eder:

- Chunk'lar yüklü kalır (sunucunun simulation distance ayarı kadar alan)
- Ekinler büyür, mob'lar doğal olarak doğar ve hemen kaybolmaz
- Redstone, hopper, köylüler ve demir golemler normal çalışır

Sahte oyuncu sekme listesinde ve oyuncu sayısında görünmez, kimse onu göremez. Creative modda ve ölümsüz olduğu için mob'lar ona saldırmaz. Gece atlamayı engellemez, phantom doğurmaz, başarım (advancement) almaz.

## Komutlar

| Komut | Açıklama |
| --- | --- |
| `/afkfarm add <isim>` | Durduğun yere farm kaydeder ve sahte oyuncuyu koyar |
| `/afkfarm remove <isim>` | Farmı siler |
| `/afkfarm list` | Farmlarını ve çalışıp çalışmadığını listeler |

`/afk` kısaltması da çalışır.

## Yetkiler

| Yetki | Varsayılan | Açıklama |
| --- | --- | --- |
| `afkfarm.use` | herkes | Kendi farmlarını ekleme, silme, listeleme |
| `afkfarm.admin` | op | Herkesin farmını yönetme, farm limitine takılmama |

## Ayarlar

`plugins/AFKFarm/config.yml`:

```yaml
max-farms-per-player: 2
```

Her farm sunucuya bir oyuncu kadar yük getirir, limiti buna göre ayarla. Farmlar `plugins/AFKFarm/farms.yml` dosyasında tutulur ve sunucu açılınca otomatik başlar.

## Bilmen gerekenler

- Sahte oyuncu tam olarak komutu yazdığın yerde durur. Basınç plakası (pressure plate) veya tripwire üstünde durarak kaydetme, onları tetikler.
- Paper'ın oyuncu başına mob limiti açıksa sahte oyuncu da kendi mob limitini getirir, yani tam bir oyuncu kadar mob doğar.
- Plugin sunucunun iç koduna bağlıdır ve **Paper 26.3-230** için derlenmiştir. Paper'ı yeni bir Minecraft sürümüne (ör. 26.4) güncellersen plugin'in yeniden derlenmesi gerekir.

## Docker sunucusuna kurulum

1. Container'ın içinde sunucunun `plugins` klasörünü bul (itzg imajında `/data/plugins`):

   ```bash
   find / -type d -name plugins -not -path "/proc/*" 2>/dev/null
   ```

2. Eski sürümü sil ve son sürümü indir. Bu komut kurulumda da güncellemede de aynıdır:

   ```bash
   rm -f /data/plugins/AFKFarm*.jar
   curl -L -o /data/plugins/AFKFarm.jar https://github.com/loademon/AFK-Farm-Plugin/releases/latest/download/AFKFarm.jar
   ```

3. Sunucuyu yeniden başlat (`/reload` kullanma):

   ```bash
   docker restart <container-adı>
   ```

4. Oyunda `/afkfarm` yazarak plugin'in yüklendiğini kontrol et.

## Derleme

Java 25 gerekir, Gradle wrapper eksikse kendisi indirir.

```bash
./gradlew build
```

Jar dosyası `build/libs/` klasörüne çıkar.
