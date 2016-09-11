Examples from JUG MSK 2016 - DevOps for rest of us
==================================================

No warranty guarantee

Requirements
------------

* vagrant 1.8.4 (1.8.5 have a public key insertion bug) or >=1.8.6 ( not
    released yet). `brew cask install vagrant`
* vagrant hostmanager plugin for resolve hosts and discovery between nodes.
    `vagrant plugin install vagrant-hostmanager`
* Ansible 2.0+.`brew install ansible`
* [Optional] Docker-machine 1.10. Only if you want to try minimesos
* [Optional] Mesos binaries in local machine if you run mesos-framework
    (project in mesos-framework directory) for add java.library.path. See
    `run.framework` script
* groovy for running script yaml-to-json.yml. `brew install groovy`

Run
===

* `vagrant up`
* go to [mm1 mesos admin ui](http://mm1/mesos)
* go to [mm1 marathon ui](http://mm1/marathon)
* go to [mm1 marathon ui](http://mm1/marathon)

Applications
------------

Use next shell scripts for deploy application to mesos cluster:

* `marathon.artifactory.up`
* `marathon.dummyhttpserver.up`
* `marathon.dummyhttpserver.update`
* `marathon.lb.up`
* `marathon.lb.update`
* `marathon.mesosdns.up`
* `marathon.nginx.up`
* `marathon.nginx.update`

_.up for init application
_.update for update existed manifest in marathon

`run.marathon.lb` for run haproxy locally



