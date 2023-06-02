#!/bin/bash

usage() { echo "Usage: $0 [-u <username>] [-k <key path>] [-s <server name>]" 1>&2; exit 1; }

while getopts ":u:k:s:" opt; do
  case $opt in
    u) username="$OPTARG"
    ;;
    k) keyPath="$OPTARG"
    ;;
    s) targetServer="$OPTARG"
    ;;
    *) usage
    ;;
  esac
done

shift $((OPTIND-1))

if [ -z "${username}" ] || [ -z "${keyPath}" ] || [ -z "${targetServer}" ]; then
    usage
fi

echo "Create app dirs on remote machine if needed"
botBasePath=/usr/gptslackbot
ssh -i $keyPath $username@$targetServer << EOF
if [ ! -d "$botBasePath" ]; then
	sudo mkdir $botBasePath
fi
if [ ! -d "$botBasePath"/logs ]; then
	sudo mkdir $botBasePath/logs
fi
if [ ! -d "$botBasePath"/ssl ]; then
	sudo mkdir $botBasePath/ssl
fi

sudo chown $username:$username -R $botBasePath
EOF

echo "Create context for remote machine if needed"
if [ -z "$(docker context ls | grep -o "$targetServerContext")" ]; then
  docker context create srv"$targetServer"Context --default-stack-orchestrator=swarm --docker host=ssh://$username@$targetServer --description "VDS connected context"
fi
echo "Build and deploy app to remote machine"
export DOCKER_CONTEXT=srv"$targetServer"Context
docker compose up -d --build
export DOCKER_CONTEXT=