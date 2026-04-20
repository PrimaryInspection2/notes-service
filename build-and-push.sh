#!/bin/bash
set -e

DOCKER_USERNAME=andreyjava404

echo "Building jar..."
mvn clean package -DskipTests

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>&1 | awk 'END{print $NF}')
GIT_HASH=$(git rev-parse --short HEAD)
TAG="$VERSION-$GIT_HASH"

echo "Building Docker image..."
docker build -t notes-service .

echo "Tagging image: $TAG"
docker tag notes-service $DOCKER_USERNAME/notes-service:$TAG
docker tag notes-service $DOCKER_USERNAME/notes-service:latest

echo "Pushing image..."
docker push $DOCKER_USERNAME/notes-service:$TAG
docker push $DOCKER_USERNAME/notes-service:latest

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
sed -i "s|  tag:.*|  tag: $TAG|" "$SCRIPT_DIR/k8s/notes-service-chart/values.yaml"
sed -i "s|^appVersion:.*|appVersion: \"$TAG\"|" "$SCRIPT_DIR/k8s/notes-service-chart/Chart.yaml"

echo "Done! Image: $DOCKER_USERNAME/notes-service:$TAG"