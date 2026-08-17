#!/bin/bash
#
# Convierte esta carpeta en tu repositorio de GitHub, le aplica los cambios
# nuevos del modo enfoque y los sube.
#
# Uso:  bash subir.sh
#
set -e
cd "$(dirname "$0")"

REPO="https://github.com/wcastillorojas95-sys/habitos.git"

echo
echo "=== 1/6  Preparando el repositorio ==="
if [ ! -d .git ]; then
  git init -b main
  git remote add origin "$REPO"
else
  git remote set-url origin "$REPO" 2>/dev/null || git remote add origin "$REPO"
fi
# Solo para este repositorio, no toca tu configuración global de git.
git config user.name  "Lucas"
git config user.email "wcastillorojas95@gmail.com"

echo
echo "=== 2/6  Descargando la versión 2.0 desde GitHub ==="
git fetch origin
git reset --hard origin/main
git branch --set-upstream-to=origin/main main >/dev/null 2>&1 || true

echo
echo "=== 3/6  Quitando los restos de la versión vieja ==="
# La carpeta tenía la v1.0. Todo lo que no esté en el repo sobra, menos lo mío.
git clean -fd -e _v21 -e subir.sh
# Que git ignore el andamiaje sin ensuciar el .gitignore del proyecto.
grep -qx "subir.sh" .git/info/exclude 2>/dev/null || echo "subir.sh" >> .git/info/exclude
grep -qx "_v21/"    .git/info/exclude 2>/dev/null || echo "_v21/"    >> .git/info/exclude

echo
echo "=== 4/6  Aplicando el modo enfoque ==="
if [ ! -d _v21 ]; then
  echo "ERROR: no encuentro la carpeta _v21 con los archivos nuevos."
  exit 1
fi
cp -R _v21/. .
rm -rf _v21

echo
echo "=== 5/6  Esto es lo que va a subir ==="
git add -A
git status --short

echo
echo "=== 6/6  Subiendo a GitHub ==="
echo
echo "  Si te pide usuario:     wcastillorojas95-sys"
echo "  Si te pide contraseña:  NO es la de tu cuenta. Necesitas un token."
echo "  Sácalo en: github.com/settings/tokens  ->  Generate new token (classic)"
echo "             marca el permiso 'repo' y pega el token como contraseña."
echo
git commit -m "Modo enfoque: cronometro con fijado de pantalla y firma de debug fija"
git push -u origin main

echo
echo "======================================================================"
echo " Listo. La compilación arranca sola. Míralo aquí:"
echo " https://github.com/wcastillorojas95-sys/habitos/actions"
echo
echo " A partir de ahora, para subir cambios basta con:"
echo "   git add -A && git commit -m \"lo que cambiaste\" && git push"
echo "======================================================================"
