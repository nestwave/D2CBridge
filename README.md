# Nestwave Device to Cloud Bridge Documentation

This repository contains source code files for Nestwave Device to Cloud Bridge (DCB).

![image](https://user-images.githubusercontent.com/84769396/172661074-39a031bb-c92b-4dab-8d7c-f741423173ed.png)

Customer protocol is COAP or HTTP in the example code provided. 

## Directory structure
Nestwave DCB source code should be located in a folder named `device`.
Sensitive data are located in `security` folder.
```
+-----------+
|${NSW_ROOT}|
+-----------+
    |
    |    +-----+
    +----|cloud|
    |    +-----+
    |       |
    |       |    +------+
    |       +----|device| (DCB root)
    |       |    +------+
    |       |        |    +--------+
    |       |        +----|security|
    |       |        |    +--------+
    |       |        |         |
    |       |        |         +----:README.md
    |       |        |         |
    |       |        |         +----:secret.jwt
    |       |        |         |
    |       |        |         +----:sensitive-config.ini
    |       |        |
    |       |        |    +---+
    |       |        +----|src|
    |       |        |    +---+
    |       |        |      |
    |       |        |      |    +----+
    |       |        |      +----|main|
    |       |        |           +----+
    |       |        |
    |       |        +----:Makefile
    |       |
    |       |    +--------+
    |       +----|security| (TLS certificates location)
    |       |    +--------+
    |       |        |
    |       |        +----:keystore.p12
    |       |
    |       +----:docker-compose-bridge.yml
    |
    |    +-----+
    +----|tools| (DCB dependency)
         +-----+
```

**NB:** Files in `security` folder other than `README.md` shall never be committed to GIT repository.

## System requirements

In order to work with Nestwave Device to Cloud Bridge (DCB), a Debian 11 version is recommended.
Other versions may work but are not supported.
The following required packages should be installed :
```
apt install git make mvn python3-configargparse docker-compose
```

The user should be added to the `docker` group.
```
adduser <user> docker
```
Until user logouts and logins again, an explicit switch group will be required.
```
sg docker
```

## Fetching sources
One needs to execute a few steps to get a working local copy.
### Create destination directory
We will install the D2CB source in `/opt/Nestwave/dev/D2CBridge` but one can use any location.

```
mkdir -p /opt/Nestwave/dev/D2CBridge/cloud
```

### Clone tools
The DC2B code requires and extra repository for tools. This repository is provided by Nestwave as a compressed tarball. Please visit [this page](https://github.com/nestwave/D2CBridge/releases) and download it from the asset section of the latest tools version. Then perform the following command.
```
TOOLS_VERSION=v1.6-8
# You already downloaded tools-${TOOLS_VERSION}.tar.xz using your browser.
tar -C /opt/Nestwave/dev/D2CBridge -vaxf ~/Downloads/tools-${TOOLS_VERSION}.tar.xz
```

#### Clone D2CB sources
Just execute the following command and you are ready to build your device to Nestwave cloud bridge.
```
git -C /opt/Nestwave/dev/D2CBridge/cloud clone git@github.com:nestwave/D2CBridge device
```

## Compilation
You will need to create a file `device/security/sensitive-config.ini` with required information.
Please read [security/README.md](security/README.md) for more information about how to handle sensitive data.

The file `security/secret.jwt` is created automatically during the build process.

TLS certificate will need to be exported into p12 format and stored in a file named `keystore.p12` located on the `cloud` folder (at the same level of `device` folder).
If using Let's Encrypt project, it can be generated using the following script:
```
#! /usr/bin/env sh

set -xe

cd `dirname $0`

HOSTNAME=${1:-`hostname -f`}
SERVICES="authentication navigation"
# Certificate remote path below is Nestwave one.
# Customers should change it to thier specific path.
# If using LetsEncrypt, just replace nw.do by your domain name.
CERT_REMOTE_PATH=/etc/letsencrypt/live/nw.do
CERT_LOCAL_PATH=${PWD}/security
# Get keystore pasword from sensitive-config.ini.
KEY_STORE_PASSWORD=`sed -ne 's/KEY_STORE_PASSWORD=\(.*\)$/\1/p' security/sensitive-config.ini`

test -d ${CERT_LOCAL_PATH} ||  mkdir ${CERT_LOCAL_PATH}
chmod 700 ${CERT_LOCAL_PATH}

scp root@${HOSTNAME}:${CERT_REMOTE_PATH}/*.pem ${CERT_LOCAL_PATH}

# Run openssl to create the key store.
# You may get the error: "sh: 2: KEY_STORE_PASSWORD: parameter not set or null"
# This means that sed expression above failed ot get KEY_STORE_PASSWORD value.
# Then, hard code it above by copying it from security/sensitive-config.ini.
openssl pkcs12 -export \
	-in ${CERT_LOCAL_PATH}/fullchain.pem \
	-inkey ${CERT_LOCAL_PATH}/privkey.pem \
	-out ${CERT_LOCAL_PATH}/keystore.p12 -name tomcat -CAfile chain.pem \
	-caname root -passout "pass:${KEY_STORE_PASSWORD:?}"
```

For compilation, just use `make` or `make clean all`.
If no NestCloud user login and password were set in `security/sensitive-config.ini`, then the process will prompt for your Nestwave username and password. If you don't have one, please go to https://cloud.nestwave.com/ to create your account.

In order to make the process fully automatic one can use `make NSW_USERNAME='username@company.com' NSW_PASSWORD='password'`.

## Execution

### Docker compose file
You need to create a file `docker-compose-bridge.yml` located on the `cloud` folder (at the same level of `device` folder) and which content is:
```yaml
version: '2'
services:
  tracking:
    container_name: tracking
    image: postgres:11
    restart: always
    networks:
      - backend
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_USER=yourPreferredUserName
      - POSTGRES_PASSWORD=yourPreferredPassword
      - POSTGRES_DB=tracking
    volumes:
      - tracking_dbdata:/var/lib/postgresql/data

  device:
    container_name: device
    image: device
    build: device
    ports:
      - "8087:8087"
      - "8088:8088"
      - "5683:5683/udp"
      - "5684:5684"
    networks:
      - backend
    depends_on:
      - tracking
    dns_search:
      - nw.do

networks:
  backend:

volumes:
  tracking_dbdata:
```

Then you need to run the following command
```sh
docker-compose -f docker-compose-bridge.yml up -d --build
```

## Technical documentation
Please ensure you have read [NestCloud Bridge Specification V1.03](doc/NestCloud%20Bridge%20Specification%20v1.03.pdf) before you start working on D2CB.

### Positions tracking database
The D2CB allows storing device positions in a DB in order provide them to a web frontend (an Apache based example is provided [here](src/main/html)).

The  DB that is located on a separate server. In this case the code should be reworked to use asynchronous operations.

#### Tables layout
The positions tracking DB is named `positions` and holds a set of columns that are defined [here](src/main/java/com/nestwave/device/repository/position/PositionRecord.java).

The `key.id` corresponds to device unique identifier that is referenced in this document by _device ID_. The device ID allows identifying different devices, but needs a provisioning step upon production.

#### Database credentials
In order to connect to the tracking position DB, credentials should be provided via the variables `JDBC_USERNAME` and `JDBC_PASSWORD`.

Please read [security/README.md](security/README.md) for more information about how to handle sensitive data.

### Device ID
D2CB uses a data packet that holds a device unique identifier and a Fletcher 32 integrity check word.

The device identifier is the first 32 bits of the received data packet.
The device ID is handled as a variable length prefix and an enumeration part. This is one exemple of how to manage the device IDs.
![Message structure and identifier](https://user-images.githubusercontent.com/84769396/173068497-05844000-8379-4272-be1e-62f38e0a8cca.png)


### Adding a plugin
An example of plugins exists in [`src/main/java/com/traxmate`](src/main/java/com/traxmate).

In order to add a new one, just copy the folder to another name and modify the code as needed.
