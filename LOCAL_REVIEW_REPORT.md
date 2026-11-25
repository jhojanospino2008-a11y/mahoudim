# Revisión local de MahouDim 1.0.12d

- Fuente de recuperación: `/home/ubuntu/upload/mahoudim-1_0_12d-unlimited-apothic.jar`
- Proyecto: `/home/ubuntu/mahoudim-1_0_12d-source`
- Respaldo anterior: `/home/ubuntu/mahoudim-current-backup`

## Verificaciones
- `./gradlew clean compileJava --no-daemon --console=plain`: **correcto**
- `./gradlew build --no-daemon --console=plain`: **correcto**
- JAR generado: `build/libs/mahoudim-1.0.12.jar`
- SHA-256 del JAR: `acd14c73659e994ffca19d1e5737ad585047c1963e6b01585fd95b872cf4422e`

## Contenido del JAR
- Clases compiladas: 92
- Entradas de recursos `assets/` y `data/`: 329
- Configuración de mixins: `mahoudim.mixins.json`
- Recetas de Apothic Spawners: 18 (`data/mahoudim/recipe/spawner_modifiers/`)
- Clases nuevas verificadas: `ArcaneCircleRenderer`, `ProjectionPreviewEvents`, `EffectInscriptionBook`, `EffectInscriptionEvents`

## Respaldo y publicación
- El repositorio remoto todavía no ha sido modificado.
- El estado anterior está conservado en el respaldo local indicado arriba.
- Esta revisión no autoriza por sí sola ninguna publicación destructiva.

## Archivos principales
- src/main/java/jhojan/mahoudim/Config.java
- src/main/java/jhojan/mahoudim/MahouDim.java
- src/main/java/jhojan/mahoudim/PowderCatalog.java
- src/main/java/jhojan/mahoudim/RecipeHook.java
- src/main/java/jhojan/mahoudim/Reg.java
- src/main/java/jhojan/mahoudim/block/EnchantmentRitualCircleBlock.java
- src/main/java/jhojan/mahoudim/block/GateCircleBlock.java
- src/main/java/jhojan/mahoudim/block/JuadanaRitualCircleBlock.java
- src/main/java/jhojan/mahoudim/block/PocketCircleBlock.java
- src/main/java/jhojan/mahoudim/block/RitualRewardCircleBlock.java
- src/main/java/jhojan/mahoudim/block/TechniqueCircleBlock.java
- src/main/java/jhojan/mahoudim/book/BookExtension.java
- src/main/java/jhojan/mahoudim/client/ArcaneCircleRenderer.java
- src/main/java/jhojan/mahoudim/client/ClientHandlers.java
- src/main/java/jhojan/mahoudim/client/ClientSetup.java
- src/main/java/jhojan/mahoudim/client/CoreScreen.java
- src/main/java/jhojan/mahoudim/client/EffectTechniqueScreen.java
- src/main/java/jhojan/mahoudim/client/GateScreen.java
- src/main/java/jhojan/mahoudim/client/ProjectionPreviewEvents.java
- src/main/java/jhojan/mahoudim/client/SecondCodeScreen.java
- src/main/java/jhojan/mahoudim/client/SecondSorceryCodeRenderer.java
- src/main/java/jhojan/mahoudim/enchantment/RitualEnchantment.java
- src/main/java/jhojan/mahoudim/enchantment/RitualEnchantmentEvents.java
- src/main/java/jhojan/mahoudim/item/EffectInscriptionBook.java
- src/main/java/jhojan/mahoudim/item/JuadanaEvents.java
- src/main/java/jhojan/mahoudim/item/JuadanaItem.java
- src/main/java/jhojan/mahoudim/item/JuadanaRitualScroll.java
- src/main/java/jhojan/mahoudim/item/MahoraWheelItem.java
- src/main/java/jhojan/mahoudim/item/RitualEnchantmentScroll.java
- src/main/java/jhojan/mahoudim/item/RitualRewardScroll.java
- src/main/java/jhojan/mahoudim/item/SecondCodeCraftingEvents.java
- src/main/java/jhojan/mahoudim/item/SecondSorceryCode.java
- src/main/java/jhojan/mahoudim/item/TechniqueScroll.java
- src/main/java/jhojan/mahoudim/menu/SecondCodeMenu.java
- src/main/java/jhojan/mahoudim/mixin/MixinBlockElementBounds.java
- src/main/java/jhojan/mahoudim/mixin/MixinGuidebookInstance.java
- src/main/java/jhojan/mahoudim/mixin/MixinHumanoidModel.java
- src/main/java/jhojan/mahoudim/mixin/MixinItemInHandRenderer.java
- src/main/java/jhojan/mahoudim/mixin/MixinMahoujinRenderer.java
- src/main/java/jhojan/mahoudim/mixin/MixinMahoujinTileEntity.java
- src/main/java/jhojan/mahoudim/mixin/MixinMahoujinWoolCarpets.java
- src/main/java/jhojan/mahoudim/mixin/MixinManaClientEvents.java
- src/main/java/jhojan/mahoudim/mixin/MixinMysticCodeRenderer.java
- src/main/java/jhojan/mahoudim/mixin/MixinMysticCodeSwitchPacket.java
- src/main/java/jhojan/mahoudim/mixin/MixinMysticCodeSwitchPacketAccessor.java
- src/main/java/jhojan/mahoudim/mixin/MixinWheelGui.java
- src/main/java/jhojan/mahoudim/net/ClientPayloadRouter.java
- src/main/java/jhojan/mahoudim/net/CoreActionPacket.java
- src/main/java/jhojan/mahoudim/net/EffectActionPacket.java
- src/main/java/jhojan/mahoudim/net/GateActionPacket.java
- src/main/java/jhojan/mahoudim/net/NetRegistry.java
- src/main/java/jhojan/mahoudim/net/OpenCorePacket.java
- src/main/java/jhojan/mahoudim/net/OpenEffectScreenPacket.java
- src/main/java/jhojan/mahoudim/net/OpenGatePacket.java
- src/main/java/jhojan/mahoudim/realm/Ore.java
- src/main/java/jhojan/mahoudim/realm/RealmConfig.java
- src/main/java/jhojan/mahoudim/realm/RealmData.java
- src/main/java/jhojan/mahoudim/realm/RealmEvents.java
- src/main/java/jhojan/mahoudim/realm/RealmManager.java
- src/main/java/jhojan/mahoudim/realm/Theme.java
- src/main/java/jhojan/mahoudim/tech/EffectInscriptionEvents.java
- src/main/java/jhojan/mahoudim/tech/EffectTechniqueActions.java
- src/main/java/jhojan/mahoudim/tech/MahoraAdaptationEvents.java
- src/main/java/jhojan/mahoudim/tech/Technique.java
- src/main/java/jhojan/mahoudim/tech/TechniqueActions.java
- src/main/java/jhojan/mahoudim/tech/TechniqueEvents.java
- src/main/java/jhojan/mahoudim/tech/TimeStopController.java
- src/main/java/jhojan/mahoudim/tech/UnlimitedProjectionSpell.java
- src/main/java/jhojan/mahoudim/tile/EnchantmentRitualCircleTile.java
- src/main/java/jhojan/mahoudim/tile/JuadanaRitualCircleTile.java
- src/main/java/jhojan/mahoudim/tile/TechniqueCircleTile.java
- src/main/java/jhojan/mahoudim/tile/UtilityCircleTile.java
- src/main/java/jhojan/mahoudim/worldgen/TaggedOreFeature.java
