package com.loademon.afkfarm.command;

import com.loademon.afkfarm.fake.FakePlayers;
import com.loademon.afkfarm.farm.Farm;
import com.loademon.afkfarm.farm.FarmStore;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /afkfarm add|remove|list */
public final class AfkFarmCommand implements BasicCommand {

    public static final String USE_PERMISSION = "afkfarm.use";
    public static final String ADMIN_PERMISSION = "afkfarm.admin";

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list");

    private final FarmStore farms;
    private final FakePlayers fakePlayers;
    private final IntSupplier maxFarmsPerPlayer;

    public AfkFarmCommand(final FarmStore farms, final FakePlayers fakePlayers, final IntSupplier maxFarmsPerPlayer) {
        this.farms = farms;
        this.fakePlayers = fakePlayers;
        this.maxFarmsPerPlayer = maxFarmsPerPlayer;
    }

    @Override
    public void execute(final CommandSourceStack source, final String[] args) {
        final CommandSender sender = source.getSender();
        if (args.length == 0) {
            this.sendUsage(sender);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add" -> this.add(sender, args);
            case "remove" -> this.remove(sender, args);
            case "list" -> this.list(sender);
            default -> this.sendUsage(sender);
        }
    }

    private void add(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendRichMessage("<red>Bu komutu sadece oyuncular kullanabilir.");
            return;
        }
        if (args.length < 2) {
            sender.sendRichMessage("<red>Kullanım: /afkfarm add <isim>");
            return;
        }

        final String name = args[1];
        if (!VALID_NAME.matcher(name).matches()) {
            sender.sendRichMessage("<red>Farm ismi 1-16 karakter olmalı ve sadece harf, rakam ve _ içermeli.");
            return;
        }
        if (this.farms.get(name) != null) {
            sender.sendRichMessage("<red>'" + name + "' isminde bir farm zaten var.");
            return;
        }

        final int limit = this.maxFarmsPerPlayer.getAsInt();
        if (!player.hasPermission(ADMIN_PERMISSION) && this.farms.countOwnedBy(player.getUniqueId()) >= limit) {
            sender.sendRichMessage("<red>En fazla " + limit + " farm kaydedebilirsin. Önce birini sil: /afkfarm remove <isim>");
            return;
        }

        final Farm farm = Farm.at(name, player.getUniqueId(), player.getName(), player.getLocation());
        if (!this.fakePlayers.spawn(farm)) {
            sender.sendRichMessage("<red>Farm başlatılamadı, sunucu konsoluna bak.");
            return;
        }
        this.farms.add(farm);

        final Location at = player.getLocation();
        sender.sendRichMessage("<green>'" + name + "' farmı kaydedildi (" + at.getBlockX() + ", " + at.getBlockY() + ", " + at.getBlockZ()
            + "). Sunucudan çıksan da burada bir oyuncu varmış gibi çalışacak.");
    }

    private void remove(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendRichMessage("<red>Kullanım: /afkfarm remove <isim>");
            return;
        }

        final Farm farm = this.farms.get(args[1]);
        if (farm == null || !this.canManage(sender, farm)) {
            sender.sendRichMessage("<red>Bu isimde bir farmın yok. Farmlarını görmek için: /afkfarm list");
            return;
        }

        this.fakePlayers.despawn(farm.name());
        this.farms.remove(farm.name());
        sender.sendRichMessage("<green>'" + farm.name() + "' farmı silindi.");
    }

    private void list(final CommandSender sender) {
        final List<Farm> visible = this.visibleFarms(sender);
        if (visible.isEmpty()) {
            sender.sendRichMessage("<gray>Kayıtlı farm yok. Farmının başında /afkfarm add <isim> yaz.");
            return;
        }

        sender.sendRichMessage("<gold>Farmlar (" + visible.size() + "):");
        for (final Farm farm : visible) {
            final String state = this.fakePlayers.isSpawned(farm.name()) ? "<green>çalışıyor" : "<red>durdu (dünya yüklü değil)";
            sender.sendRichMessage("<yellow>" + farm.name() + " <gray>- " + farm.world() + " "
                + (int) Math.floor(farm.x()) + ", " + (int) Math.floor(farm.y()) + ", " + (int) Math.floor(farm.z())
                + " - sahibi " + farm.ownerName() + " - " + state);
        }
    }

    private void sendUsage(final CommandSender sender) {
        sender.sendRichMessage("<gold>/afkfarm add <isim> <gray>- durduğun yere AFK farm kaydeder");
        sender.sendRichMessage("<gold>/afkfarm remove <isim> <gray>- farmı siler");
        sender.sendRichMessage("<gold>/afkfarm list <gray>- farmlarını listeler");
    }

    private boolean canManage(final CommandSender sender, final Farm farm) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            return true;
        }
        return sender instanceof final Player player && player.getUniqueId().equals(farm.owner());
    }

    private List<Farm> visibleFarms(final CommandSender sender) {
        return this.farms.all().stream().filter(farm -> this.canManage(sender, farm)).toList();
    }

    @Override
    public Collection<String> suggest(final CommandSourceStack source, final String[] args) {
        if (args.length <= 1) {
            final String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream().filter(sub -> sub.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            final String prefix = args[1].toLowerCase(Locale.ROOT);
            return this.visibleFarms(source.getSender()).stream()
                .map(Farm::name)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
        }
        return List.of();
    }

    @Override
    public String permission() {
        return USE_PERMISSION;
    }
}
