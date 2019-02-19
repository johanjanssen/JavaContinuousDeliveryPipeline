docker stop $(docker ps -a -q)
docker system prune --all --volumes --force

