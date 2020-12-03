<p align="center">
  <img src="https://raw.githubusercontent.com/rwth-acis/las2peer/master/img/logo/bitmap/las2peer-logo-128x128.png" />
</p>
<h1 align="center">Verification of learning analytics data</h1>

This repository contains services related to verification of learning analytics data, as well as privacy and consent management. 

Learning Analytics Verification Service
===========================================
The LA Verification Service is a las2peer Service which enables consent management and verification for LA data.


Configuration
-------------------
In order to use the full functionality of the service, it has to be bootstrapped to a las2peer network that has the following services deployed:
- [MoodleDataProxy](https://github.com/rwth-acis/moodle-data-proxy) that is connected to an instance of the Moodle Learning Management System (LMS)
- [LearningLockerService](https://github.com/rwth-acis/learning-locker-service) connected to a Learning Record Store (LRS)
- [MobSOSDataProcessingService](https://github.com/rwth-acis/mobsos-data-processing) to enable the transmission of xAPI-statements from Moodle to the LRS
- [SocialBotManagerService](https://github.com/rwth-acis/las2peer-Social-Bot-Manager-Service) is required to enable the communication with a chatbot

Build
--------

Execute the following command on your shell:

```shell
ant jar
```

Start
--------

To start the la-verification-service, follow the [Starting-A-las2peer-Network tutorial](https://github.com/rwth-acis/las2peer-Template-Project/wiki/Starting-A-las2peer-Network) and bootstrap your service to a [mobsos-data-processing service](https://github.com/rwth-acis/mobsos-data-processing/tree/bachelor-thesis-philipp-roytburg).

Initialization
-----------------------

Before the service can be properly used it needs to be initialized. 
During the initialization, the required smart contracts will be loaded, and messages for the bot communication, and pre-defined consentLevels will be read from property files.
To initialize the service, send a POST request to the following path:
```
POST <service-address>/verification/init
```

How to run using Docker
-------------------

First build the image:
```bash
docker build . -t la-verification-service
```

Then you can run the image like this:

```bash
docker run -p port:9011 la-verification-service
```


### Node Launcher Variables

Set [las2peer node launcher options](https://github.com/rwth-acis/las2peer-Template-Project/wiki/L2pNodeLauncher-Commands#at-start-up) with these variables.

| Variable | Default | Description |
|----------|---------|-------------|
| LAS2PEER_BOOTSTRAP | unset | Set the --bootstrap option to bootstrap with existing nodes. The container will wait for any bootstrap node to be available before continuing. |
| LAS2PEER_CONFIG_ENDPOINT | unset | Set variable to configure the endpoint from which to load blockchain configuration parameters. |
| LAS2PEER_ETH_HOST | unset | Set variable to configure which Ethereum host to use to access the Ethereum blockchain. |
