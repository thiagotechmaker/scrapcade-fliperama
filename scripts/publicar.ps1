# ScrapCade no iChroma: publica uma versao nas GitHub Releases (precisa do gh logado: gh auth login).
# Uso (no PowerShell do Windows, com a branch dev pronta e enviada):
#   powershell -File scripts\publicar.ps1 [-Apk caminho\launcher.apk]
# A versao vem do android:versionName do launcher/AndroidManifest.xml. O script confere tudo antes de publicar:
#   - o APK e dessa versao (aapt), a secao [versao] existe no CHANGELOG e a tag ainda nao existe
#   - nada por commitar e a dev igual a origin/dev
# Depois: main recebe a dev (fast-forward), tag anotada v<versao>, push e Release de pre-lancamento com o APK
# (scrapcade-ichroma-v<versao>.apk) e as notas tiradas do CHANGELOG.
# Use o APK assinado com a mesma chave das versoes anteriores, senao "adb install -r" recusa a atualizacao.
param([string]$Repo = "thiagotechmaker/scrapcade-fliperama",
      [string]$Apk = (Join-Path (Split-Path $PSScriptRoot) "launcher\launcher.apk"))
# "Continue": no PowerShell 5.1, com "Stop" qualquer texto que o git ou o gh escrevem no stderr vira erro.
# Cada comando externo e conferido pelo $LASTEXITCODE.
$ErrorActionPreference = "Continue"
$raiz = Split-Path $PSScriptRoot
function Para($m) { Write-Host "PARADO: $m"; exit 1 }
function G { git -C $raiz -c user.name=T-Tech-Maker -c user.email=T-Tech-Maker@users.noreply.github.com @args; if ($LASTEXITCODE) { Para "git $args" } }

[xml]$man = Get-Content (Join-Path $raiz "launcher\AndroidManifest.xml") -Encoding UTF8
$v = $man.manifest.versionName
if ($v -notmatch '^\d+\.\d+\.\d+(-[0-9A-Za-z.]+)?$') { Para "versionName '$v' nao e SemVer" }

$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { "$env:LOCALAPPDATA\Android\Sdk" }
$aapt = Get-ChildItem "$sdk\build-tools\*\aapt.exe" | Sort-Object FullName -Descending | Select-Object -First 1
if (-not (Test-Path $Apk)) { Para "falta o APK $Apk (rode launcher\build.ps1)" }
$badge = & $aapt.FullName dump badging $Apk | Select-Object -First 1
if ($badge -notmatch "versionName='$([regex]::Escape($v))'") { Para "o APK nao e da versao $v ($badge)" }

$cl = Get-Content (Join-Path $raiz "CHANGELOG.md") -Encoding UTF8
$ini = ($cl | Select-String -Pattern ("^## \[" + [regex]::Escape($v) + "\]")).LineNumber
if (-not $ini) { Para "falta a secao [$v] no CHANGELOG.md" }
$fim = ($cl | Select-Object -Skip $ini | Select-String -Pattern "^## \[" | Select-Object -First 1).LineNumber
$secao = if ($fim) { $cl[$ini..($ini + $fim - 2)] } else { $cl[$ini..($cl.Count - 1)] }

gh release view "v$v" -R $Repo *> $null
if ($LASTEXITCODE -eq 0) { Para "a versao v$v ja foi publicada: suba o versionName" }
if (git -C $raiz status --porcelain) { Para "ha mudancas por commitar" }
G fetch -q origin
if ((git -C $raiz rev-parse dev) -ne (git -C $raiz rev-parse origin/dev)) { Para "a dev local e a do GitHub estao diferentes: de push (ou pull) antes" }

G fetch -q . dev:main
G tag -a "v$v" -m "v$v" main
G push -q origin main "v$v"

$notas = "**ScrapCade no iChroma v$v**`n`n" +
  "**Instalar:** com o aparelho preparado (veja o [tutorial](https://github.com/$Repo/blob/main/INSTALACAO.md)), ``adb install -r`` com o APK abaixo. Os emuladores e os jogos não acompanham o app.`n`n" +
  ($secao -join "`n").Trim() + "`n`nHistórico completo no [CHANGELOG](https://github.com/$Repo/blob/main/CHANGELOG.md)."
$tmp = Join-Path $env:TEMP "scrapcade-ichroma-v$v.apk"
Copy-Item $Apk $tmp -Force
$nt = New-TemporaryFile
[IO.File]::WriteAllText($nt, $notas, (New-Object Text.UTF8Encoding $false))   # UTF-8 sem BOM
gh release create "v$v" -R $Repo --verify-tag --latest --title "ScrapCade no iChroma v$v" --notes-file $nt $tmp
$ok = $LASTEXITCODE
Remove-Item $tmp, $nt
if ($ok) { Para "gh release create falhou (a tag v$v ja foi enviada; rode de novo so o gh release create)" }
