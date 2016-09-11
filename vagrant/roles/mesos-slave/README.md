Mesos-slave
=========

Мезос слейв устанавливается с помощью rpm пакета.
Так же устанавливается mesos-master, в качестве зависимости. Но он находится в выключенном состоянии,
так что в любой момент ваш слейв можно сделать мастером :)

Requirements
------------

Used docker images:

* RHel 7+
* Docker 1.7+

Example nginx config for discovery installed services:

Bamboo proxy: для доступа к приложениям на разных узлах

    upstream backend_bamboo_proxy {
            server {{ansible_default_ipv4['address']}}:81 weight=10;
    }

    server {
        listen       80;
        server_name  {{ ansible_hostname }}.proxy;

        access_log  /var/log/nginx/bamboo_proxy.access.log;

        location / {
            proxy_pass http://backend_bamboo_proxy;
        }
    }

Bamboo: мониторинг работы bamboo и его конфигурирования на лету

    upstream backend_bamboo {
            server {{ansible_default_ipv4['address']}}:8000 weight=10;
    }

    server {
        listen       80;
        server_name  {{ ansible_hostname }}.bamboo;

        access_log  /var/log/nginx/bamboo.access.log;

        location / {
            auth_basic "Restricted";
            auth_basic_user_file /etc/nginx/httppasswd;
            proxy_pass http://backend_bamboo;
        }
    }

Общий конфиг для доступа по хосту:

    server {
        listen       80;
        server_name  {{ ansible_hostname }};

        # update state for bamboo
        location /api/state/ {
            access_log  /var/log/nginx/bamboo.access.log;
            auth_basic "Restricted";
            auth_basic_user_file /etc/nginx/httppasswd;
            proxy_pass http://backend_bamboo/api/state/;
        }

        location /bamboo/ {
            access_log  /var/log/nginx/bamboo.access.log;
            auth_basic "Restricted";
            auth_basic_user_file /etc/nginx/httppasswd;
            proxy_pass http://backend_bamboo/;
        }

        location /health {
           return 200 "{}";
        }
    }

Role Variables
--------------

Use tag *flush_all* for delete all mesos slave files
Use --extra-vars "flush_all=true" for delete all es trails

files/mesos-slave/* содержит конфигурацию mesos-slave

containerizers - включает поддержку докера (по умолчаниб докер + стандартные контейнеры)
docker_stop_timeout - таймаут для остановки контейнера
executor_registration_timeout -  если слейв отвалился, таски ждут столько времени перед тем как пометить ноду как деградированную

In inventory file example variables:

    mesos_zk=zk://zoo_node_1:2181,zoo_node_2:2181,zoo_node_3:2181/mesos
    mesos_port_range=5900-20000

    mesos_slave_config_dir
    mesos_config_dir
    mesos_slave_workdir - по умолчанию /data/mesos
    mesos_slave_attributes: - структура dict, которая сохраняется в mesos_slave_config_dir/attributes/item.key << item.val
        rack:
          description: 'Datacenter or rack'
          val: 'default'
        type:
          description: 'type of slave. Application/data/mq/compute etc'
          val: 'application'

mesos_version: 0.28.2 - rpm version, that will be loaded and installed through artifactory proxy

mesos_slave_config_dir: /etc/mesos-slave
mesos_config_dir: /etc/mesos
mesos_slave_workdir: /data/mesos

mesos_zk: zk://zk_node1:2181,zk_node2:2181,zk_node3:2181/mesos
mesos_port_range: 5900-20000
mesos_gc_delay: 3days период чистки контейнеров в mesos-slave может быть в формате: 3days, 2weeks(согласно документации mesos http://mesos.apache.org/documentation/latest/configuration/)

See ./files/defaults.yml for mor information about default variables

Example Playbook
----------------

- hosts: mesos-slave
  become: true
  bcome_user: root
  sudo: yes
  vars:
    mesos_zk: zk://zk_node1:2181,zk_node2:2181,zk_node3:2181/mesos
  roles:
   - { role: mesos-slave }
