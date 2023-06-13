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

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.FilterCommand;
import com.jagrosh.jmusicbot.settings.Settings;

import java.util.Collections;


/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class EightDickCmd extends FilterCommand {
    public EightDickCmd(Bot bot) {
        super(bot);
        this.name = "8d";
        this.help = "Enables the 8d filter";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        Boolean is8D = settings.getFilters("8d");

        if (is8D) {
            handler.getPlayer().setFilterFactory(null);

            settings.setFilter("8d", false);

            event.getChannel().sendMessage("<:zztick:1117868510169792593> | Disabled 8d filter.").queue();

            return;
        }

        handler.getPlayer().setFilterFactory((track, format, output) -> {
            RotationPcmAudioFilter audioFilter = new RotationPcmAudioFilter(output, format.sampleRate)
                    .setRotationSpeed(0.3);
            return Collections.singletonList(audioFilter);
        });

        settings.setFilter("8d", true);

        event.getChannel().sendMessage("<:zztick:1117868510169792593> | Applied 8d filter.").queue();
    }
}