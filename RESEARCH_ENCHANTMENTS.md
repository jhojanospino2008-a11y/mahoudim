# Investigación técnica: encantamientos rituales y Apotheosis

Minecraft 1.21.1 carga los encantamientos data-driven desde `data/<namespace>/enchantment/`, con tags de objetos para `supported_items` y `primary_items`. Los libros encantados usan los registros estándar de Minecraft, mientras que mecánicas con estado —contadores, ventanas, enfriamientos, partículas o interacciones con bloques— necesitan lógica Java del servidor. [1] [2]

Cada receta de encantamiento de MahouDim combina tres polvos Mahoujin y produce un círculo visible de un solo uso que entrega un pergamino estándar de Mahou Tsukai. Al hacer clic derecho una vez en ese pergamino se recibe el libro encantado de nivel I. La regla local de alfombrillas exige cuatro alfombrillas de lana colocadas en los puntos cardinales, no las consume y no afecta recetas de otros mods. Los niveles superiores se obtienen combinando libros en un yunque.

La compatibilidad con Apotheosis no es una dependencia obligatoria. `Apothic Attributes`, `Apothic Spawners` y `Apothic Enchanting` se declaran opcionales. Los encantamientos usan registros, libros y tags vanilla; la receta adicional de spawner se condiciona a la presencia de Apothic Spawners. La disponibilidad de estos encantamientos en las mesas de Apothic Enchanting puede depender de sus tags y configuración; no se afirma una prueba dentro del juego. [3] [4] [5]

## Efectos incluidos

En combate, `cadencia_suspendida` reduce brevemente la velocidad de ataque de un blanco tras una cadena de golpes, cuando este expone dicho atributo; `ancla_de_inercia` mitiga el siguiente retroceso recibido del mismo objetivo; `marca_de_retorno` señala con partículas, solo para el tirador y con línea de visión, a un blanco alcanzado por una flecha; `umbral_de_impacto` aplica Lentitud después de una transición acuática y un impacto de tridente; `eco_de_compas` prepara una respuesta tras bloquear con escudo; `pliegue_de_guardia` y `nudo_de_anclaje` reducen el siguiente retroceso tras sus respectivas condiciones de defensa; y `costura_de_instante` muestra al jugador partículas hacia la fuente de impactos seguidos.

Para el mundo y la recolección, `pulso_de_estrato` señala solo minerales expuestos y visibles; `reticula_de_cosecha` cosecha algunos cultivos maduros vecinos; `reloj_de_desgaste` evita un punto de desgaste tras una secuencia de bloques minados y un cooldown; `sondeo_de_veta` a veces señala una cavidad aproximada, sin identificar minerales; y `corte_de_intervalo` acorta el tiempo antes de que una oveja esquilada recupere lana, lo que puede permitir más esquilas con el tiempo.

Para movilidad y utilidades, `paso_de_fase` da un impulso breve al tocar nieve en polvo después de una caída corta, sin reducir daño de caída; `velo_de_reloj` reduce el rango de adquisición de hostiles al permanecer inmóvil y agachado; `memoria_de_umbral` suaviza durante unos ticks el ajuste del rumbo cardinal tras un cambio dimensional; `brujula_de_retornos` alterna entre aparición del Overworld y la última salida dimensional guardada; `cuerda_de_marea` da un pequeño impulso al siguiente lanzamiento de caña tras una captura; `resonancia_de_campana` señala aldeanos cercanos sin cambiar su IA; y `reserva_de_compas` avisa cuando el tiempo restante de lluvia cae bajo un umbral sin modificar el clima.

## Fuentes

[1]: https://docs.neoforged.net/docs/1.21.1/resources/server/enchantments/ "NeoForged: Enchantments for Minecraft 1.21.1"
[2]: https://docs.neoforged.net/docs/1.21.1/concepts/events/ "NeoForged: Event system"
[3]: https://github.com/Shadows-of-Fire/Apothic-Attributes "Shadows of Fire: Apothic Attributes"
[4]: https://github.com/Shadows-of-Fire/Apothic-Spawners "Shadows of Fire: Apothic Spawners"
[5]: https://github.com/Shadows-of-Fire/Apothic-Enchanting "Shadows of Fire: Apothic Enchanting"
