package org.skills.party;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.data.database.DataContainer;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.FastUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class SkillsParty extends DataContainer {
    private String name;
    private UUID id;
    private UUID leader;
    private List<UUID> members = new CopyOnWriteArrayList<>();

    public SkillsParty(UUID leader, String name, boolean load) {
        if (!load) this.id = UUID.randomUUID();
        this.leader = leader;
        this.name = name;
    }

    public static SkillsParty createParty(OfflinePlayer leader, String name) {
        SkillsParty party = new SkillsParty(leader.getUniqueId(), name, false);
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(leader);
        info.setParty(party);
        info.setRank(PartyRank.LEADER);
        party.members.add(party.leader);
        SkillsPro.get().getPartyManager().load(party);
        return party;
    }

    public static SkillsParty getParty(UUID id) {
        Objects.requireNonNull(id, "Cannot get party with null ID");
        return SkillsPro.get().getPartyManager().getData(id);
    }

    public void disband() {
        for (UUID member : this.members) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(member);
            info.setParty((UUID) null);
            info.setRank(null);
        }
        SkillsPro.get().getPartyManager().delete(this.id);
    }

    public List<Player> getOnlineMembers() {
        List<Player> members = new ArrayList<>(this.members.size());

        for (UUID member : this.members) {
            Player player = Bukkit.getPlayer(member);
            if (player != null) members.add(player);
        }

        return members;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    public List<OfflinePlayer> getPlayerMembers() {
        List<OfflinePlayer> members = new ArrayList<>(this.members.size());

        for (UUID member : this.members) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(member);
            members.add(player);
        }

        return members;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public @NonNull
    String getKey() {
        return FastUUID.toString(this.id);
    }

    @NotNull
    @Override
    public String getCompressedData() {
        return compressUUID(leader) + name + compressCollecton(members, DataContainer::compressUUID);
    }

    @Override
    public void setIdentifier(@NonNull String identifier) {
        this.id = FastUUID.fromString(identifier);
    }
}
