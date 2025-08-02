# Stop all containers (even outside Compose)
docker ps -q | xargs -r docker stop

# Remove all containers
docker ps -aq | xargs -r docker rm -f

# Prune everything: networks, images, volumes
docker system prune -af --volumes