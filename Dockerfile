# NODE_MODULES AND IVY BUILD CACHE
FROM openjdk:8-jdk-alpine AS buildcache
RUN apk add --no-cache bash nodejs npm git python build-base htop curl sed apache-ant tar wget vim && npm i -g pm2 http-server
# https://github.com/mhart/alpine-node/issues/48#issuecomment-370171836
RUN addgroup -g 1000 -S build && adduser -u 1000 -S build -G build
RUN mkdir /app-cache && chown build:build /app-cache
USER build
WORKDIR /app-cache
RUN git clone https://github.com/rwth-acis/las2peer-registry-contracts.git -b master
WORKDIR /app-cache/las2peer-registry-contracts
RUN npm install
WORKDIR /app-cache
# RUN git clone https://github.com/rwth-acis/las2peer/ -b ba-lennart-bengtson
RUN git clone https://github.com/rwth-acis/las2peer/
WORKDIR /app-cache/las2peer
RUN git checkout tags/v1.0.1
RUN ant build-only

FROM openjdk:8-jdk-alpine
ENV LAS2PEER_PORT=9000
ARG LAS2PEER_BOOTSTRAP=""
ENV LAS2PEER_BOOTSTRAP="${LAS2PEER_BOOTSTRAP}"

RUN apk add --no-cache bash nodejs npm git python build-base htop curl sed apache-ant tar wget vim && npm i -g pm2 http-server

RUN addgroup -g 1000 -S build && adduser -u 1000 -S build -G build
RUN mkdir -p /app && chown build:build /app
RUN mkdir -p /app/las2peer/node-storage && chown build:build /app/las2peer/node-storage
USER build

WORKDIR /app
RUN git clone https://github.com/ettore26/wait-for-command

COPY --chown=build:build . /app
COPY --from=buildcache --chown=build:build /app-cache/las2peer/core/lib /app/lib
COPY --from=buildcache --chown=build:build /app-cache/las2peer/bundle/export/jars /app/lib

# cache bust, see https://stackoverflow.com/a/39278224
ADD https://api.github.com/repos/rwth-acis/las2peer-registry-contracts/git/refs/heads/master version.json
RUN git clone https://github.com/rwth-acis/las2peer-registry-contracts.git -b master
WORKDIR /app/las2peer-registry-contracts
COPY --from=buildcache --chown=build:build /app-cache/las2peer-registry-contracts/node_modules /app/las2peer-registry-contracts/node_modules
RUN npm install

WORKDIR /app
RUN ant jar

WORKDIR /app/la-registry-contracts
COPY --from=buildcache --chown=build:build /app-cache/las2peer-registry-contracts/node_modules /app/la-registry-contracts/node_modules
RUN npm install

WORKDIR /app
RUN chmod +x /app/start.sh

ENTRYPOINT /app/start.sh