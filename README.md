# MahouDim — proyecto editable v1.0.8

Este repositorio ya está organizado como proyecto **Gradle + NeoForge**. El código Java editable vive en `src/main/java` y los recursos del mod en `src/main/resources`. El ZIP recuperado y el JAR original están guardados en `archive/reference/` únicamente como referencia de la versión recibida.

## Compilar

Se necesita JDK 21. En Linux o macOS:

```bash
./gradlew build
```

En Windows:

```bat
gradlew.bat build
```

El JAR generado queda en `build/libs/mahoudim-1.0.8.jar`. Para abrir un entorno de pruebas se puede usar `./gradlew runClient`; el proyecto también incluye `runServer` y una acción de GitHub que ejecuta la compilación al hacer push o abrir un pull request.

La compilación usa Minecraft 1.21.1, NeoForge 21.1.265 y obtiene Mahou Tsukai desde Curse Maven. No se guarda el JAR de Mahou Tsukai en este repositorio.

## Procedencia y cautelas

Los archivos `.java` proceden de una reconstrucción por decompilación; no se encontró el proyecto fuente original, así que comentarios y nombres locales podrían diferir de los originales. El proyecto ya pasó `./gradlew --no-daemon build` y generó el JAR, pero todavía no se ha probado dentro del cliente ni de un servidor de Minecraft. `SOURCE_RECOVERY_NOTE.txt` conserva la advertencia del paquete recibido.

Los `.class` duplicados que aparecían dentro de `src/main/resources` en el ZIP no se incluyeron en el source set activo: el compilador los debe generar a partir de `src/main/java`. El ZIP original y el JAR original siguen preservados en `archive/reference/`.
