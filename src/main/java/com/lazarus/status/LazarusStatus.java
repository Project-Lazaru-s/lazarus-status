package com.lazarus.status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

@Mod(LazarusStatus.ID)
public class LazarusStatus {
    public static final String ID = "lazarusstatus";
    private static final String TAG = "LazarusStatus";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ID, "main"), () -> "2", "2"::equals, "2"::equals);

    public LazarusStatus() {
        CHANNEL.registerMessage(0, Request.class, Request::encode, Request::decode, Request::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, Snapshot.class, Snapshot::encode, Snapshot::decode, Snapshot::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void request() {
        CHANNEL.sendToServer(new Request());
    }

    private static long worldTime(ServerPlayer player) {
        return Math.max(0, player.server.overworld().getDayTime());
    }

    private static long worldDay(ServerPlayer player) {
        return worldTime(player) / StatusClock.DAY_TICKS;
    }

    private static CompoundTag stats(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(TAG)) {
            CompoundTag data = new CompoundTag();
            data.putLong("aliveTicks", 0);
            data.putLong("lastWorldTime", worldTime(player));
            root.put(TAG, data);
        }
        CompoundTag data = root.getCompound(TAG);
        if (!data.contains("aliveTicks")) {
            // Existing 1.0.x runs retain completed days; earlier fractional hours cannot be recovered.
            data.putLong("aliveTicks", data.contains("startDay")
                    ? StatusClock.restoreDays(worldDay(player), data.getLong("startDay")) : 0);
            data.putLong("lastWorldTime", worldTime(player));
        }
        if (!data.contains("totalKills")) data.putInt("totalKills", data.getInt("zombieKills"));
        if (!data.contains("longestRun")) data.putLong("longestRun", data.getLong("aliveTicks"));
        return data;
    }

    private static void accrue(ServerPlayer player) {
        CompoundTag data = stats(player);
        long now = worldTime(player);
        long previous = data.getLong("lastWorldTime");
        data.putLong("aliveTicks", StatusClock.advance(data.getLong("aliveTicks"), previous, now,
                !player.isCreative() && !player.isSpectator()));
        data.putLong("lastWorldTime", now);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            stats(player).putLong("lastWorldTime", worldTime(player));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) accrue(player);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player
                && player.tickCount % 20 == 0) accrue(player);
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        accrue(oldPlayer);
        CompoundTag data = stats(oldPlayer).copy();
        if (event.isWasDeath()) {
            data.putLong("longestRun", Math.max(data.getLong("longestRun"), data.getLong("aliveTicks")));
            data.putInt("deaths", data.getInt("deaths") + 1);
            data.putLong("aliveTicks", 0);
            data.putInt("zombieKills", 0);
        }
        data.putLong("lastWorldTime", worldTime(player));
        player.getPersistentData().put(TAG, data);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        LivingEntity victim = event.getEntity();
        if (!isZombie(victim)) return;
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : victim.getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer != null) {
            CompoundTag data = stats(killer);
            data.putInt("zombieKills", data.getInt("zombieKills") + 1);
            data.putInt("totalKills", data.getInt("totalKills") + 1);
        }
    }

    private static boolean isZombie(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED
                || type == EntityType.ZOMBIE_VILLAGER) return true;
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) return false;
        String path = id.getPath();
        if (id.getNamespace().equals("apocalypsenow"))
            return path.matches("(walker|runner|crawler)_\\d+");
        if (id.getNamespace().equals("undead_revamp2"))
            return path.equals("theheavy") || path.equals("bomber") || path.equals("thespitter")
                    || path.equals("thesmoker") || path.equals("thehunter") || path.equals("sucker");
        return false;
    }

    private record Request() {
        private static void encode(Request packet, FriendlyByteBuf buf) { }
        private static Request decode(FriendlyByteBuf buf) { return new Request(); }
        private static void handle(Request packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player == null) return;
                accrue(player);
                CompoundTag data = stats(player);
                long survived = data.getLong("aliveTicks");
                CHANNEL.sendTo(new Snapshot(worldDay(player), survived, data.getInt("zombieKills"),
                        Math.max(survived, data.getLong("longestRun")), data.getInt("totalKills"),
                        data.getInt("deaths")), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            });
            context.get().setPacketHandled(true);
        }
    }

    public record Snapshot(long worldDay, long survivalTicks, int zombieKills,
                           long longestRunTicks, int totalKills, int deaths) {
        private static void encode(Snapshot packet, FriendlyByteBuf buf) {
            buf.writeVarLong(packet.worldDay);
            buf.writeVarLong(packet.survivalTicks);
            buf.writeVarInt(packet.zombieKills);
            buf.writeVarLong(packet.longestRunTicks);
            buf.writeVarInt(packet.totalKills);
            buf.writeVarInt(packet.deaths);
        }
        private static Snapshot decode(FriendlyByteBuf buf) {
            return new Snapshot(buf.readVarLong(), buf.readVarLong(), buf.readVarInt(),
                    buf.readVarLong(), buf.readVarInt(), buf.readVarInt());
        }
        private static void handle(Snapshot packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientStatus.open(packet)));
            context.get().setPacketHandled(true);
        }
    }
}
