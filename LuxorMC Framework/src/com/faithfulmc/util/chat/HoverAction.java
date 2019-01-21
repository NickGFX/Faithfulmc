package com.faithfulmc.util.chat;

import net.minecraft.server.v1_7_R4.EnumHoverAction;

public enum HoverAction {
    SHOW_TEXT(EnumHoverAction.SHOW_TEXT), SHOW_ITEM(EnumHoverAction.SHOW_ITEM), SHOW_ACHIEVEMENT(EnumHoverAction.SHOW_ACHIEVEMENT);

    private final EnumHoverAction hoverAction;

    private HoverAction(final EnumHoverAction hoverAction) {
        this.hoverAction = hoverAction;
    }

    public EnumHoverAction getNMS() {
        return this.hoverAction;
    }
}
