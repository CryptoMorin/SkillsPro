package org.skills.services;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ServiceVault {
    private static final Economy ECON;

    static {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        ECON = rsp.getProvider();
    }

    public static double getMoney(OfflinePlayer player) {
        return ECON.getBalance(player);
    }

    public static EconomyResponse deposit(OfflinePlayer player, double amount) {
        return ECON.depositPlayer(player, amount);
    }

    public static boolean hasMoney(OfflinePlayer player, double amount) {
        return ECON.has(player, amount);
    }

    public static EconomyResponse withdraw(OfflinePlayer player, double amount) {
        return ECON.withdrawPlayer(player, amount);
    }
}
