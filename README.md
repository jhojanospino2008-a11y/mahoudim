# MahouDim — respaldo recuperado v1.0.8

Este repositorio privado contiene el código fuente recuperado de **MahouDim 1.0.8** y el binario `mahoudim-1.0.8-spellcloth-scroll-fix.jar` que se comparó con él. Es un respaldo histórico para continuar el trabajo a partir de esta versión; no se ha copiado aquí el contenido del repositorio público de la versión fallida.

## Contenido

- `source-recovered/MahouDim-1.0.8/`: fuente Java reconstruida, recursos y notas del paquete.
- `artifacts/MahouDim-1.0.8-source-recovered.zip`: ZIP recibido, sin modificar.
- `artifacts/mahoudim-1.0.8-spellcloth-scroll-fix.jar`: JAR recibido, sin modificar.

## Comparación

Los 227 archivos de contenido del JAR tienen una contraparte en `src/main/resources` del ZIP y sus bytes coinciden exactamente. Los metadatos identifican `modId=mahoudim`, versión `1.0.8`, Minecraft `1.21.1` y NeoForge `21.1.265+`.

## Limitación importante

El ZIP indica que los 35 archivos `.java` fueron reconstruidos por decompilación porque no se encontró el proyecto fuente original. Por ello, comentarios, nombres locales y construcciones pueden diferir; el paquete no incluye un proyecto Gradle garantizado como compilable. El JAR original se conserva como referencia del comportamiento compilado.
