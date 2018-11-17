# Code & Chill
[![Travis (.org) branch](https://img.shields.io/travis/CodeChillAlluna/code-chill-server/master.svg?style=for-the-badge)](https://travis-ci.org/CodeChillAlluna/code-chill-server) 
[![Codacy branch 
grade](https://img.shields.io/codacy/grade/35fabe502d4341cc9096e5e63812f348/master.svg?style=for-the-badge)](https://app.codacy.com/project/Lulu300/code-chill-server/dashboard) 
[![Coveralls github 
branch](https://img.shields.io/coveralls/github/CodeChillAlluna/code-chill-server/master.svg?style=for-the-badge)](https://coveralls.io/github/CodeChillAlluna/code-chill-server) 
[![Code 
Climate](https://img.shields.io/codeclimate/maintainability/CodeChillAlluna/code-chill-server.svg?style=for-the-badge)](https://codeclimate.com/github/CodeChillAlluna/code-chill-server) 
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg?style=for-the-badge)](https://www.gnu.org/licenses/gpl-3.0)

Master project : Online development environment

Code&Chill is a web application written in Java and React. It gives users the possibility to use their development environment in a browser. No more worries about setup so just code, 
and chill.

If you want to know more about this project check our [website](https://codechillalluna.github.io/code-chill/).

This repository contains the server part of the project, if you want to check the client part click [here](https://github.com/CodeChillAlluna/code-chill-client).



## Summary

- Installation for contributing to this project (click [here](#development-environment))
- Installation of the application (click [here](#installation))



## Development Environment

Here is the setup to installing the development environment if you want to contributing to this project.

### Requirements

- [Virtualbox](https://www.virtualbox.org), or other virtualization tools
- [Vagrant](https://www.vagrantup.com)



### Clone repo and create branch

First you need to clone the repo. The master branch is protected, so you need to create a branch to start developing.

```sh
git clone https://github.com/CodeChillAlluna/code-chill-server.git
cd code-chill-server
git checkout -b your_branch
```



### Setup vagrant

Then you need to start the vagrant to create the VM.

```sh
vagrant up
```

#### Common vagrant commands

- Connect to the VM: `vagrant ssh`
- Shutdown the VM: `vagrant halt`
- Launch the VM: `vagrant up`
- Reload the VM: `vagrant reload`
- Delete the VM: `vagrant destroy`
- Provisioning the VM: `vagrant provision`



### Start application

To start or building the application or execute tests, you need to access the VM via SSH.

```sh
vagrant ssh
```

Before starting the application don't forget to start the server. If you have not installed it yet click [here](https://github.com/CodeChillAlluna/code-chill-server).



#### Start the application

```sh
mvn spring-boot:run
```

Then you can go to : http://localhost:3000 to view the app.



#### Execute tests

```sh
mvn test -DforkCount=0
```



#### Build the application

```sh
mvn clean package -DforkCount=0
```



## Installation 

Here is the setup to installing the production environment if you want to use this project.

### Requirements

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)



### Configure & Start application

Create a file named `application.yml` wherever you want and change the values you need.

```javascript
# config context path to "/" by setting an empty string
server:
  contextPath:


spring:
  # Spring profile
  profiles: "prod"
  # JACKSON
  jackson:
    serialization:
      INDENT_OUTPUT: true
  mail:
    host: 
    port: 587
    username: "CHANGEME"
    password: "CHANGEME"
    smtp:
      protocol: smtp
      debug: true
    properties.mail.smtp:
      auth: true
      starttls.enable: true

  datasource:
    url: jdbc:postgresql://code-chill-db/codechill
    username: code
    password: chill

  jpa:
    generate:
      ddl: true
    hibernate:
      ddl-auto: update

jwt:
  header: Authorization
  secret: mySecret
  expiration: 604800
  route:
    authentication:
      path: auth
      refresh: refresh

logging:
  file: /var/log/codechill-server.log
  pattern:
    file: "%d %-5level [%thread] %logger : %msg%n"
  level:
    ROOT: ERROR

app:
  # URL of the docker API to use for client environment
  dockerurl: http://localhost:2375
  # URL of the server where Code&Chill is installed
  clienturl: http://localhost:3000
  # Range of port to use for user environment
  minPort: 64000
  maxPort: 64050
```



Then create a file named `docker-compose.yml` wherever you want.

```yaml
version: "3.3"

services:
  code-chill-server:
    image: codechillaluna/code-chill-server
    volumes:
    # Change "$HOME/config" by the folder where your application.yml is.
    - $HOME/config:/config/
    ports:
    # Change the first value to the port you want to use on your host.
    - "8080:8080"
    depends_on:
    - code-chill-db
  code-chill-db:
    image: postgres:10.5-alpine
    environment:
      # Change postgres credentials and database name.
      POSTGRES_PASSWORD: chill
      POSTGRES_USER: code
      POSTGRES_DB: codechill
```



Now you just have to go on the folder where your `docker-compose.yml` is and execute this command to start Code&Chill Server : 

```sh
docker-compose up
```

Now you can install [Code&Chill Client](https://github.com/CodeChillAlluna/code-chill-client#installation) and start coding on Code&Chill !