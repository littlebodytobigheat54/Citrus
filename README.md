# Citra Android Custom 🎮

Emulador de Nintendo 3DS para Android, baseado no core do [Lime3DS](https://github.com/Lime3DS/Lime3DS) com interface personalizada em Kotlin/Jetpack Compose.

---

## ✨ Recursos

- 📱 Interface Material 3 com modo escuro/claro
- 🖥️ Dupla tela ajustável (vertical, horizontal, tela única, swap)
- 🎮 Controles virtuais personalizáveis
- ⚡ Suporte a Vulkan e OpenGL ES
- 💾 Save States (slots 0–4)
- 📊 Overlay de FPS, CPU e GPU
- 📁 Biblioteca de jogos com scanner de pastas
- 🎚️ Volume, resolução interna e velocidade de CPU ajustáveis
- 📸 Captura de tela

---

## 🏗️ Como compilar no GitHub Actions

### 1. Faça um fork deste repositório

Clique em **Fork** no canto superior direito do GitHub.

### 2. Ative o GitHub Actions

Vá em **Actions** → clique em **"I understand my workflows, go ahead and enable them"**.

### 3. Dispare o build

Vá em **Actions** → **Build Citra Android** → **Run workflow** → **Run workflow**.

### 4. Baixe o APK

Após o build (≈ 60–90 min), vá em **Actions** → clique no run concluído → role até **Artifacts** → baixe `citra-android-release`.

---

## 🛠️ Como compilar localmente

### Requisitos

| Ferramenta | Versão |
|---|---|
| Android Studio | Hedgehog 2023.1+ |
| NDK | r26b (26.1.10909125) |
| CMake | 3.22.1 |
| JDK | 17 |
| Python | 3.10+ |
| RAM | 16 GB+ |
| Espaço | 50 GB+ |

### Passos

```bash
# 1. Clonar
git clone --recursive https://github.com/SEU_USUARIO/Citra-Android-Custom.git
cd Citra-Android-Custom

# 2. Clonar o core (Lime3DS)
git clone --recursive --depth=1 \
  https://github.com/Lime3DS/Lime3DS.git \
  citra-core

# 3. Copiar sources
cp -r citra-core/src android/app/src/main/cpp/citra-src
cp -r citra-core/externals ./externals

# 4. Configurar NDK (editar com seu path real)
echo "sdk.dir=$HOME/Android/Sdk" > android/local.properties
echo "ndk.dir=$HOME/Android/Sdk/ndk/26.1.10909125" >> android/local.properties

# 5. Build
cd android
chmod +x gradlew
./gradlew assembleRelease

# APK em: android/app/build/outputs/apk/release/
```

---

## 📁 Estrutura do Projeto

```
Citra-Android-Custom/
├── .github/workflows/
│   └── android.yml              ← CI/CD build automático
├── CMakeLists.txt               ← Build raiz C++
├── android/
│   ├── app/
│   │   ├── build.gradle.kts     ← Dependências Android
│   │   ├── proguard-rules.pro
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── cpp/
│   │       │   ├── CMakeLists.txt
│   │       │   ├── android_jni.cpp      ← Bridge JNI principal
│   │       │   ├── emu_window_android.* ← Janela EGL/OpenGL
│   │       │   ├── input_android.*      ← Input handler
│   │       │   ├── audio_android.*      ← AAudio output
│   │       │   └── citra-src/           ← Core do Lime3DS (copiado no build)
│   │       ├── java/com/citra/android/
│   │       │   ├── CitraApplication.kt
│   │       │   ├── MainActivity.kt
│   │       │   ├── EmulatorActivity.kt
│   │       │   ├── jni/NativeLibrary.kt ← Kotlin ↔ C++
│   │       │   ├── ui/
│   │       │   │   ├── EmulatorScreen.kt
│   │       │   │   ├── GameLibraryScreen.kt
│   │       │   │   ├── SettingsScreen.kt
│   │       │   │   ├── VirtualControlsOverlay.kt
│   │       │   │   └── theme/Theme.kt
│   │       │   ├── viewmodel/
│   │       │   │   └── EmulatorViewModel.kt
│   │       │   ├── model/GameEntry.kt
│   │       │   └── utils/
│   │       │       ├── GameScanner.kt
│   │       │       └── PreferenceManager.kt
│   │       └── res/
│   │           └── values/{strings,colors,themes}.xml
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── gradle.properties
└── README.md
```

---

## ⚖️ Licença e Avisos Legais

- Este projeto é distribuído sob a **GPLv2**, respeitando a licença do Citra/Lime3DS.
- Os créditos dos desenvolvedores originais do Citra são mantidos integralmente.
- **Não inclui BIOS, firmware ou ROMs.**
- Use apenas com cópias legais de jogos que você possui.
- O projeto é open-source e sem fins comerciais.

---

## 🙏 Créditos

- [Citra Team](https://github.com/citra-emu/citra) — emulador original
- [Lime3DS](https://github.com/Lime3DS/Lime3DS) — fork ativo da comunidade
- [PabloMK7](https://github.com/PabloMK7/citra) — fork com melhorias de compatibilidade
