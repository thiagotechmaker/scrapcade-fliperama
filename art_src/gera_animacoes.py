"""Animacoes do ScrapCade OS (mesma letra de pixel e mesmas cores da logo) para a tela de 800x480 do iChroma.
Gera:
  launcher/scrapcadeboot2.zip            boot do Android: as letras acendem uma a uma, um brilho atravessa a logo
                                         e fica uma barra de blocos correndo em laco ate o sistema subir
  launcher/assets/carregando/00..07.png  "CARREGANDO..." do menu: anel de blocos girando + pontinhos (fundo transparente)
  launcher/assets/carregando.png         os mesmos 8 quadros lado a lado (tira), se preferir uma imagem so
  launcher/assets/web/carregando.gif     o mesmo carregando, ja animado, para a pagina do navegador
  art_src/scrapcade/previa_boot.gif      previa do boot para ver no PC (nao vai para o aparelho)
Uso: py -3.12 gera_animacoes.py"""
import io
import os
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ART = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(ART)
SC = os.path.join(ART, "scrapcade")
ASSETS = os.path.join(REPO, "launcher", "assets")

LETRAS = {
    "S": [".####", "#....", "#....", ".###.", "....#", "....#", "####."],
    "C": [".####", "#....", "#....", "#....", "#....", "#....", ".####"],
    "R": ["####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#"],
    "A": [".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"],
    "P": ["####.", "#...#", "#...#", "####.", "#....", "#....", "#...."],
    "D": ["####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####."],
    "E": ["#####", "#....", "#....", "####.", "#....", "#....", "#####"],
    "O": [".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."],
    "G": [".####", "#....", "#....", "#.###", "#...#", "#...#", ".###."],     # novas, para "CARREGANDO"
    "N": ["#...#", "##..#", "##..#", "#.#.#", "#..##", "#..##", "#...#"],
}
BRANCO, BRANCO_ESC = (255, 255, 255, 255), (215, 220, 240, 255)
MENTA, MENTA_ESC, CINZA = (50, 230, 168, 255), (25, 150, 120, 255), (200, 205, 230, 255)
SOMBRA = (0, 0, 0, 140)


def escreve(d, texto, x, y, b, cor, cor_baixo=None, sombra=True):
    """Igual ao da logo: texto com blocos de b pixels; as duas linhas de baixo podem ter outra cor."""
    for ch in texto:
        g = LETRAS[ch]
        for desl, cs in ((1, SOMBRA), (0, None)) if sombra else ((0, None),):
            for r, linha in enumerate(g):
                for c, v in enumerate(linha):
                    if v != "#":
                        continue
                    px, py = x + c * b + desl * b // 3, y + r * b + desl * b // 3
                    cc = cs or (cor_baixo if cor_baixo and r >= 5 else cor)
                    d.rectangle([px, py, px + b - 1, py + b - 1], fill=cc)
        x += 6 * b
    return x


def jpeg(im, q=88):
    b = io.BytesIO()
    im.save(b, "JPEG", quality=q, optimize=True)
    return b.getvalue()


def mistura(a, b, t):
    """Cor entre a e b (RGB), t de 0 a 1."""
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


# ---------------------------------------------------------------- BOOT
def camadas(B):
    """As 9 letras da logo, cada uma na sua camada transparente (na mesma posicao da logo inteira), e o selo OS no fim."""
    larg, alt = 6 * B * 13, 9 * B

    def nova():
        im = Image.new("RGBA", (larg, alt), (0, 0, 0, 0))
        return im, ImageDraw.Draw(im)

    cams, x = [], B // 2
    for ch, cor, baixo in [(c, BRANCO, BRANCO_ESC) for c in "SCRAP"] + [(c, MENTA, MENTA_ESC) for c in "CADE"]:
        im, d = nova()
        x = escreve(d, ch, x, B // 2, B, cor, baixo)
        cams.append(im)
    im, d = nova()
    b2 = B // 2
    ox, oy = x + B // 2, B // 2 + 7 * B - 7 * b2 - 2 * b2
    w2 = 2 * 6 * b2 + b2
    d.rectangle([ox - b2, oy - b2, ox + w2, oy + 7 * b2 + b2], outline=CINZA, width=max(2, b2 // 3))
    escreve(d, "OS", ox + b2 // 2, oy, b2, CINZA, sombra=False)
    cams.append(im)
    todo = Image.new("RGBA", (larg, alt), (0, 0, 0, 0))
    for c in cams:
        todo.alpha_composite(c)
    caixa = todo.getbbox()
    return [c.crop(caixa) for c in cams]


B = 11
PAD = 28                                    # folga em volta da logo para o brilho (glow) nao ser cortado
CAMS = camadas(B)
LW, LH = CAMS[0].size
BAR_N, BAR_W, BAR_H, BAR_G = 12, 48, 16, 8  # 12 blocos: 12*48 + 11*8 = 664 px, a largura da logo
LOGO_X, LOGO_Y = (800 - LW) // 2, (480 - LH) // 2 - 34
BAR_X, BAR_Y = (800 - (BAR_N * BAR_W + (BAR_N - 1) * BAR_G)) // 2, LOGO_Y + LH + 58
FUNDO_BLOCO = (30, 36, 66)


def logo_quadro(visiveis, flash=None, brilho=None):
    """A logo com as camadas 'visiveis'; a camada 'flash' entra branca com um halo menta; 'brilho' e o x de uma faixa de luz."""
    L = Image.new("RGBA", (LW + 2 * PAD, LH + 2 * PAD), (0, 0, 0, 0))
    for i in visiveis:
        L.alpha_composite(CAMS[i], (PAD, PAD))
    if flash is not None:
        a = Image.new("L", L.size, 0)
        a.paste(CAMS[flash].getchannel("A"), (PAD, PAD))
        halo = Image.new("RGBA", L.size, MENTA[:3] + (255,))
        halo.putalpha(a.filter(ImageFilter.GaussianBlur(9)).point(lambda v: min(255, v * 2)))
        L.alpha_composite(halo)
        branca = Image.new("RGBA", L.size, (255, 255, 255, 255))
        branca.putalpha(a)
        L.alpha_composite(branca)
    if brilho is not None:
        arr = np.array(L).astype(np.float32)
        perfil = np.clip(1 - np.abs(np.arange(L.width) - brilho) / 70, 0, 1) ** 1.5
        k = (perfil[None, :] * 0.6 * arr[..., 3] / 255)[..., None]
        arr[..., :3] = arr[..., :3] * (1 - k) + 255 * k
        L = Image.fromarray(arr.astype(np.uint8), "RGBA")
    return L


def barra(img, ganho=1.0, cabeca=None):
    """Barra de 12 blocos embaixo da logo. 'cabeca' e o bloco mais aceso; atras dele fica um rastro de 3 blocos."""
    d = ImageDraw.Draw(img)
    for i in range(BAR_N):
        cor = tuple(int(c * ganho) for c in FUNDO_BLOCO)
        if cabeca is not None:
            dist = (cabeca - i) % BAR_N
            if dist < 4:
                cor = mistura(FUNDO_BLOCO, mistura(MENTA, (255, 255, 255), 0.35 if dist == 0 else 0), (1.0, 0.6, 0.35, 0.18)[dist])
        x = BAR_X + i * (BAR_W + BAR_G)
        d.rectangle([x, BAR_Y, x + BAR_W - 1, BAR_Y + BAR_H - 1], fill=cor)
        d.rectangle([x, BAR_Y + BAR_H - 4, x + BAR_W - 1, BAR_Y + BAR_H - 1], fill=mistura((0, 0, 0), cor, 0.72))


def quadro_boot(visiveis, flash=None, brilho=None, ganho_barra=None, cabeca=None):
    t = Image.new("RGBA", (800, 480), (0, 0, 0, 255))
    t.alpha_composite(logo_quadro(visiveis, flash, brilho), (LOGO_X - PAD, LOGO_Y - PAD))
    if ganho_barra is not None or cabeca is not None:
        barra(t, ganho_barra if ganho_barra is not None else 1.0, cabeca)
    return t.convert("RGB")


def boot():
    todas = range(len(CAMS))
    # parte 0 (toca uma vez): cada letra acende em branco com halo e assenta na cor; depois um brilho atravessa a logo
    # e a trilha da barra aparece. parte 1 (em laco): a luz corre pelos 12 blocos, um bloco por quadro.
    p0 = [quadro_boot(range(k), flash=k) for k in todas]
    xs = np.linspace(-40, LW + 2 * PAD + 40, 5)
    p0 += [quadro_boot(todas, brilho=x, ganho_barra=g) for x, g in zip(xs, (0.2, 0.4, 0.6, 0.8, 1.0))]
    p1 = [quadro_boot(todas, cabeca=i) for i in range(BAR_N)]
    zpath = os.path.join(REPO, "launcher", "scrapcadeboot2.zip")
    with zipfile.ZipFile(zpath, "w", zipfile.ZIP_STORED) as z:       # Android 2.3 so le quadros sem compressao
        z.writestr("desc.txt", "800 480 10\np 1 0 part0\np 0 0 part1\n")
        for i, q in enumerate(p0):
            z.writestr(f"part0/{i:03d}.jpg", jpeg(q))
        for i, q in enumerate(p1):
            z.writestr(f"part1/{i:03d}.jpg", jpeg(q))
    print("boot:", len(p0), "+", len(p1), "quadros;", os.path.getsize(zpath), "bytes")
    # previa em GIF: parte 0 e tres voltas da parte 1, na metade do tamanho
    seq = [q.resize((400, 240), Image.LANCZOS).convert("P", palette=Image.ADAPTIVE, colors=128) for q in p0 + p1 * 3]
    seq[0].save(os.path.join(SC, "previa_boot.gif"), save_all=True, append_images=seq[1:], duration=100, loop=0, optimize=True)


# ---------------------------------------------------------------- CARREGANDO
CEL, ESP, TB, PAD_C = 16, 4, 5, 8            # celula e espaco do anel, bloco do texto, margem
ANEL = [(0, 0), (1, 0), (2, 0), (2, 1), (2, 2), (1, 2), (0, 2), (0, 1)]
TEXTO = "CARREGANDO"
ANEL_L = 3 * CEL + 2 * ESP
TX = PAD_C + ANEL_L + 28
TEXTO_FIM = TX + len(TEXTO) * 6 * TB - TB
CW, CH = TEXTO_FIM + TB + 3 * 3 * TB + PAD_C, ANEL_L + 2 * PAD_C
BG_GIF = (14, 18, 80)


def quadro_carregando(passo):
    im = Image.new("RGBA", (CW, CH), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for i, (cx, cy) in enumerate(ANEL):                       # anel 3x3: a cabeca anda no sentido horario com rastro
        t = {0: 1.0, 1: 0.65, 2: 0.4, 3: 0.25}.get((passo - i) % 8, 0.13)
        x, y = PAD_C + cx * (CEL + ESP), PAD_C + cy * (CEL + ESP)
        d.rectangle([x, y, x + CEL - 1, y + CEL - 1], fill=MENTA[:3] + (int(255 * t),))
        d.rectangle([x, y + CEL - 4, x + CEL - 1, y + CEL - 1], fill=MENTA_ESC[:3] + (int(255 * t),))
    ty = (CH - 7 * TB) // 2
    escreve(d, TEXTO, TX, ty, TB, BRANCO, BRANCO_ESC)
    for k in range([0, 0, 1, 1, 2, 2, 3, 3][passo]):          # pontinhos: 0, 1, 2 e 3, dois quadros cada
        x = TEXTO_FIM + TB + k * 3 * TB
        d.rectangle([x + TB // 3, ty + 6 * TB + TB // 3, x + TB // 3 + TB - 1, ty + 7 * TB + TB // 3 - 1], fill=SOMBRA)
        d.rectangle([x, ty + 6 * TB, x + TB - 1, ty + 7 * TB - 1], fill=BRANCO)
    return im


def carregando():
    os.makedirs(os.path.join(ASSETS, "carregando"), exist_ok=True)
    quadros = [quadro_carregando(p) for p in range(8)]
    tira = Image.new("RGBA", (CW * 8, CH), (0, 0, 0, 0))
    for i, q in enumerate(quadros):
        q.save(os.path.join(ASSETS, "carregando", f"{i:02d}.png"), optimize=True)
        tira.paste(q, (i * CW, 0))
    tira.save(os.path.join(ASSETS, "carregando.png"), optimize=True)
    gif = []
    for q in quadros:
        f = Image.new("RGBA", q.size, BG_GIF + (255,))
        f.alpha_composite(q)
        gif.append(f.convert("RGB").convert("P", palette=Image.ADAPTIVE, colors=48))
    gif[0].save(os.path.join(ASSETS, "web", "carregando.gif"), save_all=True, append_images=gif[1:], duration=100, loop=0, optimize=True)
    print("carregando:", CW, "x", CH, "px, 8 quadros")


os.makedirs(SC, exist_ok=True)
os.makedirs(os.path.join(ASSETS, "web"), exist_ok=True)
boot()
carregando()
