#!/bin/bash

##########################################
# Waranty:

# THE PROGRAM IS DISTRIBUTED IN THE HOPE THAT IT WILL BE USEFUL, BUT WITHOUT ANY WARRANTY. 
# IT IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, 
# EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, 
# THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. 
# THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. 
# SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL 
# NECESSARY SERVICING, REPAIR OR CORRECTION.


# IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW THE AUTHOR WILL BE LIABLE TO YOU FOR DAMAGES, 
# INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY 
# TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED 
# INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), 
# EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

# This program is a bash example for automating gateway installtion
# The example is using Administrator username and password in order to authenticate (not key and secret key)
# (Administrator role must be set as APIConsumenr in order to use username and password)
# authentication results with a session token that can be used to make API calls 
#
# Prerequisites:
# JQ library (for parsing json output) in path
#    Download from https://stedolan.github.io/jq/
#    Make sure JQ variable is poiting to your JQ implementation
# Set the SSH_KEY_FILE variable to point to your own ssh pem file
#
# Usage:
# install_gateway.sh <api_username> <api_password> <target server IP>
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
      echo "Please provide an Administrator's username"
      return 1
   fi

   if [ -z "$USERNAME" ]
   then
      echo "Please provide an Administrator's password"
      return 1
   fi
   if [ -z "$TARGET_GWSERVER" ]
   then
      echo "Please provide IP Address of the target server on which you wish to install the gateway"
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
