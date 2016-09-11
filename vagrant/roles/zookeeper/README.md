Zookeeper
=========

Install zookeper in docker container

Requirements
------------

* Docker 1.7+
* RedHat 7+

Role Variables
--------------

Use tag *flush_all* for delete all mesos slave files
Use --extra-vars "flush_all=true" for delete all es trails

* docker.zk - zookeeper image
* zookeeper_config_dir - /etc/mesos/zookeeper/
* zookeeper_id - explicit value of node's id (default value is the host number, if any)
* zookeeper_group_hosts - host for resolve all zk nodes/ Use only if `zookeeper_hosts` is not defined. Default value - `zk`
* zookeeper_hosts - hosts by Mikhail. I dont understand what does it mean
* zk_hostname - attach to each host for specify public hostname for resolve node

Dependencies
------------

Docker configure for acceess to your repository

Example Playbook
----------------

Including an example of how to use your role (for instance, with variables passed in as parameters) is always nice for users too:

    - hosts: zk
      remote_user: dockeradm
      sudo: yes
      vars:
        docker:
          zk: YOUR_PRIVATE_REPOSITORY/zookeeper
      roles:
        - role: zookeeper

Example сluster mode:

zookeeper_hosts:
    - { zk_hostname: 'mesos-master', zk_id: 1 }
    - { zk_hostname: 'mesos-slave', zk_id: 2 }
    - { zk_hostname: 'mesos-marathon', zk_id: 3 }

In inventory file:

zookeeper_group_hosts: 'groups_zk'

[groups_zk]
supermegahost_in_dc3 zk_id=3

Author Information
------------------

Tolkachev Kirill <tolk.kv@gmail.com>
