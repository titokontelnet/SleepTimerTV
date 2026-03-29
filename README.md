# Sleep Timer TV 🌙

App sencilla para Android TV / TCL que permite programar el apagado
de pantalla con un solo clic desde el control remoto.

## Funcionalidades
- Temporizadores: 15, 30, 45, 60, 90 min y 2 horas
- Cuenta regresiva visible en pantalla
- Se mantiene activa en segundo plano (presiona Atrás)
- Cancela el timer en cualquier momento
- Optimizada para control remoto (D-pad)

## Cómo compilar

### Requisitos
- Android Studio Hedgehog o superior
- JDK 17

### Pasos
1. Abre Android Studio
2. File → Open → selecciona la carpeta `SleepTimerTV`
3. Espera que Gradle sincronice
4. Build → Build Bundle(s)/APK(s) → Build APK(s)
5. El APK queda en: `app/build/outputs/apk/debug/app-debug.apk`

### Instalar en la TCL
1. Copia el APK a una USB
2. En la TV: Configuración → Seguridad → Fuentes desconocidas → Activar
3. Usa un gestor de archivos (ej. FX File Explorer) para abrir el APK
4. Instala y lanza "Sleep Timer TV"
5. Al primer uso: acepta el permiso de administrador de dispositivo
   (necesario para apagar la pantalla)

## Nota técnica
Usa `DevicePolicyManager.lockNow()` para apagar la pantalla,
que es el método estándar sin necesidad de root.
