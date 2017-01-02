#!/bin/bash

##########################################
# Bash example for automating gateway installtion
# The example is based on an authentication based on username and password
# 
# Prerequisit:
# JQ library (for parsing json output) in path
# Download from https://stedolan.github.io/jq/
#
# usage:
# install_gateway.sh <api_username> <api_password> <target server IP>
# note that SSH_KEY_FILE must point to your SSH pem file
#########################################

PLATFORM="SoftwareInstallation"
LICENSE="LargeGW"
API_ENDPOINT="api.fortycloud.net"
RESOURCE_SESSION="restapi/v0.4/tokens"
RESOURCE_INSTALL_SCRIPT="restapi/v0.4/installation-scripts"

USERNAME=""
PASSWORD=""
TOKEN=""
TARGET_GWSERVER=""
SSH_KEY_FILE=
#JQ=./jq-osx-amd64
JQ=./jq-linux64
#JQ=./jq-win64.exe

validate_jq(){
   which $JQ
   if [ $? != 0 ]
   then
      echo "jq (bash json parser) is not available in current path"
      echo "please download jq from https://stedolan.github.io/jq/ and make it available in your path" 
      echo "make sure the JQ variable is pointing to the jq executable"
      return 1
   fi
}
validate_input(){
   if [ -z "$USERNAME" ]
   then
      echo "please provide API username"
      return 1
   fi

   if [ -z "$USERNAME" ]
   then
      echo "please provide API password"
      return 1
   fi
   if [ -z "$TARGET_GWSERVER" ]
   then
      echo "please provide IP Address of the target server"
      return 1
   fi

}
usage(){
   echo "script usage: $0 <api_username> <api_password> <target server IP>" 
}
main(){
    USERNAME=$1
    PASSWORD=$2   
    TARGET_GWSERVER=$3
    validate_input 
    if [ $? != 0 ]
    then
       usage
       exit 1
    fi
    validate_jq
    if [ $? != 0 ]
    then
       exit 1
    fi

    JSONDATA="{\"auth\": {\"passwordCredentials\": {\"username\": \"$USERNAME\",\"password\": \"$PASSWORD\"},\"tenantName\": \"MyAccount\"}}"
    SESSION_RESPONSE=$(curl -s -X POST -d "$JSONDATA" https://$API_ENDPOINT/$RESOURCE_SESSION)
    TOKEN=$(echo $SESSION_RESPONSE | $JQ '.access.token.id')
    TOKEN=$(echo $TOKEN | tr -d '"')
    echo "API SESSION TOKEN: "$TOKEN
    JSON_INSTALL_REQ="{\"installationPlatform\":\"$PLATFORM\",\"licenseName\":\"$LICENSE\"}"
    INSSCRIPT_RESPONSE=$(curl -s -H "X-Auth-Token:$TOKEN" -X POST -d "$JSON_INSTALL_REQ" https://$API_ENDPOINT/$RESOURCE_INSTALL_SCRIPT)
    INSTALL_SCRIPT=$(echo $INSSCRIPT_RESPONSE | $JQ '.installationScript')
    INSTALL_SCRIPT=$(echo $INSTALL_SCRIPT | tr -d '"')
    ssh -i $SSH_KEY_FILE ubuntu@$TARGET_GWSERVER -T $INSTALL_SCRIPT
}
main $@
