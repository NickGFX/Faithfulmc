package com.faithfulmc.hardcorefactions.listener;

import com.luxormc.event.PlayerMineItemsEvent;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.RecipesFurnace;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CutCleanListener implements Listener {
    public static final ImmutableMap<Material, Material> CUTCLEAN_ORES = ImmutableMap.of(
            Material.GOLD_ORE, Material.GOLD_INGOT,
            Material.IRON_ORE, Material.IRON_INGOT
    );
    private static final String AUTO_SMELT_ORE_PERMISSION = "hcf.autosmeltore";

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(PlayerMineItemsEvent event) {
        Player player = event.getPlayer();
        if ((ConfigurationService.CUT_CLEAN || player.hasPermission(AUTO_SMELT_ORE_PERMISSION)) && player.getItemInHand() != null && !player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            for(ItemStack itemStack: event.getStackCollection()){
                Material type = itemStack.getType();
                Material cutClean = CUTCLEAN_ORES.get(type);
                if(cutClean != null){
                    itemStack.setType(cutClean);
                    Block block = Block.getById(type.getId());
                    int xp = block.getExpDrop(((CraftWorld)player.getWorld()).getHandle(), 0, player.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)) + getEXP(itemStack, itemStack.getAmount());
                    player.giveExp(xp);
                }
            }
        }
    }

    private int getEXP(ItemStack itemStack, int amount){
        int i = amount;
        float f = RecipesFurnace.getInstance().b(CraftItemStack.asNMSCopy(itemStack));
        int j;

        if (f == 0.0F) {
            i = 0;
        } else if (f < 1.0F) {
            j = MathHelper.d((float) i * f);
            if (j < MathHelper.f((float) i * f) && (float) Math.random() < (float) i * f - (float) j) {
                ++j;
            }

            i = j;
        }
        return i;
    }

    /*
    private void blockBreak(Player player, Material type, Material blockType, Block block, boolean cutclean, boolean pickup){
        byte data = block.getData();

        net.minecraft.server.v1_7_R4.Block block1 = CraftMagicNumbers.getBlock(block);
        net.minecraft.server.v1_7_R4.World world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();


        net.minecraft.server.v1_7_R4.ItemStack hand = entityPlayer.bF();

        boolean flag1 = entityPlayer.a(block1);

        //Item Breaking
        if (hand != null) {
            hand.a(world, block1, block.getX(), block.getY(), block.getZ(), entityPlayer);
            if (hand.count == 0) {
                entityPlayer.bG();
            }
        }

        //Dropping items
        if (flag1) {

            //Apply exhaustion
            entityPlayer.a(StatisticList.MINE_BLOCK_COUNT[net.minecraft.server.v1_7_R4.Block.getId(block1)], 1);
            entityPlayer.applyExhaustion(entityPlayer.world.paperSpigotConfig.blockBreakExhaustion);

            int i1 = EnchantmentManager.getBonusBlockLootEnchantmentLevel(entityPlayer);
            boolean silkTouch = EnchantmentManager.hasSilkTouchEnchantment(entityPlayer);
            int j1 = block1.a(world.random) * (i1 + 1);

            if(j1 > 0) {
                ItemStack itemStack;
                byte dropData = 0;
                if(block1.d() && !block1.isTileEntity() && (silkTouch || ((blockType == Material.LEAVES || blockType == Material.LEAVES_2) && hand.getItem() == Items.SHEARS))){
                    net.minecraft.server.v1_7_R4.Item item = net.minecraft.server.v1_7_R4.Item.getItemOf(block1);
                    itemStack = CraftItemStack.asNewCraftStack(item);
                    if(item != null && item.n()) {
                        MaterialData materialData = itemStack.getData();
                        materialData.setData(data);
                        itemStack.setData(materialData);
                    }
                }
                else {
                    if (type == null) {
                        type = Material.getMaterial(net.minecraft.server.v1_7_R4.Item.getId(block1.getDropType(i1, world.random, data)));
                        dropData = (byte) block1.getDropData(world, block.getX(), block.getY(), block.getZ());
                    }
                    itemStack = new ItemStack(type, j1, dropData);
                }


                if(!pickup || !player.getInventory().addItem(itemStack).isEmpty()) {
                    float f = 0.7F;
                    double d0 = (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d1 = (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d2 = (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    Item item = block.getWorld().dropItem(block.getLocation().add(d0, d1, d2), itemStack);
                    item.setPickupDelay(10);
                }
            }

            if(!silkTouch){
                int exp = block1.getExpDrop(world, data, cutclean ? i1 + 1 : i1);
                if (i1 > 0) {
                    double multiplier = i1 * 1.5D;
                    exp = (int) Math.ceil(i1 * multiplier);
                }
                if (exp > 0) {
                    player.giveExp(exp);
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5f, 1f);
                }
            }
        }
    }
    */
}
