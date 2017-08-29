#!/usr/bin/env python
from __future__ import print_function
import requests
import json

__title__ = "40cloud_usr_passw_auth.py"
__author__ = "Bentzy Sagiv"
__license__ = "GPL"
__version__ = "1.0.1"
__maintainer__ = "Bentzy Sagiv"
__email__ = "support@fortycloud.com"
__date__ = "08.29.17"

# Example for 40Cloud API - User/Password authentication.
#
# Usage: 
# - Change the values for fc_api_username and fc_api_password by your own values
# - Run the script

fc_api_username="<YOUR_FC_API_USERNAME> "
fc_api_password="<YOUR_FC_API_SECRET>"
fc_api_entry_point="https://api.fortycloud.net"
fc_domain="restapi"
fc_restapiversion="v0.4"
fc_resourcetoken_auth="tokens"
fc_resourcetoken_enroll = "enrollment-scripts"
fc_local_filename="installationscript.sh"

fc_url_auth=fc_api_entry_point + "/" + fc_domain + "/" + fc_restapiversion + "/" + fc_resourcetoken_auth

fc_url_download=fc_api_entry_point + "/" + fc_domain + "/" + fc_restapiversion + "/" + fc_resourcetoken_enroll


#1. get sessionid
credentials={"username":fc_api_username,"password":fc_api_password}
payload = {"auth": {"passwordCredentials":credentials, "tenantName": "nomatter"}}
headers = {'content-type' : 'application/json'}

response = requests.post(fc_url_auth,data=json.dumps(payload),headers=headers)
data = response.json()
sessionid = data['access']['token']['id']


#2. get installation script using sessionid
payload2 = {"installationPlatform":"SoftwareInstallation"}
headers2 = {'content-type' : 'application/json', 'X-Auth-Token' : sessionid}

response2 = requests.post(fc_url_download,data=json.dumps(payload2),headers=headers2, timeout=30)
data2 = response2.json()
scriptcontent = data2['enrollmentScript']

#3. create installation script file
scriptfile=open(fc_local_filename  , "w")        
print (scriptcontent, file = scriptfile)



