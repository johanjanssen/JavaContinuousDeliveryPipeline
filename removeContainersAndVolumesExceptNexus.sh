docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
docker volume rm dockerregistry-auth
docker volume rm dockerregistry-certs
docker volume rm dockerregistry-data
docker volume rm gitlab-data-etc
docker volume rm gitlab-data-log
docker volume rm gitlab-data-opt
docker volume rm jenkins-data
docker volume rm jenkins-mavendata
docker volume rm sonarqube-bundled-plugins
docker volume rm sonarqube-conf
docker volume rm sonarqube-data
docker volume rm sonarqube-extensions
