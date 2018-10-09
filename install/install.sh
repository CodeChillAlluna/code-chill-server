#!/bin/bash

vagrant=/vagrant
HOME_DIR=/home$vagrant

export DEBIAN_FRONTEND=noninteractive

print_help () {
  echo "#################################################"
  echo "                     HELP                        "
  echo "#################################################"
  echo "Connect to the VM: 'vagrant ssh'"
  echo "Shutdown the VM: 'vagrant halt'"
  echo "Launch the VM: 'vagrant up'"
  echo "Reload the VM: 'vagrant reload'"
  echo "Delete the VM: 'vagrant destroy'"
  echo "Verify packages are up to date: 'vagrant provision'"
}

docker_install () {
  # Installation de Docker
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
  sudo apt-key fingerprint 0EBFCD88
  sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
  sudo apt-get update
  sudo apt-get install docker-ce -y
  sudo su -
  echo -e "[Unit]
  Description=Docker Application Container Engine
  Documentation=https://docs.docker.com
  After=network-online.target docker.socket firewalld.service
  Wants=network-online.target
  Requires=docker.socket\n
  [Service]
  Type=notify
  # the default is not to use systemd for cgroups because the delegate issues still
  # exists and systemd currently does not support the cgroup feature set required
  # for containers run by docker
  ExecStart=/usr/bin/dockerd -H=tcp://0.0.0.0:2375
  ExecReload=/bin/kill -s HUP \$MAINPID
  LimitNOFILE=1048576
  # Having non-zero Limit*s causes performance problems due to accounting overhead
  # in the kernel. We recommend using cgroups to do container-local accounting.
  LimitNPROC=infinity
  LimitCORE=infinity
  # Uncomment TasksMax if your systemd version supports it.
  # Only systemd 226 and above support this version.
  TasksMax=infinity
  TimeoutStartSec=0
  # set delegate yes so that systemd does not reset the cgroups of docker containers
  Delegate=yes
  # kill only the docker process, not all processes in the cgroup
  KillMode=process
  # restart the docker process if it exits prematurely
  Restart=on-failure
  StartLimitBurst=3
  StartLimitInterval=60s\n
  [Install]
  WantedBy=multi-user.target" > /lib/systemd/system/docker.service
  systemctl daemon-reload
  systemctl restart docker
  export DOCKER_HOST=tcp://localhost:2375
  if ! grep -qF "DOCKER_HOST=tcp://localhost:2375" /etc/environment
  then
    echo "DOCKER_HOST=tcp://localhost:2375" >> /etc/environment
  fi
  source /etc/environment
  systemctl restart docker
}

dockerfiles () {
  # Installation of docker files
  git clone https://github.com/CodeChillAlluna/DockerFiles.git
  docker build -f DockerFiles/CodeChill-Ubuntu/DockerFile -t codechill/ubuntu-base .
  docker build -f DockerFiles/CodeChill-Ubuntu-User/DockerFile -t codechill/ubuntu-base-user .
  rm -R DockerFiles
  docker run --name codechill -dti codechill/ubuntu-base-user /bin/bash
}

db () {
  DB_USER=code
  DB_PWD=chill
  DB_NAME=codechill
  DB_NAME_TEST=codechill_test

  sudo su - postgres -c psql <<EOF
CREATE DATABASE $DB_NAME;
CREATE USER $DB_USER WITH PASSWORD '$DB_PWD';

ALTER ROLE $DB_USER SET client_encoding TO 'utf8';
ALTER ROLE $DB_USER SET default_transaction_isolation TO 'read committed';
ALTER ROLE $DB_USER SET timezone TO 'UTC';

GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
ALTER USER $DB_USER CREATEDB;
EOF

  sudo su - postgres -c psql <<EOF
CREATE DATABASE $DB_NAME_TEST;

GRANT ALL PRIVILEGES ON DATABASE $DB_NAME_TEST TO $DB_USER;
ALTER USER $DB_USER CREATEDB;
EOF

}

install () {
  if ! grep -qF "cd "$vagrant $HOME_DIR/.bashrc
  then
          echo "cd "$vagrant >> $HOME_DIR/.bashrc
  fi

  # Update system
  sudo apt-get update
  sudo apt-get upgrade -y

  # Install dependencies for installation
  sudo apt-get install software-properties-common -y
  sudo apt-get install curl -y

  # Install git
  sudo apt-get install -y git

  # Install jdk
  sudo apt-get install -y openjdk-8-jdk openjdk-8-jre

  # Install build automation
  sudo apt-get install -y maven

  # Installation de postgresql
  sudo apt-get install -y python-dev
  sudo apt-get install -y libpq-dev
  sudo apt-get install -y postgresql
  sudo apt-get install -y postgresql-contrib

  # Configuration de la BDD postgresql
  db

  # Installation de Docker
  docker_install

  # Installation docker files
  dockerfiles

  # Install nginx
  sudo apt-get install -y nginx

  print_help
}

echo $1

if [ "$1" -eq "0" ]
then
  db
elif [ "$1" -eq "1" ]
then
  docker_install
elif [ "$1" -eq "2" ]
then
  dockerfiles
elif [ "$1" -eq "3" ]
then
  docker_install
  dockerfiles
else
  install
fi
