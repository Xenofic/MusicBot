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
package com.jagrosh.jmusicbot.commands.filters;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.FilterCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;


/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class BassboostCmd extends FilterCommand {
    public BassboostCmd(Bot bot) {
        super(bot);
        this.name = "bassboost";
        this.help = "Enables the bassboost filter";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        Boolean is8D = settings.getFilters("bassboost");

        if (is8D) {
            handler.getPlayer().setFilterFactory(null);

            settings.setFilter("bassboost", false);

            event.getChannel().sendMessage("<:zztick:1117868510169792593> | Disabled bassboost filter.").queue();

            return;
        }

        float[] gains = {0.6f, 0.67f, 0.67f, 0, -0.5f, 0.15f, -0.45f, 0.23f, 0.35f, 0.45f, 0.55f, 0.6f, 0.55f, 0};

        EqualizerFactory eq = new EqualizerFactory();

        for (int i = 0; i < gains.length; i++) {
            eq.setGain(i, gains[i]);
        }

        handler.getPlayer().setFilterFactory(eq);

        settings.setFilter("bassboost", true);

        event.getChannel().sendMessage("<:zztick:1117868510169792593> | Applied bassboost filter.").queue();
    }
}