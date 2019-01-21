package com.faithfulmc.framework;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ProtocolHook {
    private static final ItemStack AIR;

    public static void hook(final BasePlugin basePlugin) {
        /*
        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        final UserManager userManager = basePlugin.getUserManager();
        protocolManager.addPacketListener((PacketListener)new PacketAdapter(basePlugin, new PacketType[] { PacketType.Play.Server.ENTITY_EQUIPMENT }) {
            public void onPacketSending(final PacketEvent event) {
                if (basePlugin.getServerHandler().useProtocolLib) {
                    final Player player = event.getPlayer();
                    final BaseUser baseUser = userManager.getUser(player.getUniqueId());
                    if (!baseUser.isGlintEnabled()) {
                        final PacketContainer packet = event.getPacket();
                        final StructureModifier modifier = packet.getItemModifier();
                        if (modifier.size() > 0) {
                            final ItemStack stack = (ItemStack)modifier.read(0);
                            if (stack != null && stack.getType() != Material.AIR) {
                                convert(stack);
                            }
                        }
                    }
                }
            }
        });
        protocolManager.addPacketListener((PacketListener)new PacketAdapter(basePlugin, new PacketType[] { PacketType.Play.Server.ENTITY_METADATA }) {
            public void onPacketSending(final PacketEvent event) {
                if (basePlugin.getServerHandler().useProtocolLib) {
                    final Player player = event.getPlayer();
                    final BaseUser baseUser = userManager.getUser(player.getUniqueId());
                    if (true) {
                        final PacketContainer packet = event.getPacket();
                        final StructureModifier modifier = packet.getEntityModifier(event);
                        if (modifier.size() > 0 && modifier.read(0) instanceof Item) {
                            final WrappedDataWatcher watcher = new WrappedDataWatcher((List)packet.getWatchableCollectionModifier().read(0));
                            if (watcher.size() >= 10) {
                                final ItemStack stack = watcher.getItemStack(10).clone();
                                if (stack != null && stack.getType() != Material.AIR) {
                                    convert(stack);
                                }
                            }
                        }
                    }
                }
            }
        });
        */
    }

    private static ItemStack convert(final ItemStack origin) {
        if (origin != null && origin.getType() != Material.AIR) {
            switch (origin.getType().ordinal()) {
                case 1:
                case 2: {
                    if (origin.getDurability() > 0) {
                        origin.setDurability((short) 0);
                        break;
                    }
                    break;
                }
                case 3: {
                    origin.setType(Material.BOOK);
                    break;
                }
                default: {
                    origin.getEnchantments().keySet().forEach(origin::removeEnchantment);
                    break;
                }
            }
            return origin;
        }
        return origin;
    }

    static {
        AIR = new ItemStack(Material.AIR, 1);
    }
}
