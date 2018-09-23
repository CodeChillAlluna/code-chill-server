#!/bin/bash

vagrant=/vagrant
HOME_DIR=/home$vagrant

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

export DEBIAN_FRONTEND=noninteractive

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
sudo sh $vagrant/install/db.sh

# Install nginx
sudo apt-get install nginx

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

sudo sh $vagrant/install/docker.sh

print_help
