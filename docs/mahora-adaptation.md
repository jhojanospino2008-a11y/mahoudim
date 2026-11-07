# Técnica: Adaptación de Mahora

La receta Mahoujin `mahoudim:mahoujin_mahora_adaptation` entrega el pergamino `mahoudim:scroll_mahora_adaptation`. Al lanzarlo, el jugador recibe una `Rueda de Mahora` única. Si ya la lleva, el lanzamiento no concede otra.

Mientras la rueda esté en el inventario del jugador, cada tipo de daño se registra por separado. La rueda gira cada dos impactos válidos del mismo tipo y reduce ese daño un 25 %, 50 % y hasta un máximo del 75 %. La memoria de cada tipo se reinicia tras cinco minutos sin recibirlo. El progreso se guarda en los datos persistentes namespaced `mahoudim.mahora_adaptation`, con un límite de 16 tipos, y acompaña los datos de jugador al clonar la entidad.

El polvo `mahoudim:powdered_mahora` se fabrica triturando un `minecraft:echo_shard` con el martillo de Mahou Tsukai (1 polvo) o con mortero y mano (2 polvos). El polvo se usa como uno de los tres catalizadores del ritual.

La mitigación se aplica en `LivingDamageEvent.Pre`, que NeoForge 1.21.x documenta como el último punto para modificar el daño antes de aplicarlo a la salud, una vez procesadas armadura y pociones; absorción se procesa después. Por ese motivo, el código contabiliza impactos que alcanzan esta fase, no ataques anulados antes por invulnerabilidad.

## Referencia técnica

- NeoForge, `LivingDamageEvent.java` (rama 1.21.x): https://raw.githubusercontent.com/neoforged/NeoForge/1.21.x/src/main/java/net/neoforged/neoforge/event/entity/living/LivingDamageEvent.java
- NeoForge Primer 1.20.6 → 1.21: https://docs.neoforged.net/primer/docs/1.21/neo/

La compilación no sustituye una prueba dentro de Minecraft; conviene validar servidor dedicado, equipo/escudo, absorción, recarga de mundo y persistencia al volver a entrar.
