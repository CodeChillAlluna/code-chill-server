#!/bin/bash

clean_docker() {
  docker stop $(docker ps -a -q)
  docker rm $(docker ps -a -q)
}

format_code() {
  mvn fmt:format
}

run_server() {
  clean_docker
  format_code
  mvn spring-boot:run
}

run_test() {
  clean_docker
  format_code
  mvn test
}

package() {
  clean_docker
  format_code
  mvn clean package
}

if [ "$1" == "run" ]
then
  run_server
elif [ "$1" == "test" ]
then
  run_test
elif [ "$1" == "package" ]
then
  package
elif [ "$1" == "format" ]
then
  format_code
else
  echo -e 'Wrong arg, Usage :\n1) run (Run the server)\n2) test (Run all tests)\n3) package (Make a jar)\n4) format (format code)'
fi