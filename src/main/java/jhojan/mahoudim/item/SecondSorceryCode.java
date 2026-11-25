package jhojan.mahoudim.item;

import java.util.ArrayList;
import java.util.List;
import jhojan.mahoudim.menu.SecondCodeMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ComponentItemHandler;
import stepsword.mahoutsukai.config.MTConfig;
import stepsword.mahoutsukai.item.mysticcode.MysticCode;
import stepsword.mahoutsukai.item.spells.SpellScroll;
import stepsword.mahoutsukai.item.spells.boundary.BoundarySpellScroll;
import stepsword.mahoutsukai.item.spells.secret.FallenDownSpellScroll;
import stepsword.mahoutsukai.item.spells.secret.GandrSpellScroll;
import stepsword.mahoutsukai.util.EffectUtil;

public class SecondSorceryCode extends MysticCode implements MenuProvider {
   public static final int SLOT_COUNT = 6;

   public SecondSorceryCode() {
      super("second_sorcery_code", new Properties().stacksTo(1).durability(Math.max(1, MTConfig.FIRST_SORCERY_DURABILITY * 2)));
   }

   public static ComponentItemHandler inventory(ItemStack stack) {
      return new ComponentItemHandler(stack, DataComponents.CONTAINER, 6);
   }

   public static boolean isBeingUsed(Player player) {
      ItemStack active = player.getUseItem();
      return !active.isEmpty()
         ? active.getItem() instanceof SecondSorceryCode
         : player.getMainHandItem().getItem() instanceof SecondSorceryCode || player.getOffhandItem().getItem() instanceof SecondSorceryCode;
   }

   public int getMaxDamage(ItemStack stack) {
      return Math.max(1, MTConfig.FIRST_SORCERY_DURABILITY * 2);
   }

   public boolean isEnchantable(ItemStack stack) {
      return stack.getDamageValue() == 0;
   }

   public int getEnchantmentValue() {
      return 15;
   }

   public boolean requiresDura() {
      return true;
   }

   public boolean consumesScroll() {
      return false;
   }

   public ArrayList<? extends String> getBlacklist() {
      return new ArrayList<>(MTConfig.MYSTIC_CODE_FIRST_SORCERY_BLACKLIST);
   }

   public Component getDisplayName() {
      return Component.translatable("item.mahoudim.second_sorcery_code");
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.second_sorcery_code.tooltip"));
   }

   public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
      ItemStack code = player.getMainHandItem().getItem() instanceof SecondSorceryCode ? player.getMainHandItem() : player.getOffhandItem();
      return new SecondCodeMenu(id, playerInventory, code);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      if (player.isDiscrete()) {
         if (!level.isClientSide) {
            player.openMenu(this);
         }

         return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
      } else {
         player.startUsingItem(hand);
         return new InteractionResultHolder(InteractionResult.FAIL, player.getItemInHand(hand));
      }
   }

   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BLOCK;
   }

   public void releaseUsing(ItemStack code, Level level, LivingEntity user, int timeLeft) {
      if (!level.isClientSide && user instanceof Player player) {
         int slot = MysticCode.getSlot(code);
         ComponentItemHandler inventory = inventory(code);
         if (slot >= 0 && slot < 6) {
            ItemStack scrollStack = inventory.getStackInSlot(slot);
            if (!EffectUtil.inItemBlacklist(scrollStack.getItem(), this.getBlacklist(), level) && scrollStack.getItem() instanceof SpellScroll scroll) {
               scroll.useAction(scrollStack, level, user, this.consumesScroll());
               inventory.setStackInSlot(slot, scrollStack);
               EquipmentSlot equipmentSlot = player.getOffhandItem() == code ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
               code.hurtAndBreak(1, user, equipmentSlot);
            }
         }

         if (user instanceof ServerPlayer serverPlayer) {
            MysticCode.triggerUpdate(serverPlayer, code);
         }
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         Level level = player.level();
         if (player.isDiscrete()) {
            if (!level.isClientSide) {
               player.openMenu(this);
            }

            return InteractionResult.SUCCESS;
         } else {
            int slot = MysticCode.getSlot(context.getItemInHand());
            ComponentItemHandler inventory = inventory(context.getItemInHand());
            if (slot >= 0 && slot < 6) {
               ItemStack scrollStack = inventory.getStackInSlot(slot);
               if (!EffectUtil.inItemBlacklist(scrollStack.getItem(), this.getBlacklist(), level)
                  && scrollStack.getItem() instanceof BoundarySpellScroll boundary) {
                  InteractionResult result = boundary.boundaryScrollUse(
                     player, level, scrollStack, context.getClickedPos(), context.getClickedFace(), this.consumesScroll()
                  );
                  inventory.setStackInSlot(slot, scrollStack);
                  if (!level.isClientSide) {
                     EquipmentSlot equipmentSlot = context.getHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                     context.getItemInHand().hurtAndBreak(1, player, equipmentSlot);
                  }

                  return result;
               }

               player.startUsingItem(context.getHand());
            }

            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
               MysticCode.triggerUpdate(serverPlayer, context.getItemInHand());
            }

            return InteractionResult.FAIL;
         }
      }
   }

   public void onUseTick(Level level, LivingEntity user, ItemStack code, int count) {
      int slot = MysticCode.getSlot(code);
      if (slot >= 0 && slot < 6) {
         ItemStack scroll = inventory(code).getStackInSlot(slot);
         if (scroll.getItem() instanceof GandrSpellScroll || scroll.getItem() instanceof FallenDownSpellScroll) {
            scroll.getItem().onUseTick(level, user, scroll, count);
         }
      }
   }

   public List<String> getSlotsForWheel(ItemStack code) {
      ComponentItemHandler handler = inventory(code);
      List<String> names = new ArrayList<>(6);

      for (int i = 0; i < 6; i++) {
         names.add(MysticCode.convertStackToString(handler.getStackInSlot(i)));
      }

      return names;
   }
}
