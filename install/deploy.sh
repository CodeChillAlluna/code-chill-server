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
    echo $TRAVIS_BRANCH
    if [ "$TRAVIS_BRANCH" == "master" ]
    then
      echo "Master"
      VERSION=`cat VERSION`
      docker tag codechillaluna/code-chill-server codechillaluna/code-chill-server:latest
      docker push codechillaluna/code-chill-server:latest
    else
      echo "Other branch"
      VERSION=$TRAVIS_BRANCH
      echo $VERSION
    fi
    docker tag codechillaluna/code-chill-server codechillaluna/code-chill-server:$VERSION
    docker push codechillaluna/code-chill-server:$VERSION
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
elif [ "$1" == "3" ]
then
  deploy
else
  build_app
  build_dockerfile
  deploy
fi