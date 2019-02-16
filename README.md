# Code & Chill
[![Server version](https://img.shields.io/github/tag/CodeChillAlluna/code-chill-server.svg?label=version&style=for-the-badge)](https://github.com/CodeChillAlluna/code-chill-server)
[![Travis (.org) branch](https://img.shields.io/travis/CodeChillAlluna/code-chill-server/master.svg?style=for-the-badge)](https://travis-ci.org/CodeChillAlluna/code-chill-server) 
[![Codacy branch grade](https://img.shields.io/codacy/grade/35fabe502d4341cc9096e5e63812f348/master.svg?style=for-the-badge)](https://app.codacy.com/project/Lulu300/code-chill-server/dashboard) 
[![Coveralls github branch](https://img.shields.io/coveralls/github/CodeChillAlluna/code-chill-server/master.svg?style=for-the-badge)](https://coveralls.io/github/CodeChillAlluna/code-chill-server) 
[![Code Climate](https://img.shields.io/codeclimate/maintainability/CodeChillAlluna/code-chill-server.svg?style=for-the-badge)](https://codeclimate.com/github/CodeChillAlluna/code-chill-server) 
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

Master project : Online development environment

Code&Chill is a web application written in Java and React. It gives users the possibility to use their development environment in a browser. No more worries about setup so just code, 
and chill.

If you want to know more about this project check our [website](https://codechillalluna.github.io/code-chill/).

This repository contains the server part of the project, if you want to check the client part click [here](https://github.com/CodeChillAlluna/code-chill-client).



## Summary

- [Installation for contributing to this project](#development-environment)
- [Installation of the application](#installation)



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



#### Start the API

```sh
mvn spring-boot:run

# You can also use our custom script to clean docker container before start
./server run
```

Then you can request : http://localhost:8080 to view use the API.



#### Execute tests

```sh
mvn test

# You can also use our custom script to clean docker container before start
./server test
```



#### Build the application

```sh
mvn clean package

# You can also use our custom script to clean docker container before start
./server package
```



#### Format your code

```sh
mvn fmt:format
```

Remember to format your code before commit, otherwise your build will fail.



#### API Documentation

If you want to see the doc for this API, go to http://localhost:8080/swagger-ui.html



## Installation 

The procedure to install Code&Chill is accessible in our main [repository](https://github.com/CodeChillAlluna/code-chill#installation).
