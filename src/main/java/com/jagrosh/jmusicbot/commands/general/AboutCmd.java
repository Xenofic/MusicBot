/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.lang.management.ManagementFactory;


/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AboutCmd extends Command {
    public AboutCmd(Bot bot) {
        this.name = "about";
        this.help = "Displays bout's about and stats";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        final long duration = ManagementFactory.getRuntimeMXBean().getUptime();

        final long years = duration / 31104000000L;
        final long months = duration / 2592000000L % 12;
        final long days = duration / 86400000L % 30;
        final long hours = duration / 3600000L % 24;
        final long minutes = duration / 60000L % 60;
        final long seconds = duration / 1000L % 60;

        String uptime = (years == 0 ? "" : "**" + years + "** Year(s), ") + (months == 0 ? "" : "**" + months + "** Month(s), ") + (days == 0 ? "" : "**" + days + "** Day(s), ") + (hours == 0 ? "" : "**" + hours + "** Hour(s), ")
                + (minutes == 0 ? "" : "**" + minutes + "** Minute(s), ") + (seconds == 0 ? "" : "**" + seconds + "** Second(s), ") /* + (milliseconds == 0 ? "" : milliseconds + " Milliseconds, ") */;

        MessageBuilder builder = new MessageBuilder();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getAuthor().getName() + event.getAuthor().getDiscriminator(), event.getAuthor().getAvatarUrl(), event.getAuthor().getAvatarUrl());
        embed.setTitle("Official Bot Server Invite", "https://discord.gg/xeone");
        embed.setColor(event.getSelfMember().getColor());
        embed.setThumbnail(event.getSelfUser().getAvatarUrl());
        embed.addField("__Botinfo__", String.format("**Uptime:** %s\n**Guilds:** %s\n**Users:** Total %s | %s Cached\n**Ping:** %s\n**Commands:** %s", uptime, event.getJDA().getGuilds().size(), event.getJDA().getUsers().size(), event.getJDA().getUserCache().size(), event.getJDA().getGatewayPing(), "36"), false);
        embed.addField("__Channels__", String.format("<:X_text:1015937465510526976> %s | <:Voice:1115634094278197299> %s | <:StagePublic:1115634110317203567> %s", event.getJDA().getTextChannels().size(), event.getJDA().getVoiceChannels().size(), event.getJDA().getStageChannels().size()), false);

        event.getChannel().sendMessage(builder.setEmbeds(embed.build()).build()).setActionRow(
                Button.success("botinfo", "Botinfo").asDisabled(),
                Button.secondary("systeminfo", "System Info").asDisabled(),
                Button.secondary("moduleinfo", "Module Info").asDisabled()
        ).queue();
    }

}
