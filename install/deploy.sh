#!/bin/bash

docker_path=docker
docker_build=$docker_path/codechill-server.jar

build_app () {
    # Build the app
    mvn clean package -DforkCount=0
}

build_dockerfile() {
    # Build dockerfile
    cd $docker_path
    cp ../target/spring*.jar codechill-server.jar
    mkdir $HOME/config
    cp config/application.yml $HOME/config/application.yml
    docker-compose build
    cd ..
    rm -r $docker_build
}

deploy() {
    echo "TODO"
}

if [ "$1" == "0" ]
then
  build_app
  build_dockerfile
elif [ "$1" == "1" ]
then
  build_dockerfile
elif [ "$1" == "2" ]
then
  build_dockerfile
  deploy
else
  build_app
  build_dockerfile
  deploy
fi