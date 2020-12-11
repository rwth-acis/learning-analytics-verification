#!/bin/bash
# note: not sh or zsh compatible
set -e
#set -o verbose # echo all commands before execution

# when migrating (deploying smart contracts, done by boot node),
# after eth client is seemingly ready, wait for this many extra seconds
# (because e.g. account unlocking takes time)
EXTRA_ETH_WAIT=${EXTRA_ETH_WAIT:-30}

# max wait for bootstrapping node to perform migration and share its config
# with trivial block time, this takes just a few seconds; with higher block
# times, this can be e.g. 15 minutes (but possibly much more, depending on
# difficulty)
# ... really, at this point we might as well wait forever (until the user
# kills us), but let's go for a solid six hours
CONFIG_ENDPOINT_WAIT=${CONFIG_ENDPOINT_WAIT:-21600}

NODE_ID_SEED=${NODE_ID_SEED:-$RANDOM}

ETH_PROPS_DIR=/app/etc/
ETH_PROPS=i5.las2peer.registry.data.RegistryConfiguration.properties

LA_ETH_PROPS_FILE='/app/etc/i5.las2peer.services.learningAnalyticsVerification.LaRegistryConfiguration.properties'

function waitForEndpoint {
    /app/wait-for-command/wait-for-command.sh -c "nc -z ${1} ${2:-80}" --time ${3:-10} --quiet
}

function host { echo ${1%%:*}; }
function port { echo ${1#*:}; }
 
 function truffleMigrateLa { 
    echo Starting truffle migration of LA contracts...
    cd /app/la-registry-contracts
    ./node_modules/.bin/truffle migrate --network docker_boot 2>&1 | tee migration-la.log
    echo done. Setting contract addresses in config file ...
    # yeah, this isn't fun:
    cat migration-la.log | grep -A5 "\(Deploying\|Replacing\|contract address\) \'\(ConsentRegistry\|VerificationRegistry\)\'" | grep '\(Deploying\|Replacing\|contract address\)' | tr -d " '>:" | sed -e '$!N;s/\n//;s/Deploying//;s/Replacing//;s/contractaddress/Address = /;s/./\l&/' >> "${LA_ETH_PROPS_FILE}"
    cp migration-la.log /app/las2peer/node-storage/migration-la.log
    echo done. 
 }

if [ -n "$LAS2PEER_ETH_HOST" ]; then
    echo Replacing Ethereum client host in config files ...
    ETH_HOST_SUB=$(host $LAS2PEER_ETH_HOST)
	sed -i "s/eth-bootstrap/${ETH_HOST_SUB}/" /app/la-registry-contracts/truffle.js
    echo done.
fi

export SERVICE_PROPERTY_FILE='/app/etc/i5.las2peer.services.learningAnalyticsVerification.LearningAnalyticsVerificationService.properties'

if [ -n "$LAS2PEER_ETH_HOST" ]; then
	echo Waiting for Ethereum client at $(host $LAS2PEER_ETH_HOST):$(port $LAS2PEER_ETH_HOST)...
	if waitForEndpoint $(host $LAS2PEER_ETH_HOST) $(port $LAS2PEER_ETH_HOST) 100; then
		echo Found Eth client. 
		if [ -s "/app/las2peer/node-storage/migration-la.log" ]; then
			cat /app/las2peer/node-storage/migration-la.log | grep -A5 "\(Deploying\|Replacing\|contract address\) \'\(ConsentRegistry\|VerificationRegistry\)\'" | grep '\(Deploying\|Replacing\|contract address\)' | tr -d " '>:" | sed -e '$!N;s/\n//;s/Deploying//;s/Replacing//;s/contractaddress/Address = /;s/./\l&/' >> "${LA_ETH_PROPS_FILE}"
			echo Migrated from logs.
		else
			truffleMigrateLa
		fi
	else
		echo Ethereum client not accessible. Aborting.
		exit 2
	fi
fi

cd /app
if [ -n "$LAS2PEER_BOOTSTRAP" ]; then
    if waitForEndpoint $(host ${LAS2PEER_BOOTSTRAP}) $(port ${LAS2PEER_BOOTSTRAP}) 600; then
        echo Las2peer bootstrap available, continuing.
    else
        echo Las2peer bootstrap specified but not accessible. Aborting.
        exit 3
    fi
fi

# it's realistic for different nodes to use different accounts (i.e., to have
# different node operators). this function echos the N-th mnemonic if the
# variable WALLET is set to N. If not, first mnemonic is used
function selectMnemonic {
    declare -a mnemonics=("differ employ cook sport clinic wedding melody column pave stuff oak price" "memory wrist half aunt shrug elbow upper anxiety maximum valve finish stay" "alert sword real code safe divorce firm detect donate cupboard forward other" "pair stem change april else stage resource accident will divert voyage lawn" "lamp elbow happy never cake very weird mix episode either chimney episode" "cool pioneer toe kiwi decline receive stamp write boy border check retire" "obvious lady prize shrimp taste position abstract promote market wink silver proof" "tired office manage bird scheme gorilla siren food abandon mansion field caution" "resemble cattle regret priority hen six century hungry rice grape patch family" "access crazy can job volume utility dial position shaft stadium soccer seven")
    if [[ ${WALLET} =~ ^[0-9]+$ && ${WALLET} -lt ${#mnemonics[@]} ]]; then
    # get N-th mnemonic
        echo "${mnemonics[${WALLET}]}"
    else
        # note: zsh and others use 1-based indexing. this requires bash
        echo "${mnemonics[0]}"
    fi
}

#prepare pastry properties
echo external_address = $(curl -s https://ipinfo.io/ip):${LAS2PEER_PORT} > etc/pastry.properties

export SERVICE_VERSION=$(awk -F "=" '/service.version/ {print $2}' etc/ant_configuration/service.properties)
export SERVICE_NAME=$(awk -F "=" '/service.name/ {print $2}' etc/ant_configuration/service.properties)
export SERVICE_CLASS=$(awk -F "=" '/service.class/ {print $2}' etc/ant_configuration/service.properties)
export SERVICE=${SERVICE_NAME}.${SERVICE_CLASS}@${SERVICE_VERSION}

[[ -z "${SERVICE_PASSPHRASE}" ]] && export SERVICE_PASSPHRASE='newPass'

# configure service properties
function set_in_service_config {
    sed -i "s?${1}[[:blank:]]*=.*?${1}=${2}?g" ${SERVICE_PROPERTY_FILE}
}

echo Starting las2peer node ...
if [ -n "$LAS2PEER_ETH_HOST" ]; then
    echo ... using ethereum boot procedure: 
    java $(echo $ADDITIONAL_JAVA_ARGS) \
        -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher \
        --service-directory service \
        --port $LAS2PEER_PORT \
        $([ -n "$LAS2PEER_BOOTSTRAP" ] && echo "--bootstrap $LAS2PEER_BOOTSTRAP") \
        --node-id-seed $NODE_ID_SEED \
        --ethereum-mnemonic "$(selectMnemonic)" \
        $(echo $ADDITIONAL_LAUNCHER_ARGS) \
        uploadStartupDirectory \
		startService\("'""${SERVICE}""'", "'""${SERVICE_PASSPHRASE}""'"\) \
		startWebConnector \
        "node=getNodeAsEthereumNode()" "registry=node.getRegistryClient()" "n=getNodeAsEthereumNode()" "r=n.getRegistryClient()"
else
    echo ... using non-ethereum boot procedure:
    java $(echo $ADDITIONAL_JAVA_ARGS) \
        -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher \
        --service-directory service \
        --port $LAS2PEER_PORT \
        $([ -n "$LAS2PEER_BOOTSTRAP" ] && echo "--bootstrap $LAS2PEER_BOOTSTRAP") \
        --node-id-seed $NODE_ID_SEED \
        $(echo $ADDITIONAL_LAUNCHER_ARGS) \
        startWebConnector
fi
