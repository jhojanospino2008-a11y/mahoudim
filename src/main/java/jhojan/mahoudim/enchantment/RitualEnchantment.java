package jhojan.mahoudim.enchantment;

import jhojan.mahoudim.tech.Technique;

/** Data-driven enchantments granted by Mahoujin carpet rituals. */
public enum RitualEnchantment {
   CADENCIA_SUSPENDIDA("cadencia_suspendida", 2, Technique.Cat.STONE, Technique.Cat.QUARTZ, Technique.Cat.EMERALD),
   ANCLA_DE_INERCIA("ancla_de_inercia", 2, Technique.Cat.IRON, Technique.Cat.OBSIDIAN, Technique.Cat.NETHERITE),
   MARCA_DE_RETORNO("marca_de_retorno", 2, Technique.Cat.ENDER, Technique.Cat.EYE, Technique.Cat.AMETHYST),
   UMBRAL_DE_IMPACTO("umbral_de_impacto", 2, Technique.Cat.PRISMARINE, Technique.Cat.COPPER, Technique.Cat.QUARTZ),
   ECO_DE_COMPAS("eco_de_compas", 1, Technique.Cat.IRON, Technique.Cat.MAHORA, Technique.Cat.EMERALD),
   PULSO_DE_ESTRATO("pulso_de_estrato", 2, Technique.Cat.SCULK, Technique.Cat.COPPER, Technique.Cat.STONE),
   RETICULA_DE_COSECHA("reticula_de_cosecha", 2, Technique.Cat.GOLD, Technique.Cat.EMERALD, Technique.Cat.AMETHYST),
   RELOJ_DE_DESGASTE("reloj_de_desgaste", 2, Technique.Cat.IRON, Technique.Cat.QUARTZ, Technique.Cat.SCULK),
   SONDEO_DE_VETA("sondeo_de_veta", 1, Technique.Cat.END_STONE, Technique.Cat.STONE, Technique.Cat.DIAMOND),
   CORTE_DE_INTERVALO("corte_de_intervalo", 2, Technique.Cat.COPPER, Technique.Cat.PRISMARINE, Technique.Cat.GOLD),
   PLIEGUE_DE_GUARDIA("pliegue_de_guardia", 2, Technique.Cat.NETHERITE, Technique.Cat.OBSIDIAN, Technique.Cat.QUARTZ),
   PASO_DE_FASE("paso_de_fase", 2, Technique.Cat.ENDER, Technique.Cat.PRISMARINE, Technique.Cat.SCULK),
   VELO_DE_RELOJ("velo_de_reloj", 1, Technique.Cat.GOLD, Technique.Cat.MAHORA, Technique.Cat.EYE),
   MEMORIA_DE_UMBRAL("memoria_de_umbral", 2, Technique.Cat.ENDER, Technique.Cat.AMETHYST, Technique.Cat.DIAMOND),
   COSTURA_DE_INSTANTE("costura_de_instante", 2, Technique.Cat.AMETHYST, Technique.Cat.OBSIDIAN, Technique.Cat.STONE),
   BRUJULA_DE_RETORNOS("brujula_de_retornos", 2, Technique.Cat.EYE, Technique.Cat.ENDER, Technique.Cat.GOLD),
   CUERDA_DE_MAREA("cuerda_de_marea", 2, Technique.Cat.PRISMARINE, Technique.Cat.QUARTZ, Technique.Cat.MAHORA),
   RESONANCIA_DE_CAMPANA("resonancia_de_campana", 1, Technique.Cat.STONE, Technique.Cat.QUARTZ, Technique.Cat.OBSIDIAN),
   NUDO_DE_ANCLAJE("nudo_de_anclaje", 2, Technique.Cat.IRON, Technique.Cat.SCULK, Technique.Cat.DIAMOND),
   RESERVA_DE_COMPAS("reserva_de_compas", 2, Technique.Cat.GOLD, Technique.Cat.QUARTZ, Technique.Cat.MAHORA);

   public final String id;
   public final int maxLevel;
   public final Technique.Cat a;
   public final Technique.Cat b;
   public final Technique.Cat c;

   RitualEnchantment(String id, int maxLevel, Technique.Cat a, Technique.Cat b, Technique.Cat c) {
      this.id = id;
      this.maxLevel = maxLevel;
      this.a = a;
      this.b = b;
      this.c = c;
   }

   public String blockId() {
      return "mahoujin_enchantment_" + this.id;
   }

   public String nameKey() {
      return "mahoudim.book.enchantment." + this.id + ".name";
   }

   public String descKey() {
      return "mahoudim.book.enchantment." + this.id + ".desc";
   }
}
