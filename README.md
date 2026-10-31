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

1. GitHub'daki **Releases** sayfasından `AFKFarm-1.0.0.jar` dosyasının linkini kopyala.
2. Container'ın içinde sunucunun `plugins` klasörünü bul:

   ```bash
   find / -type d -name plugins -not -path "/proc/*" 2>/dev/null
   ```

3. Jar'ı o klasöre indir (`/sunucu/plugins` yerine bulduğun yolu yaz):

   ```bash
   curl -L -o /sunucu/plugins/AFKFarm.jar https://github.com/KULLANICI/AFK-Farm-Plugin/releases/download/v1.0.0/AFKFarm-1.0.0.jar
   ```

   Repo gizliyse (private) bu link çalışmaz. O zaman jar'ı bilgisayarına indirip Docker'ın çalıştığı makineden container'a kopyala:

   ```bash
   docker cp AFKFarm-1.0.0.jar <container-adı>:/sunucu/plugins/AFKFarm.jar
   ```

4. Sunucuyu yeniden başlat (`/reload` kullanma):

   ```bash
   docker restart <container-adı>
   ```

5. Oyunda `/afkfarm` yazarak plugin'in yüklendiğini kontrol et.

## Derleme

Java 25 gerekir, Gradle wrapper eksikse kendisi indirir.

```bash
./gradlew build
```

Jar dosyası `build/libs/` klasörüne çıkar.
