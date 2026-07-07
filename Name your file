package net.fabricmc.example;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LightningWandItem extends Item {
    public LightningWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            lightning.setPosition(user.getX(), user.getY(), user.getZ());
            world.spawnEntity(lightning);

            if (!user.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            
            user.sendMessage(Text.literal("§6Пика-Чууу!§e Молния готова!"), false);
        }
        
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
