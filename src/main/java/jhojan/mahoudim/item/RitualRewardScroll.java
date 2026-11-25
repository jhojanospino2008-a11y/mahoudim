package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import stepsword.mahoutsukai.item.spells.SpellScroll;

public abstract class RitualRewardScroll extends SpellScroll {
   protected RitualRewardScroll() {
      super(new Properties().stacksTo(1));
   }

   protected abstract ItemStack createReward(Player var1);

   protected abstract Component claimedMessage();

   public final InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack scroll = player.getItemInHand(hand);
      InteractionResult result = this.claim(level, player, scroll);
      return new InteractionResultHolder(result, scroll);
   }

   public final InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      return player == null ? InteractionResult.FAIL : this.claim(context.getLevel(), player, context.getItemInHand());
   }

   private InteractionResult claim(Level level, Player player, ItemStack scroll) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack reward = this.createReward(player);
         if (reward.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mahoudim.ritual_scroll_unavailable"), true);
            return InteractionResult.FAIL;
         } else {
            if (!player.getInventory().add(reward)) {
               player.drop(reward, false);
            }

            if (!player.isCreative()) {
               scroll.shrink(1);
            }

            player.displayClientMessage(this.claimedMessage(), true);
            return InteractionResult.SUCCESS;
         }
      }
   }

   public int getInitialManaCost() {
      return 0;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.ritual_reward_scroll.tooltip"));
   }
}
