package elucent.eidolon.item;

import elucent.eidolon.entity.BonechillProjectileEntity;
import elucent.eidolon.registries.Entities;
import elucent.eidolon.registries.Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

public class BonechillWandItem extends WandItem {
    private final Random random = new Random();

    public BonechillWandItem(Properties builderIn) {
        super(builderIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (this.loreTag != null) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(String.valueOf(ChatFormatting.DARK_PURPLE) + ChatFormatting.ITALIC + I18n.get(this.loreTag)));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        if (!world.isClientSide) {
            Vec3 pos = entity.position().add(0, entity.getEyeHeight(), 0).add(entity.getViewVector(1));
            Vec3 vel = entity.getEyePosition(0).add(entity.getLookAngle().scale(40)).subtract(pos).scale(1.0 / 20);
            var projectile = new BonechillProjectileEntity(Entities.BONECHILL_PROJECTILE.get(), world).shoot(pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, entity.getUUID());
            world.addFreshEntity(projectile);
            world.playSound(null, pos.x, pos.y, pos.z, Sounds.CAST_BONECHILL_EVENT.get(), SoundSource.NEUTRAL, 0.75f, random.nextFloat() * 0.2f + 0.9f);
            stack.hurtAndBreak(1, entity, (player) -> player.broadcastBreakEvent(hand));
        }
        entity.swing(hand);
        entity.getCooldowns().addCooldown(this, 10);

        return InteractionResultHolder.success(stack);
    }
}
