# Print com Atraso

App Android (Java, sem dependências) que tira print da tela depois de alguns segundos.

**Como funciona:** você abre o app, escolhe o atraso (3 a 30 s), toca em **Tirar print**, o app vai para segundo plano, você navega até a tela que quer e, passado o tempo, o print é salvo em `Pictures/Screenshots` (aparece na Galeria) com uma vibração curta de confirmação.

Requisitos: Android 11 ou superior (testado para Android 12).

## Compilar sem Android Studio (GitHub Actions)

1. Crie um repositório no GitHub (pode ser privado) e envie esta pasta:

   ```bash
   cd PrintDelay
   git init
   git add .
   git commit -m "Print com Atraso"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/PrintDelay.git
   git push -u origin main
   ```

2. Abra a aba **Actions** do repositório. O workflow **Build APK** roda sozinho (leva uns 3–5 min). Também dá para rodar manualmente em *Run workflow*.
3. Quando terminar, abra a execução e baixe o artefato **PrintDelay-apk** (um zip com `app-debug.apk`).
4. Passe o APK para o celular e instale (permita "instalar apps desconhecidos" para o navegador/gerenciador de arquivos quando o Android pedir).

## Primeiro uso

O Android só permite capturar a tela via serviço de acessibilidade, então é preciso ativar uma única vez:

1. Abra o app e toque em **Tirar print**.
2. Toque em **Abrir configurações** → **Print com Atraso** (pode estar em *Apps instalados*) → ative → confirme.
3. Volte ao app. O status deve mostrar **Serviço ativo ✓**.

O serviço não lê nem envia nada do que está na tela; ele só chama a função de captura quando o tempo acaba.

## Limitações

- Apps que bloqueiam captura (bancos, alguns apps de vídeo/DRM) saem com tela preta — é uma proteção do próprio app.
- Se o Android encerrar o serviço (economia de bateria agressiva), reative em Configurações → Acessibilidade.
- Se tocar em **Tirar print** de novo antes do tempo acabar, o agendamento anterior é substituído.
