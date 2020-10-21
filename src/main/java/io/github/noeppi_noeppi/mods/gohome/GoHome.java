package io.github.noeppi_noeppi.mods.gohome;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

@Mod(GoHome.MODID)
public class GoHome {

    public static final String MODID = "gohome";

    public GoHome() {
        MinecraftForge.EVENT_BUS.addListener(this::commands);
    }

    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("home").executes(ctx -> {
            BlockPos target = HomeData.get(ctx.getSource().getWorld()).getHome(ctx.getSource().asPlayer(), "home");
            if (target == null) {
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.notfound", "home"), true);
            } else {
                ctx.getSource().asPlayer().setPositionAndUpdate(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.success", "home"), true);
            }
            return 0;
        }).then(argument("homeId", StringArgumentType.string()).executes(ctx -> {
            BlockPos target = HomeData.get(ctx.getSource().getWorld()).getHome(ctx.getSource().asPlayer(), ctx.getArgument("homeId", String.class));
            if (target == null) {
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.notfound", ctx.getArgument("homeId", String.class)), true);
            } else {
                ctx.getSource().asPlayer().setPositionAndUpdate(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.success", ctx.getArgument("homeId", String.class)), true);
            }
            return 0;
        })));

        event.getDispatcher().register(literal("sethome").executes(ctx -> {
            BlockPos target = ctx.getSource().asPlayer().getPosition().toImmutable();
            HomeData.get(ctx.getSource().getWorld()).setHome(ctx.getSource().asPlayer(), "home", target);
            ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.set", "home"), true);
            return 0;
        }).then(argument("homeId", StringArgumentType.string()).executes(ctx -> {
            BlockPos target = ctx.getSource().asPlayer().getPosition().toImmutable();
            HomeData.get(ctx.getSource().getWorld()).setHome(ctx.getSource().asPlayer(), ctx.getArgument("homeId", String.class), target);
            ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.set", ctx.getArgument("homeId", String.class)), true);
            return 0;
        })));

        event.getDispatcher().register(literal("delhome").executes(ctx -> {
            if (HomeData.get(ctx.getSource().getWorld()).setHome(ctx.getSource().asPlayer(), "home", null)) {
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.del", "home"), true);
            } else {
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.notfound", "home"), true);
            }
            return 0;
        }).then(argument("homeId", StringArgumentType.string()).executes(ctx -> {
            if (HomeData.get(ctx.getSource().getWorld()).setHome(ctx.getSource().asPlayer(), ctx.getArgument("homeId", String.class), null)) {
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.del", ctx.getArgument("homeId", String.class)), true);
            } else {
                ctx.getSource().sendFeedback(new TranslationTextComponent("gohome.notfound", ctx.getArgument("homeId", String.class)), true);
            }
            return 0;
        })));

        event.getDispatcher().register(literal("listhomes").executes(ctx -> {
            IFormattableTextComponent tc = new TranslationTextComponent("gohome.list");
            for (String home : HomeData.get(ctx.getSource().getWorld()).getHomes(ctx.getSource().asPlayer())) {
                tc = tc.append(new StringTextComponent(" " + home));
            }
            ctx.getSource().sendFeedback(tc, true);
            return 0;
        }));
    }
}
