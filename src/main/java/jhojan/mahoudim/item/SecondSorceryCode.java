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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
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

/** A first-sorcery Mystic Code upgraded to store and select six scrolls. */
public class SecondSorceryCode extends MysticCode implements MenuProvider {
   public static final int SLOT_COUNT = 6;

   public SecondSorceryCode() {
      super("second_sorcery_code", new Item.Properties()
         .stacksTo(1)
         .durability(Math.max(1, MTConfig.FIRST_SORCERY_DURABILITY * 2)));
   }

   public static ComponentItemHandler inventory(ItemStack stack) {
      return new ComponentItemHandler(stack, DataComponents.CONTAINER, SLOT_COUNT);
   }

   public static boolean isBeingUsed(Player player) {
      ItemStack active = player.getUseItem();
      if (!active.isEmpty()) {
         return active.getItem() instanceof SecondSorceryCode;
      }
      return player.getMainHandItem().getItem() instanceof SecondSorceryCode
         || player.getOffhandItem().getItem() instanceof SecondSorceryCode;
   }

   @Override
   public int getMaxDamage(ItemStack stack) {
      return Math.max(1, MTConfig.FIRST_SORCERY_DURABILITY * 2);
   }

   @Override
   public boolean isEnchantable(ItemStack stack) {
      return stack.getDamageValue() == 0;
   }

   @Override
   public int getEnchantmentValue() {
      return 15;
   }

   @Override
   public boolean requiresDura() {
      return true;
   }

   @Override
   public boolean consumesScroll() {
      return false;
   }

   @Override
   public ArrayList<? extends String> getBlacklist() {
      return new ArrayList<>(MTConfig.MYSTIC_CODE_FIRST_SORCERY_BLACKLIST);
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("item.mahoudim.second_sorcery_code");
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.second_sorcery_code.tooltip"));
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
      ItemStack code = player.getMainHandItem().getItem() instanceof SecondSorceryCode
         ? player.getMainHandItem() : player.getOffhandItem();
      return new SecondCodeMenu(id, playerInventory, code);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      if (player.isDiscrete()) {
         if (!level.isClientSide) {
            player.openMenu(this);
         }
         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
      player.startUsingItem(hand);
      return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BLOCK;
   }

   @Override
   public void releaseUsing(ItemStack code, Level level, LivingEntity user, int timeLeft) {
      if (level.isClientSide || !(user instanceof Player player)) {
         return;
      }
      int slot = MysticCode.getSlot(code);
      ComponentItemHandler inventory = inventory(code);
      if (slot >= 0 && slot < SLOT_COUNT) {
         ItemStack scrollStack = inventory.getStackInSlot(slot);
         if (!EffectUtil.inItemBlacklist(scrollStack.getItem(), this.getBlacklist(), level)
            && scrollStack.getItem() instanceof SpellScroll scroll) {
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

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.FAIL;
      }
      Level level = player.level();
      if (player.isDiscrete()) {
         if (!level.isClientSide) {
            player.openMenu(this);
         }
         return InteractionResult.SUCCESS;
      }
      int slot = MysticCode.getSlot(context.getItemInHand());
      ComponentItemHandler inventory = inventory(context.getItemInHand());
      if (slot >= 0 && slot < SLOT_COUNT) {
         ItemStack scrollStack = inventory.getStackInSlot(slot);
         if (!EffectUtil.inItemBlacklist(scrollStack.getItem(), this.getBlacklist(), level)
            && scrollStack.getItem() instanceof BoundarySpellScroll boundary) {
            InteractionResult result = boundary.boundaryScrollUse(
               player, level, scrollStack, context.getClickedPos(), context.getClickedFace(), this.consumesScroll());
            inventory.setStackInSlot(slot, scrollStack);
            if (!level.isClientSide) {
               EquipmentSlot equipmentSlot = context.getHand() == InteractionHand.OFF_HAND
                  ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
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

   @Override
   public void onUseTick(Level level, LivingEntity user, ItemStack code, int count) {
      int slot = MysticCode.getSlot(code);
      if (slot < 0 || slot >= SLOT_COUNT) {
         return;
      }
      ItemStack scroll = inventory(code).getStackInSlot(slot);
      if (scroll.getItem() instanceof GandrSpellScroll || scroll.getItem() instanceof FallenDownSpellScroll) {
         scroll.getItem().onUseTick(level, user, scroll, count);
      }
   }

   @Override
   public List<String> getSlotsForWheel(ItemStack code) {
      ComponentItemHandler handler = inventory(code);
      List<String> names = new ArrayList<>(SLOT_COUNT);
      for (int i = 0; i < SLOT_COUNT; i++) {
         names.add(MysticCode.convertStackToString(handler.getStackInSlot(i)));
      }
      return names;
   }
}
