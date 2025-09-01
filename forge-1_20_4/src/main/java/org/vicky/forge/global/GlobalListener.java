package org.vicky.forge.global;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.OpenOwnedRecordsScreen;
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.dao_s.MusicPieceDAO;
import org.vicky.utilities.DatabaseManager.dao_s.MusicPlayerDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;
import org.vicky.utilities.DatabaseManager.templates.MusicPiece;
import org.vicky.utilities.DatabaseManager.templates.MusicPlayer;

import java.util.Optional;

import static org.vicky.VickyUtilitiesForge.LOGGER;


@Mod.EventBusSubscriber
public class GlobalListener {
    static final DatabasePlayerDAO dao = new DatabasePlayerDAO();
    static final MusicPlayerDAO mDao = new MusicPlayerDAO();
    private static final String FIRST_JOIN_TAG = "vickyutils_first_joined";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        CompoundTag persistentData = player.getPersistentData();
        CompoundTag forgeData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);

        if (!forgeData.contains(FIRST_JOIN_TAG)) {
            forgeData.putBoolean(FIRST_JOIN_TAG, true);
            persistentData.put(Player.PERSISTED_NBT_TAG, forgeData);
            player.sendSystemMessage(Component.literal("§6Welcome, " + player.getName().getString() + "! This is your first time here."));

            DatabasePlayer databasePlayer = new DatabasePlayer();
            databasePlayer.setId(player.getUUID());
            databasePlayer.setFirstTime(true);
            dao.save(databasePlayer);

            MusicPlayer musicPlayer = new MusicPlayer();
            musicPlayer.setId(player.getStringUUID());
            musicPlayer.addPiece(
                    new MusicPieceDAO().findById("vicky_utils_symphony1")
            );
            musicPlayer.setDatabasePlayer(databasePlayer);
            mDao.save(musicPlayer);
        } else {
            Optional<DatabasePlayer> db = dao.findById(player.getUUID());
            if (db.isEmpty()) {
                LOGGER.error("Database player for player {} does not exist...", player.getName());

                DatabasePlayer databasePlayer = new DatabasePlayer();
                databasePlayer.setId(player.getUUID());
                databasePlayer.setFirstTime(true);
                dao.save(databasePlayer);

                MusicPlayer musicPlayer = new MusicPlayer();
                musicPlayer.setId(player.getStringUUID());
                musicPlayer.addPiece(
                        new MusicPieceDAO().findById("vicky_utils_symphony1")
                );
                musicPlayer.setDatabasePlayer(databasePlayer);
                mDao.save(musicPlayer);
            } else {
                db.get().setFirstTime(false);
                dao.update(db.get());
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // Run only at END phase so the world is stable
        if (event.phase == TickEvent.Phase.END) {
            org.vicky.musicPlayer.MusicPlayer.INSTANCE.tickAll();
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Example: /vickyutils hello <name>
        dispatcher.register(Commands.literal("vicky_utils")
                .then(Commands.literal("open_library")
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                var musicPlayer = new MusicPlayerDAO().findById(player.getUUID());
                                if (musicPlayer.isEmpty()) {
                                    throw new IllegalArgumentException("The music player should exist....");
                                }
                                var packet = new OpenOwnedRecordsScreen(musicPlayer.get().getOwnedPieces().stream().map(MusicPiece::getId).toList());
                                PacketHandler.sendToClient(player, packet);
                            } else {
                                context.getSource().sendSuccess(() -> Component.literal("You cant do that in a console ;-;"), false);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

