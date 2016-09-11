Mesos-Master with frameworks
=========

Содержит следующие компоненты

* Mesos-master с фреймворками: - менеджмент ресурсов
    * chronos  - планировщик заданий
    * marathon - long running tasks менеджер

После установки можно потыкаться можно в следующие ресурсы:

* chronos порт 4400
* marathon порт 8080
* mesos master порт 5050

Для обеспечения доступа по 80 порту, необходима установленная роль nginx-oem

* mesos
    * Копирует mesos.conf в nginx_config_dir:/etc/nginx/conf.d - содержит виртуальный хост hostname.mesos
    * Копирует mesos.location в nginx_location_config_dir:/etc/nginx/location.d - мапит мезос со статическими ресурсами на hostname/mesos
    * Копирует mesos.upstreamв nginx_upstream_config_dir:/etc/nginx/upstream.d - добавляет upstream для mesos
* marathon
    * Копирует marathon.conf в nginx_config_dir:/etc/nginx/conf.d - содержит виртуальный хост hostname.marathon
    * Копирует marathon.location в nginx_location_config_dir:/etc/nginx/location.d - мапит мезос со статическими ресурсами на hostname/marathon
    * Копирует marathon.upstreamв nginx_upstream_config_dir:/etc/nginx/upstream.d - добавляет upstream для marathon
* chronos
    * Копирует chronos.conf в nginx_config_dir:/etc/nginx/conf.d - содержит виртуальный хост hostname.chronos
    * Копирует chronos.location в nginx_location_config_dir:/etc/nginx/location.d - мапит мезос со статическими ресурсами на hostname/chronos
    * Копирует chronos.upstreamв nginx_upstream_config_dir:/etc/nginx/upstream.d - добавляет upstream для chronos

Requirements
------------

* RedHat
* Docker 1.7+
* _docker images_: необходимо наличие образов в репозитории
* zookeeper configuration. See [zookeeper role](http://git/projects/DOPS/repos/zookeeper/browse)
* Optional nginx-oem role

необходимо указать следующие docker образы с помощью переменных:

* docker.chronos
* docker.marathon
* docker.master

Default values:

    docker:
      master: mesos-master
      chronos: mesos-chronos
      marathon: mesos-marathon

    mesos:
      marathon
        subscriber_type: http_callback
        http_hooks: http://localhost:8000

    mesos_work_dir: /data/mesos

Role Variables
--------------

Use tag *flush_all* for delete all mesos files
Use --extra-vars "flush_all=true" for delete all es trails

Example variables:

    mesos_ip: eager set ip
    mesos.quorum=2
    mesos.marathon.zk=zk://zk_node1:2181,zk_node2:2181,zk_node3:2181/marathon
    mesos.zk=zk://zk_node1:2181,zk_node2:2181,zk_node3:2181/mesos
    mesos.zk_hosts=zk_node1:2181,zk_node2:2181,zk_node3:2181
    mesos.external_ip по умолчанию советую использовать следующее значение "{{ mesos_ip | d(ipv4_host) | d(ansible_default_ipv4.address) }}"

Связывание фреймворков с мастером мезоса, происходит через зукипер. Поэтому в настройках указывается пул адресов кластера зукипера и имя zookeeper корня

см http://git/projects/DOPS/repos/ansible-zookeeper/browse

Dependencies
------------

* git+http://git/scm/dops/ansible-base.git role base

Example Playbook
----------------

Including an example of how to use your role (for instance, with variables passed in as parameters) is always nice for users too:

    - hosts: mesos-master
      remote_user: dockeradm
      sudo: yes
      vars:
        docker:
          master: YOUR_PRIVATE_REPO/mesos-master
          chronos: YOUR_PRIVATE_REPO/mesos-chronos
          marathon: YOUR_PRIVATE_REPO/mesos-marathon
        mesos:
          quorum: 2
          external_ip: "{{ ansible_enp0s9.ipv4.address|default(ansible_default_ipv4.address) }}"
          marathon:
            zk: zk://zk_node1:2181,zk_node2:2181,zk_node3:2181/marathon
            http_hooks: "{{ groups['mesos-slave']| map('map_format', 'http://%s:8000')| join(',') }}"
          zk: zk://zk_node1:2181,zk_node2:2181,zk_node3:2181/mesos
          zk_hosts: zk_node1:2181,zk_node2:2181,zk_node3:2181
      roles:
        - role: mesos-master

Author Information
------------------

Tolkachev Kirill tolk.kv@gmail.com

