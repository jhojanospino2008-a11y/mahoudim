package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import stepsword.mahoutsukai.item.spells.SpellScroll;

/** A standard Mahou Tsukai scroll that immediately grants its one-time ritual reward. */
public abstract class RitualRewardScroll extends SpellScroll {
   protected RitualRewardScroll() {
      super(new Item.Properties().stacksTo(1));
   }

   protected abstract ItemStack createReward(Player player);

   protected abstract Component claimedMessage();

   @Override
   public final InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack scroll = player.getItemInHand(hand);
      InteractionResult result = claim(level, player, scroll);
      return new InteractionResultHolder<>(result, scroll);
   }

   @Override
   public final InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.FAIL;
      }
      return claim(context.getLevel(), player, context.getItemInHand());
   }

   private InteractionResult claim(Level level, Player player, ItemStack scroll) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      ItemStack reward = createReward(player);
      if (reward.isEmpty()) {
         player.displayClientMessage(Component.translatable("message.mahoudim.ritual_scroll_unavailable"), true);
         return InteractionResult.FAIL;
      }

      if (!player.getInventory().add(reward)) {
         player.drop(reward, false);
      }
      if (!player.isCreative()) {
         scroll.shrink(1);
      }
      player.displayClientMessage(claimedMessage(), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public int getInitialManaCost() {
      return 0;
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.ritual_reward_scroll.tooltip"));
   }
}
