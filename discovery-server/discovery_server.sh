#!/usr/bin/env bash
set -Eeuo pipefail

# ==========================================
# Configuration
# ==========================================
IMAGE="localhost/discovery-server:1.0"
CONTAINER="discovery-server"
CONTAINERFILE="Containerfile"
PORT="8761"

# ==========================================
# Vérifications
# ==========================================
for cmd in buildah podman curl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "ERREUR: commande introuvable: $cmd"
    exit 1
  fi
done

if [[ ! -f "$CONTAINERFILE" || ! -f "pom.xml" ]]; then
  echo "ERREUR: lance ce script depuis le dossier discovery-server/"
  exit 1
fi

# ==========================================
# 1. Buildah
# ==========================================
echo "=== Buildah version ==="
buildah version

echo "=== Buildah info ==="
buildah info

echo "=== Construction de l'image ==="
buildah build \
  --layers \
  --tag "$IMAGE" \
  --file "$CONTAINERFILE" .

echo "=== Images disponibles ==="
buildah images

echo "=== Inspection de l'image ==="
buildah inspect "$IMAGE"

# ==========================================
# 2. Podman
# ==========================================
echo "=== Inspection Podman ==="
podman image inspect "$IMAGE"

echo "=== Suppression de l'ancien conteneur ==="
if podman container exists "$CONTAINER"; then
  podman rm --force "$CONTAINER"
fi

echo "=== Démarrage du conteneur ==="
podman run --detach \
  --name "$CONTAINER" \
  --publish "127.0.0.1:${PORT}:${PORT}" \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=64m \
  --cap-drop=all \
  --security-opt=no-new-privileges \
  --memory=512m \
  --cpus=1 \
  "$IMAGE"

echo "=== Logs du conteneur ==="
podman logs "$CONTAINER"

echo "=== Attente du démarrage ==="
READY=0
for i in $(seq 1 30); do
  if curl --fail --silent "http://127.0.0.1:${PORT}/actuator/health"; then
    echo
    READY=1
    break
  fi
  sleep 2
done

if [[ "$READY" -ne 1 ]]; then
  echo "ERREUR: Eureka ne répond pas."
  podman logs "$CONTAINER"
  exit 1
fi

echo "=== Eureka Dashboard ==="
echo "http://localhost:${PORT}"

# ==========================================
# 3. Minikube (optionnel)
# ==========================================
if command -v minikube >/dev/null 2>&1; then
  echo "=== État de Minikube ==="
  minikube status

  read -r -p "Charger l'image dans Minikube ? [y/N] " answer

  if [[ "$answer" =~ ^[Yy]$ ]]; then
    minikube image load "$IMAGE"
    minikube image ls
  fi
else
  echo "Minikube non installé : étape ignorée."
fi

echo "=== Terminé ==="

#  Deployment and Service
#kubectl apply -f deploy/kubernetes/discovery-server.yaml

# Pods
#kubectl get pods

# Deployment
#kubectl rollout status deployment/discovery-server

#  Logs
#kubectl logs deployment/discovery-server

# open Dashboard
#kubectl port-forward service/discovery-server 8761:8761
