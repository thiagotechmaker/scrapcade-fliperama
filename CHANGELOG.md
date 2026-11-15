# Changelog

Todas as mudanças importantes do ScrapCade no iChroma ficam aqui. O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e as versões seguem o [Versionamento Semântico](https://semver.org/lang/pt-BR/) (fase beta enquanto for `0.x`).

As versões 0.1.0-beta e 0.2.0-beta foram instaladas só no aparelho, sem Release no GitHub; as Releases começam na 0.3.0-beta.

## [Unreleased]

## [0.4.0-beta] - 2026-11-15

### Adicionado
- Animação de boot nova: as letras da logo acendem uma a uma, um brilho atravessa a logo e uma barra de blocos corre embaixo até o sistema subir (`art_src/gera_animacoes.py` gera o `scrapcadeboot2.zip`).
- "CARREGANDO..." animado (anel de blocos e pontinhos, nas letras de pixel da logo): ao abrir um jogo, com o nome dele e o atalho para voltar; embaixo da logo na abertura do menu; e durante cópias e mudanças no gerenciador de arquivos.
- A página no navegador mostra a mesma animação durante os envios e as operações de arquivo.

### Corrigido
- A logo de abertura do menu sumia antes de aparecer (o menu leva uns 2,5 s para desenhar a primeira tela): agora o tempo conta a partir do primeiro desenho.
- Ao abrir um jogo, apertar A de novo não abre o jogo duas vezes; B cancela a espera.

## [0.3.2-beta] - 2026-11-15

### Alterado
- As pastas `roms`, `saves`, `bios`, `themes` e `Jogos novos` ficam fora da varredura de mídia do Android (`.nomedia`, criado pelo próprio menu): no boot e a cada cartão ou pen drive ele não lê mais milhares de ROMs e capas à toa.

### Adicionado
- `scripts/publicar.ps1`: publica uma versão conferindo o APK, o CHANGELOG e a tag antes.
- Verificação automática no GitHub (o menu compila, scripts de arte, versão e CHANGELOG) e modelo para relatar problemas.

## [0.3.1-beta] - 2026-11-15

### Corrigido
- Sistema > Informações não mostra mais o nome antigo (T-OS): agora diz "Android 2.3.3 com boot próprio" e a versão do ScrapCade, lida do app.

### Alterado
- `art_src/logo_scrapcade.py` gera a logo de pixel atual (a mesma do PC) com caminhos do repositório; o gerador da logo antiga, que dependia de uma arte que não está aqui, saiu. Com o zImage de fábrica do aparelho, também grava a logo no kernel.
- README com o gerenciador de arquivos e mais fotos do aparelho.

## [0.3.0-beta] - 2026-11-15

### Adicionado
- Perfis com a tela "Quem vai jogar?" (ao ligar, quando há mais de um): criar com nome e cor pelo teclado da tela, trocar e apagar; cada perfil com favoritos, preferências e saves próprios (`saves/perfil-N`).
- Imagem de fundo sua: as imagens da pasta `themes` do cartão aparecem em APARÊNCIA.
- Modo atração: a proteção de tela mostra as cenas dos jogos, com nome e console.
- Painel Sistema em seções (JOGOS, JOGANDO, APARÊNCIA, TELA E ENERGIA, REDE, SISTEMA), na mesma ordem do ScrapCade OS.
- Ordenar jogos, consoles vazios, capas de novo, filtro "Continuar jogando", filtro de imagem, FPS na tela, relógio 12/24 h, voltar tudo ao padrão.
- Temas 8 bits, Portátil e Oceano; tamanho do texto; BIOS dos consoles; cópia dos saves no pen drive; proteção de tela.

### Alterado
- Pastas iguais às do ScrapCade OS: `saves` e `bios` na raiz do cartão, arcade sem a subpasta `roms`.
- Logo de pixel do ScrapCade OS no menu, na página, no bootloader, no kernel e na animação de boot.
- README sem o nome antigo do sistema (T-OS), no formato de 6 blocos, com LICENSE (GPLv3) e CHANGELOG.

## [0.2.0-beta] - 2026-11-11

### Adicionado
- Ejetar pen drive pelo menu.

### Alterado
- Nintendo 64 em resolução 360 por padrão (mais quadros por segundo).
- Pen drive: as pastas de sistema (boot, efi, sources...) são ignoradas na busca de jogos.

## [0.1.0-beta] - 2026-11-10

### Adicionado
- Menu ScrapCade (carrossel de capas pela GPU, favoritos, mais jogados, fundo do jogo, gerenciador de arquivos, página no navegador) e o driver de controle `joyd` para o Android 2.3.
- Organizador da pasta "Jogos novos", capas e dados baixados sozinhos, tutorial de instalação, capturas e fotos.
