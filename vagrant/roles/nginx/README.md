Nginx oem role
=========

Empty docker nginx with customizable config directory

Requirements
------------

* Docker 1.7+
* RHEL 7+

Role Variables
--------------

* nginx_docker_image_name - nginx image name 
* nginx_config_dir - nginx config dir for mount to docker container. Mount main config from nginx_config_dir/nginx.conf to /etc/nginx/nginx.conf
* nginx_sites_config_dir - mount to container nginx_sites_config_dir:nginx_sites_config_dir
* nginx_advanced_config_dir - mount to container nginx_advanced_config_dir:nginx_advanced_config_dir 
* nginx_location_config_dir - mount to container nginx_location_config_dir:nginx_location_config_dir
* nginx_upstream_config_dir - mount to container nginx_upstream_config_dir:nginx_upstream_config_dir
* nginx_log_dir - create log /var/log/nginx 
* nginx_www_dir - mount www dir to docker container nginx_www_dir:nginx_www_dir
* nginx_auth_pass - secure infrastructure endpoints by password

default values:

    nginx_docker_image_name: nginx
    nginx_config_dir: /etc/nginx
    nginx_sites_config_dir: /etc/nginx/sites-enabled
    nginx_advanced_config_dir: /etc/nginx/conf.d
    nginx_location_config_dir: /etc/nginx/location.d
    nginx_upstream_config_dir: /etc/nginx/upstream.d
    nginx_log_dir: /var/log/nginx
    nginx_www_dir: /var/www
    nginx_auth_pass: 
    
Use next config structure for extend nginx configuration (example):


    /etc/nginx/
    |-- conf.d                  # - this directory using for extend main configuration and contains nginx server { } block
    |   |-- chronos.conf
    |   |-- marathon.conf
    |   |-- rich_filtered.formatlog
    |   `-- mesos.conf
    |-- httppasswd              # - user:password file for restrict access (default dev:1231234)
    |-- location.d              # - directory contains nginx locations {} blocks, which include in main server { server_name: hostname }
    |   |-- chronos.location
    |   |-- marathon.location
    |   `-- mesos.location
    |-- nginx.conf
    `-- upstream.d              # - contained all upstream declaration 
        |-- chronos.upstream
        |-- marathon.upstream
        `-- mesos.upstream
    
Extending points:    

* Add *.location to `nginx_location_config_dir`
* Add *.upstream to `nginx_upstream_config_dir`
* Add *.conf to `nginx_config_dir`
* Add *.formatlog to `nginx_config_dir`

See main nginx configuration in templates/nginx.conf

        log_format rich '$remote_addr - $remote_user [$time_local] '
                    '"$request" $status $bytes_sent '
                    '"$http_referer" "$http_user_agent" '
                    '"$host" "$uri" "$args" "$sent_http_location" '
                    '"$proxy_host" "$upstream_addr" "$upstream_cache_status" '
                    '[$request_time] [$upstream_response_time]';
    
        include /etc/nginx/conf.d/*.formatlog;
        
        access_log  /var/log/nginx/access.log  rich;
    
        sendfile        on;
        #tcp_nopush     on;
    
        keepalive_timeout  65;
    
        #gzip  on;
    
        include {{nginx_upstream_config_dir}}/*.upstream;
        
        include {{nginx_advanced_config_dir}}/*.conf;
        include {{nginx_sites_config_dir}}/*;
    
        server {
            listen       80;
            server_name  {{ ansible_hostname }};
            add_header Access-Control-Allow-Origin *;
    
            location /health {
              return 200 "{}";
            }
    
            include {{nginx_location_config_dir}}/*.location;
        }
        
Dependencies
------------

* Docker service
* Docker image

Example Playbook
----------------

Including an example of how to use your role (for instance, with variables passed in as parameters) is always nice for users too:

    - hosts: all
      remote_user: dockeradm
      sudo: yes
      vars:
        nginx_docker_image_name: YOU_PRIVATE_REPO/nginx-oem
      roles:
       - role: nginx-oem


Author Information
------------------

Tolkachev Kirill <tolk.kv@gmail.com>
