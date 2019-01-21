package com.faithfulmc.framework.command.module.chat.announcement;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.command.module.chat.announcement.args.AnnouncementDelayArgument;
import com.faithfulmc.framework.command.module.chat.announcement.args.AnnouncementEditArgument;
import com.faithfulmc.framework.command.module.chat.announcement.args.AnnouncementLinesArgument;
import com.faithfulmc.framework.command.module.chat.announcement.args.AnnouncementListArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AnnouncementCommand extends BaseCommand{
    public static final String LABEL = "a";

    private AnnouncementListArgument announcementListArgument;

    public AnnouncementCommand(BasePlugin basePlugin) {
        super(LABEL, "Manage announcements");
        addArgument(announcementListArgument =  new AnnouncementListArgument(basePlugin));
        addArgument(new AnnouncementLinesArgument(basePlugin));
        addArgument(new AnnouncementDelayArgument(basePlugin));
        addArgument(new AnnouncementEditArgument(basePlugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0){
            return announcementListArgument.onCommand(sender, command, label, args);
        }
        return super.onCommand(sender, command, label, args);
    }
}
