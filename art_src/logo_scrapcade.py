"""Logo do ScrapCade OS (letras de pixel, a mesma do PC) para a tela de 800x480 do iChroma.
Gera:
  art_src/scrapcade/logo_tela_800.png   fundo preto (bootloader e kernel usam esta)
  launcher/assets/logo.jpg              abertura do menu, com o fundo azul do PC
  launcher/assets/web/logo.jpg          cabecalho da pagina no navegador
  launcher/scrapcadeboot.zip            animacao de boot do Android (aparece e pulsa)
Logo no kernel (opcional): passe o zImage de fabrica do seu aparelho; o kernel_logo.py grava a logo nele.
Uso: py -3.12 logo_scrapcade.py [zImage_de_fabrica saida_zImage]"""
import io
import math
import os
import subprocess
import sys
import zipfile

from PIL import Image, ImageDraw, ImageEnhance

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
}
BRANCO, MENTA, MENTA_ESC, CINZA = (255, 255, 255, 255), (50, 230, 168, 255), (25, 150, 120, 255), (200, 205, 230, 255)
SOMBRA = (0, 0, 0, 140)


def escreve(d, texto, x, y, b, cor, cor_baixo=None, sombra=True):
    """Desenha o texto com blocos de b pixels; as duas linhas de baixo de cada letra podem ter outra cor."""
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


def logo(B):
    """SCRAP (branco) + CADE (menta) + selo OS (cinza), com blocos de B pixels."""
    img = Image.new("RGBA", (6 * B * 13, 7 * B + 2 * B), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    x = escreve(d, "SCRAP", B // 2, B // 2, B, BRANCO, (215, 220, 240, 255))
    x = escreve(d, "CADE", x, B // 2, B, MENTA, MENTA_ESC)
    b2 = B // 2
    ox, oy = x + B // 2, B // 2 + 7 * B - 7 * b2 - 2 * b2
    w2 = 2 * 6 * b2 + b2
    d.rectangle([ox - b2, oy - b2, ox + w2, oy + 7 * b2 + b2], outline=CINZA, width=max(2, b2 // 3))
    escreve(d, "OS", ox + b2 // 2, oy, b2, CINZA, sombra=False)
    return img.crop(img.getbbox())


def fundo_azul(w, h):
    f = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(f)
    for yy in range(h):
        t = yy / h
        d.line([(0, yy), (w, yy)], fill=(int(11 + 9 * t), int(22 - 12 * t), int(96 - 51 * t), 255))
    return f


def tela(fundo, lg, ganho=1.0):
    t = fundo.copy()
    l2 = lg if ganho == 1.0 else ImageEnhance.Brightness(lg).enhance(ganho)
    t.alpha_composite(l2, ((t.width - lg.width) // 2, (t.height - lg.height) // 2))
    return t.convert("RGB")


def jpeg(im, q=88):
    b = io.BytesIO()
    im.save(b, "JPEG", quality=q, optimize=True)
    return b.getvalue()


os.makedirs(SC, exist_ok=True)
os.makedirs(os.path.join(ASSETS, "web"), exist_ok=True)
lg = logo(11)                                   # ~680 px de largura: cabe com folga nos 800
preto = tela(Image.new("RGBA", (800, 480), (0, 0, 0, 255)), lg)
preto.save(os.path.join(SC, "logo_tela_800.png"), optimize=True)
open(os.path.join(ASSETS, "logo.jpg"), "wb").write(jpeg(tela(fundo_azul(800, 480), lg), 90))
open(os.path.join(ASSETS, "web", "logo.jpg"), "wb").write(jpeg(tela(fundo_azul(800, 288), lg), 86))

# animacao de boot: aparece do preto (uma vez) e depois pulsa de leve (em laco). Poucos quadros: cada um custa
# memoria e tempo de boot. O Android 2.3 so le quadros sem compressao no zip.
zpath = os.path.join(REPO, "launcher", "scrapcadeboot.zip")
fundo = Image.new("RGBA", (800, 480), (0, 0, 0, 255))
with zipfile.ZipFile(zpath, "w", zipfile.ZIP_STORED) as z:
    z.writestr("desc.txt", "800 480 10\np 1 0 part0\np 0 0 part1\n")
    for i, g in enumerate((0.15, 0.35, 0.55, 0.75, 0.9)):
        z.writestr(f"part0/{i:03d}.jpg", jpeg(tela(fundo, lg, g)))
    for i in range(10):
        z.writestr(f"part1/{i:03d}.jpg", jpeg(tela(fundo, lg, 0.9 + 0.1 * (0.5 - 0.5 * math.cos(2 * math.pi * i / 10)))))
print("logo", lg.size, "; boot:", os.path.getsize(zpath), "bytes")

if len(sys.argv) == 3:
    r = subprocess.run([sys.executable, "-I", os.path.join(ART, "kernel", "kernel_logo.py"), sys.argv[1],
                        os.path.join(SC, "logo_tela_800.png"), sys.argv[2], "800"], capture_output=True, text=True)
    print(r.stdout[-600:], r.stderr[-400:])
