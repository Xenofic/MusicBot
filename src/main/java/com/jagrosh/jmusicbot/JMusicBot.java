/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.*;
import com.jagrosh.jmusicbot.commands.admin.*;
import com.jagrosh.jmusicbot.commands.dj.*;
import com.jagrosh.jmusicbot.commands.filters.BassboostCmd;
import com.jagrosh.jmusicbot.commands.filters.EightDickCmd;
import com.jagrosh.jmusicbot.commands.general.*;
import com.jagrosh.jmusicbot.commands.music.*;
import com.jagrosh.jmusicbot.commands.owner.*;
import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.gui.GUI;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class JMusicBot {
    public final static Logger LOG = LoggerFactory.getLogger(JMusicBot.class);
    public final static Permission[] RECOMMENDED_PERMS = { Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
            Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE };
    public final static GatewayIntent[] INTENTS = { GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0)
            switch (args[0].toLowerCase()) {
                case "generate-config":
                    BotConfig.writeDefaultConfig();
                    return;
                default:
            }
        startBot();
    }

    private static void startBot() {
        // create prompt to handle startup
        Prompt prompt = new Prompt("Kana");

        // startup checks
        OtherUtil.checkJavaVersion(prompt);

        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if (!config.isValid())
            return;
        LOG.info("Loaded config from " + config.getConfigLocation());

        // set up the listener
        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        Bot bot = new Bot(waiter, config, settings);

        /*AboutCommand aboutCommand = new AboutCommand(Color.BLUE.brighter(),
                "a music bot that is [easy to host yourself!](https://github.com/jagrosh/MusicBot) (v"
                        + OtherUtil.getCurrentVersion() + ")",
                new String[] { "High-quality music playback", "FairQueue™ Technology", "Easy to host yourself" },
                RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB6"); // 🎶*/

        // set up the command client
        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(config.getPrefix())
                .setAlternativePrefix(config.getAltPrefix())
                .setOwnerId(Long.toString(config.getOwnerId()))
                .setEmojis(config.getSuccess(), config.getWarning(), config.getError())
                // .setHelpWord(config.getHelp())
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(settings)
                .setHelpConsumer((event) -> {
                    // TODO: Make help dynamic
                    //List<Command> commands = event.getClient().getCommands();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("My prefix is: `x!`");
                    stringBuilder.append("\nI am very cute UwU");

                    MessageBuilder builder = new MessageBuilder();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor("Kana | Help Menu", event.getAuthor().getAvatarUrl(), event.getAuthor().getAvatarUrl());
                    embed.setDescription(stringBuilder);
                    embed.addField("Filters [1]", "`8d`, `bassboost`", false);
                    embed.addField("General [3]", "`about`, `ping`, `settings`", false);
                    embed.addField("Music [9]",
                            "`nowplaying`, `play`, `playlists`, `queue`, `remove`, `search`, `scsearch`, `shuffle`, `skip`",
                            false);
                    embed.addField("DJ [9]",
                            "`forceremove`, `forceskip`, `movetrack`, `pause`, `playnext`, `repeat`, `skipto`, `stop`, `volume`",
                            false);
                    embed.addField("Admin [5]", "`prefix`, `setdj`, `setskip`, `settc`, `setvc`", false);
                    embed.addField("Owner [9]",
                            "`autoplaylist`, `debug`, `playlist`, `setavatar`, `setgame`, `setname`, `setstatus`, `shutdown`, `eval`",
                            false);
                    embed.setColor(event.getSelfMember().getColor());
                    embed.setThumbnail(event.getSelfUser().getAvatarUrl());

                    event.getChannel().sendMessage(builder.setEmbeds(embed.build()).build()).setActionRow(
                            Button.link("https://youtu.be/BT9h5ifR1tY", "Get Kana"),
                            Button.link("https://discord.gg/xeone", "Support Server"),
                            Button.link("https://youtu.be/BT9h5ifR1tY", "Premium"),
                            Button.link("https://youtu.be/BT9h5ifR1tY", "Vote")).queue();
                })
                .addCommands(
                        new PingCommand(),
                        new SettingsCmd(bot),
                        new AboutCmd(bot),

                        new EightDickCmd(bot),
                        new BassboostCmd(bot),

                        new NowplayingCmd(bot),
                        new PlayCmd(bot),
                        new PlaylistsCmd(bot),
                        new QueueCmd(bot),
                        new RemoveCmd(bot),
                        new SearchCmd(bot),
                        new SCSearchCmd(bot),
                        new ShuffleCmd(bot),
                        new SkipCmd(bot),
                        new AutoplayCmd(bot),

                        new ForceRemoveCmd(bot),
                        new ForceskipCmd(bot),
                        new MoveTrackCmd(bot),
                        new PauseCmd(bot),
                        new PlaynextCmd(bot),
                        new RepeatCmd(bot),
                        new SkiptoCmd(bot),
                        new StopCmd(bot),
                        new VolumeCmd(bot),

                        new PrefixCmd(bot),
                        new SetdjCmd(bot),
                        new SkipratioCmd(bot),
                        new SettcCmd(bot),
                        new SetvcCmd(bot),

                        new AutoplaylistCmd(bot),
                        new DebugCmd(bot),
                        new PlaylistCmd(bot),
                        new SetavatarCmd(bot),
                        new SetgameCmd(bot),
                        new SetnameCmd(bot),
                        new SetstatusCmd(bot),
                        new ShutdownCmd(bot));
        if (config.useEval())
            cb.addCommand(new EvalCmd(bot));
        boolean nogame = false;
        if (config.getStatus() != OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if (config.getGame() == null)
            cb.useDefaultGame();
        else if (config.getGame().getName().equalsIgnoreCase("none")) {
            cb.setActivity(null);
            nogame = true;
        } else
            cb.setActivity(config.getGame());

        if (false) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                LOG.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        // attempt to log in and start
        try {
            JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.ONLINE_STATUS)
                    .setActivity(nogame ? null : Activity.playing("loading..."))
                    .setStatus(
                            config.getStatus() == OnlineStatus.INVISIBLE || config.getStatus() == OnlineStatus.OFFLINE
                                    ? OnlineStatus.INVISIBLE
                                    : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(cb.build(), waiter, new Listener(bot))
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);
        } catch (LoginException ex) {
            prompt.alert(Prompt.Level.ERROR, "Kana", ex + "\nPlease make sure you are "
                    + "editing the correct config.txt file, and that you have used the "
                    + "correct token (not the 'secret'!)\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        } catch (IllegalArgumentException ex) {
            prompt.alert(Prompt.Level.ERROR, "Kana", "Some aspect of the configuration is "
                    + "invalid: " + ex + "\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        } catch (ErrorResponseException ex) {
            prompt.alert(Prompt.Level.ERROR, "Kana", ex + "\nInvalid reponse returned when "
                    + "attempting to connect, please make sure you're connected to the internet");
            System.exit(1);
        }
    }
}
