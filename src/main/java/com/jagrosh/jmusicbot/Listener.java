/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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

import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Listener extends ListenerAdapter
{
    private final Bot bot;
    
    public Listener(Bot bot)
    {
        this.bot = bot;
    }
    
    @Override
    public void onReady(ReadyEvent event) 
    {
        if(event.getJDA().getGuildCache().isEmpty())
        {
            Logger log = LoggerFactory.getLogger("MusicBot");
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(JMusicBot.RECOMMENDED_PERMS));
        }
        //credit(event.getJDA());
        event.getJDA().getGuilds().forEach((guild) -> 
        {
            try
            {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getSettingsManager().getSettings(guild).getVoiceChannel(guild);
                if(defpl!=null && vc!=null && bot.getPlayerManager().setUpHandler(guild).playFromDefault())
                {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            }
            catch(Exception ignore) {}
        });
        if(bot.getConfig().useUpdateAlerts())
        {
            bot.getThreadpool().scheduleWithFixedDelay(() -> 
            {
                try
                {
                    //User owner = bot.getJDA().retrieveUserById(bot.getConfig().getOwnerId()).complete();
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if(latestVersion!=null && !currentVersion.equalsIgnoreCase(latestVersion))
                    {
                        //String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        //owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                }
                catch(Exception ex) {} // ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }
    
    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) 
    {
        bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        bot.getAloneInVoiceHandler().onVoiceUpdate(event);
    }

    @Override
    public void onButtonClick(@Nonnull ButtonClickEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        switch (event.getComponentId()) {
            case "⏯️":
                if (handler.getPlayer().isPaused()) {
                    handler.getPlayer().setPaused(false);

                    event.deferReply(true).setContent("Resumed the player").queue();
                } else if (!handler.getPlayer().isPaused()) {
                    handler.getPlayer().setPaused(true);

                    event.deferReply(true).setContent("Paused the player").queue();
                }
                break;
            case "⏭️":
                event.deferReply(true).setContent(String.format("Skipping **%s** now", handler.getPlayer().getPlayingTrack().getInfo().title)).queue();

                handler.getPlayer().stopTrack();
                break;
            case "⏹️":
                event.deferReply(true).setContent("Destroying player").queue();

                handler.getPlayer().destroy();
                break;
            case "🔁":
                Settings settings = bot.getSettingsManager().getSettings(event.getGuild().getIdLong());

                if (settings.getRepeatMode() == RepeatMode.OFF) {
                    settings.setRepeatMode(RepeatMode.SINGLE);

                    event.deferReply(true).setContent("Looping track").queue();
                } else if (settings.getRepeatMode() == RepeatMode.SINGLE) {
                    settings.setRepeatMode(RepeatMode.ALL);

                    event.deferReply(true).setContent("Looping queue").queue();
                } else if (settings.getRepeatMode() == RepeatMode.ALL) {
                    settings.setRepeatMode(RepeatMode.OFF);

                    event.deferReply(true).setContent("Looping disabled").queue();
                }
            break;
            case "🔀":
                handler.getQueue().shuffle(event.getUser().getIdLong());

                event.deferReply(true).setContent("Shuffled queue").queue();
                break;
            case "🔉":
                event.deferReply(true).setContent("soon:tm:").queue();
                break;
            case "🔊":
                event.deferReply(true).setContent("soon:tm:").queue();
                break;
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) 
    {
        bot.shutdown();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) 
    {
        //credit(event.getJDA());
    }
    
    // make sure people aren't adding clones to dbots
    /*private void credit(JDA jda)
    {
        Guild dbots = jda.getGuildById(949530111693180928L);
        if(dbots==null)
            return;
        if(bot.getConfig().getDBots())
            return;
        jda.getTextChannelById(976462563959246918L)
                .sendMessage("Pencho restart hogya bot, <@"+bot.getConfig().getOwnerId()+">.").complete();
        //dbots.leave().queue();
    }*/
}
