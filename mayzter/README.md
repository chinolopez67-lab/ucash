# 🚗 MAYZTER LITE — App nativa Android

Analiza automáticamente cada viaje entrante de **Uber** y **DiDi** y te dice en voz alta si conviene **ACEPTAR o RECHAZAR**.

---

## ¿Cómo obtener la APK? (sin instalar nada en tu PC)

### Paso 1 — Crea cuenta en GitHub
Ve a [github.com](https://github.com) y crea una cuenta gratuita.

### Paso 2 — Crea un repositorio nuevo
- Click en **"New repository"**
- Nombre: `mayzter-lite`
- Selecciona **Public**
- Click **"Create repository"**

### Paso 3 — Sube todos estos archivos
- En el repositorio, click **"uploading an existing file"**
- Arrastra TODA la carpeta del proyecto
- Click **"Commit changes"**

### Paso 4 — Espera la compilación (~5 min)
- Ve a la pestaña **Actions** en tu repositorio
- Verás un trabajo corriendo llamado **"Build APK"**
- Cuando termine, click en él → **Artifacts** → descarga **MAYZTER-LITE**

### Paso 5 — Instala en tu Android
- Descomprime el ZIP descargado
- Copia el `.apk` a tu teléfono
- En Android: **Ajustes → Seguridad → Instalar apps desconocidas** → activar para tu gestor de archivos
- Abre el APK e instala

---

## Configuración inicial en el teléfono

1. Abre **MAYZTER LITE**
2. Click **"Dar permiso de Notificaciones"** → busca MAYZTER LITE → activa el interruptor
3. Click **"Dar permiso de Overlay"** → activa el interruptor
4. Configura tus umbrales mínimos:
   - **$ mínimo por km**: cuánto necesitas ganar por kilómetro (default: 8.2)
   - **$ mínimo por hora**: cuánto necesitas ganar por hora (default: 150)
5. Click **Guardar**

¡Listo! Cada vez que llegue un viaje de Uber o DiDi, la app lo analiza automáticamente.

---

## ¿Cómo funciona?

- Escucha notificaciones de **Uber Driver** y **DiDi Conductor**
- Extrae precio, kilómetros y minutos de la notificación
- Calcula **MXN/km**, **MXN/h**, **MXN/min**
- Muestra un **overlay flotante** verde (acepta) o rojo (rechaza) por 7 segundos
- Te habla en voz alta: *"Acepta, 180 pesos por hora, 8.5 kilómetros, 22 minutos"*

---

## Permisos que pide y por qué

| Permiso | Para qué |
|---|---|
| Leer notificaciones | Detectar viajes entrantes de Uber/DiDi |
| Mostrar sobre otras apps | Overlay flotante con el resultado |
| Arrancar con el teléfono | Activarse automáticamente al encender |
| Text-to-Speech | Hablar el resultado en voz alta |
