/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AutoplayCmd extends MusicCommand
{
    public AutoplayCmd(Bot bot)
    {
        super(bot);
        this.name = "autoplay";
        this.help = "Enables autoplay for the server";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        Boolean isAutoplay = settings.getAutoplay();

        if (isAutoplay)
        {
            settings.setAutoplay(!isAutoplay);
            event.getChannel().sendMessage("<:zztick:1117868510169792593> | Successfully disabled autoplay mode, If you wanna activate it you\nhave to re-use the command.").queue();
            return;
        }

        settings.setAutoplay(true);

        event.getChannel().sendMessage("<:zztick:1117868510169792593> | Successfully enabled autoplay mode.").queue();
    }

}
