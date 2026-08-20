#!/usr/bin/env bash
#
# Déploiement de la pile Doni-Doni sur le serveur de production.
#
# Ce script vit dans le dépôt plutôt que dans le workflow GitHub. La raison
# est concrète : appleboy/ssh-action transmet le script au shell distant via
# `bash -c`, et ce transport altère les blocs multilignes élaborés — une
# boucle `for` contenant un `case` a produit un « syntax error near
# unexpected token ; » en plein déploiement, après la recréation du
# conteneur. Ici, bash lit un fichier local : rien ne peut le déformer.
#
# Effet de bord utile : le déploiement est reproductible à la main.
#
#   bash scripts/deploiement.sh <révision-de-repli>
#
# La révision de repli est celle vers laquelle revenir si l'API ne devient
# jamais disponible. Le pipeline la relève avant de mettre le code à jour.

set -euo pipefail

REVISION_PRECEDENTE=${1:?Révision de repli manquante (usage : $0 <révision>)}

SERVICE=doni-doni-api
TENTATIVES=40
DELAI=10

cd "$(dirname "$0")/.."

# Le .env porte les secrets ; il n'est pas versionné.
test -f .env || { echo "Fichier .env absent sur le serveur"; exit 1; }

echo "Révision de repli  : $REVISION_PRECEDENTE"
echo "Révision à déployer : $(git rev-parse HEAD)"

# L'image est construite AVANT toute interruption de service : un échec de
# build laisse l'ancienne version en ligne.
docker compose build "$SERVICE"

# `up -d` ne recrée que les services dont la définition a changé, il n'y a
# donc pas de `down` préalable.
docker compose up -d --remove-orphans

echo "Attente de la disponibilité de l'API..."
for tentative in $(seq 1 "$TENTATIVES"); do
  etat=$(docker inspect --format '{{.State.Health.Status}}' "$SERVICE" 2>/dev/null || echo absent)
  case "$etat" in
    healthy)
      echo "API disponible."
      exit 0
      ;;
    absent|"<no value>"|"")
      echo "Conteneur absent ou sans sonde de santé (état : $etat)."
      break
      ;;
  esac
  echo "  état : $etat (tentative $tentative/$TENTATIVES)"
  sleep "$DELAI"
done

echo "L'API n'est pas devenue disponible — retour arrière."
docker compose logs --tail 80 "$SERVICE" || true
git reset --hard "$REVISION_PRECEDENTE"
docker compose build "$SERVICE"
docker compose up -d --remove-orphans
exit 1
