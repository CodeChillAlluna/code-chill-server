# -*- mode: ruby -*-
# vi: set ft=ruby :

if ARGV[0] != 'plugin'

  required_plugins = [
    'vagrant-disksize'
  ]         
  plugins_to_install = required_plugins.select { |plugin| not Vagrant.has_plugin? plugin }
  if not plugins_to_install.empty?

    puts "Installing plugins: #{plugins_to_install.join(' ')}"
    if system "vagrant plugin install #{plugins_to_install.join(' ')}"
      exec "vagrant #{ARGV.join(' ')}"
    else
      abort "Installation of one or more plugins has failed. Aborting."
    end

  end
end

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"
  config.vm.hostname = "CodeChillServer"
  config.disksize.size = "25GB"

  config.vm.network "public_network"
  config.vm.network "forwarded_port", guest: 8080, host: 8080, host_ip: "127.0.0.1"
  config.vm.network "forwarded_port", guest: 2375, host: 2375, host_ip: "127.0.0.1"
  config.vm.network "forwarded_port", guest: 2376, host: 2376, host_ip: "127.0.0.1"
  config.vm.network "forwarded_port", guest: 80, host: 9000, host_ip: "127.0.0.1"

  for i in 64000..64050
    config.vm.network :forwarded_port, guest: i, host: i, host_ip: "127.0.0.1"
  end

  config.vm.provider "virtualbox" do |vb|
      vb.memory = 1536
      vb.name = "Code&ChillServer"
  end
  config.vm.provision "shell", path: "install/install.sh"
end