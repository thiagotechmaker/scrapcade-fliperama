# Changelog

Todas as mudanças importantes do ScrapCade no iChroma ficam aqui. O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e as versões seguem o [Versionamento Semântico](https://semver.org/lang/pt-BR/) (fase beta enquanto for `0.x`).

## [Unreleased]

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
