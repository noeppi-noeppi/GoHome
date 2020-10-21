package io.github.noeppi_noeppi.mods.gohome;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeData extends WorldSavedData {

    public static final String ID = GoHome.MODID;

    public static HomeData get(World world) {
        if (!world.isRemote) {
            DimensionSavedDataManager storage = ((ServerWorld) world).getSavedData();
            return storage.getOrCreate(HomeData::new, ID);
        } else {
            return new HomeData();
        }
    }

    private final Map<UUID, Map<String, BlockPos>> homes = new HashMap<>();

    public HomeData() {
        this(ID);
    }

    public HomeData(String name) {
        super(name);
    }

    @Override
    public void read(@Nonnull CompoundNBT nbt) {
        this.homes.clear();
        if (nbt.contains("homes", Constants.NBT.TAG_LIST)) {
            ListNBT playerList = nbt.getList("homes", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < playerList.size(); i++) {
                CompoundNBT playerHomes = playerList.getCompound(i);
                UUID player = playerHomes.getUniqueId("player");
                ListNBT homeList = playerHomes.getList("homes", Constants.NBT.TAG_COMPOUND);
                Map<String, BlockPos> homeMap = new HashMap<>();
                for (int j = 0; j < homeList.size(); j++) {
                    CompoundNBT home = homeList.getCompound(i);
                    homeMap.put(home.getString("name"), new BlockPos(home.getInt("x"), home.getInt("y"), home.getInt("z")).toImmutable());
                }
                this.homes.put(player, homeMap);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT playerList = new ListNBT();
        for (Map.Entry<UUID, Map<String, BlockPos>> playerEntry : this.homes.entrySet()) {
            CompoundNBT playerHomes = new CompoundNBT();
            playerHomes.putUniqueId("player", playerEntry.getKey());
            ListNBT homeList = new ListNBT();
            for (Map.Entry<String, BlockPos> homeEntry : playerEntry.getValue().entrySet()) {
                CompoundNBT home = new CompoundNBT();
                home.putString("name", homeEntry.getKey());
                home.putInt("x", homeEntry.getValue().getX());
                home.putInt("y", homeEntry.getValue().getY());
                home.putInt("z", homeEntry.getValue().getZ());
                homeList.add(home);
            }
            playerHomes.put("homes", homeList);
            playerList.add(playerHomes);
        }
        nbt.put("homes", playerList);
        return nbt;
    }

    @Nullable
    public BlockPos getHome(PlayerEntity player, String home) {
        if (!this.homes.containsKey(player.getGameProfile().getId()))
            return null;
        return this.homes.get(player.getGameProfile().getId()).getOrDefault(home.toLowerCase(), null);
    }

    public boolean setHome(PlayerEntity player, String home, @Nullable BlockPos pos) {
        if (pos == null) {
            if (this.homes.containsKey(player.getGameProfile().getId())) {
                boolean success = this.homes.get(player.getGameProfile().getId()).remove(home.toLowerCase()) != null;
                this.markDirty();
                return success;
            }
            return false;
        } else if (!this.homes.containsKey(player.getGameProfile().getId())) {
            this.homes.put(player.getGameProfile().getId(), new HashMap<>());
        }
        this.homes.get(player.getGameProfile().getId()).put(home.toLowerCase(), pos.toImmutable());
        this.markDirty();
        return true;
    }

    public List<String> getHomes(PlayerEntity player) {
        if (!this.homes.containsKey(player.getGameProfile().getId()))
            return ImmutableList.of();
        return ImmutableList.copyOf(this.homes.get(player.getGameProfile().getId()).keySet());
    }
}
