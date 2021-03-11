package org.skills.commands.user;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.main.locale.SkillsLang;

public class CommandUser extends SkillsCommand {
    public CommandUser() {
        super("user", SkillsLang.COMMAND_USER_DESCRIPTION, true, "playerinfo");

        new CommandUserXP(this);
        new CommandUserLevel(this);
        new CommandUserSouls(this);

        new CommandUserSkill(this);
        new CommandUserStats(this);
        new CommandUserReset(this);

        new CommandUserMastery(this);
        new CommandUserImprove(this);
        new CommandUserCosmetic(this);
    }

    public static void magicCircle(int tier, double size, ParticleDisplay display) {
        if (tier == 1) {
            // https://i.pinimg.com/originals/aa/ee/0a/aaee0a69680c1cd8b6d30d1814028143.jpg
            XParticle.polygon(4, 4, size, 0.02, 0.3, display);
            XParticle.polygon(4, 3, size / (size - 1), 0.5, 0, display.clone().rotate(0, Math.PI / 2, 0));
            XParticle.polygon(8, 3, size / (size - 1), 0.5, 0, display);
            XParticle.polygon(8, 3, size / (size - 1), 0.5, 0, display);
        } else if (tier == 2) {
            // https://i.pinimg.com/236x/41/bf/2d/41bf2d9769fc135039049c1f72bd011b--magic-circle-fantasy-weapons.jpg
            XParticle.polygon(3, 3, size / (size - 1), 0.5, 0, display);
            XParticle.polygon(6, 3, size / (size - 1), 0.5, 0, display);
        } else if (tier == 3) {
            // https://vignette.wikia.nocookie.net/overlordmaruyama/images/3/38/Overlord_EP04_023.png/revision/latest/scale-to-width-down/340?cb=20150730120703
        } else if (tier == 4) {
            // https://thumbs.dreamstime.com/b/sacred-geometry-magic-circle-rune-simple-sacred-geometry-magic-circle-rune-star-107887158.jpg
            XParticle.polygon(8, 3, size / (size - 1), 0.5, 0, display);
            XParticle.circle(size, size * 5, display);
        }

        XParticle.circle(size, size * 10, display);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
//        Player p = (Player) sender;
//        Location loc = p.getLocation();
//        ParticleDisplay dis = new ParticleDisplay(Particle.DRAGON_BREATH, loc, 1, 0, 0, 0, 0);
//        dis.data = new float[]{0, 0, 0, 0.8f};
//        circle(3, 3, 0.2, 30, dis);
//        magicCircle(Integer.parseInt(args[0]), 3, dis);
//        illuminati(Double.parseDouble(args[0]), Double.parseDouble(args[1]), dis);
        // /skill user 4 3 0 10
//        eye(3, 3, 30, 0.2, dis);
//        circle(3, 3, 0.2, 30, 0, dis);
//          circle(3, 3, 0.2, 40, 0, dis);

//        tesseract(plugin, Double.parseDouble(args[0]), 3, Double.parseDouble(args[1]), 300, dis);
//        dis.rotate(Math.PI / 2, Math.PI * Double.parseDouble(args[4]), 0);
//        mandelbrot(5, Double.parseDouble(args[0]), 0.1, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), dis);
//        XParticle.displayRenderedImage(XParticle.renderImage(XParticle.stringToImage(new Font("Serif", Font.PLAIN, 30), Color.BLACK, "Nigga").join(), 0, 0, 0.2).join(),
//                dis.location, 1, 0, 0.8f);
//        flower(5, 8, dis, () -> mandelbrot(5, Double.parseDouble(args[0]), 0.2, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), dis));
//        julia(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), dis);

//        eye(30, dis);
//        diamond(0.03, 0.1, 5, dis);
//        XParticle.ellipse(2, 8, 30, dis);
        SkillsCommandHandler.executeHelperForGroup(this, sender);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return args.length < 2 ? TabCompleteManager.getSubCommand(sender, this, args).toArray(new String[0]) : new String[0];
    }
}
