package com.faithfulmc.framework.command.module;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommandModule;
import com.faithfulmc.framework.command.module.chat.*;
import com.faithfulmc.framework.command.module.chat.announcement.AnnouncementCommand;

public class ChatModule extends BaseCommandModule {
    public ChatModule(final BasePlugin plugin) {
        this.commands.add(new AnnouncementCommand(plugin));
        this.commands.add(new BroadcastCommand(plugin));
        this.commands.add(new BroadcastRawCommand());
        this.commands.add(new ClearChatCommand());
        this.commands.add(new DisableChatCommand(plugin));
        this.commands.add(new SlowChatCommand(plugin));
        this.commands.add(new StaffChatCommand(plugin));
        this.commands.add(new IgnoreCommand(plugin));
        this.commands.add(new MessageCommand(plugin));
        this.commands.add(new MessageSpyCommand(plugin));
        this.commands.add(new ReplyCommand(plugin));
        this.commands.add(new ToggleChatCommand(plugin));
        this.commands.add(new ToggleMessagesCommand(plugin));
        this.commands.add(new ToggleStaffChatCommand(plugin));
    }
}
