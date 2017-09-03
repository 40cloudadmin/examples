#!/usr/bin/env python
__title__ = "40cloud_simple_token_auth.py"
__author__ = "Bentzy Sagiv"
__license__ = "GPL"
__version__ = "1.0.2"
__maintainer__ = "Bentzy Sagiv"
__email__ = "support@fortycloud.com"
__date__ = "09.03.17"

# Example for 40Cloud API - Simple Token authentication usage.
#
# Usage: 
# - Change the values for fc_api_key and fc_api_key by your own key and secret keys
#   as provided from 40Cloud management console
# - Run the script

import requests
import json
import datetime
import hmac
import hashlib
import base64

fc_api_key="<YOUR_FC_API_KEY>"
fc_api_secret="<YOUR_FC_API_SECRET>"
fc_api_entry_point="https://api.fortycloud.net"
fc_subnet_resource="/restapi/v0.4/subnets"

date_format="%a, %d %b %Y %H:%M:%S"
restverb="GET"
contenttype=''
body=''
signature=''

url=fc_api_entry_point+fc_subnet_resource


def calculate_signature(restverb, contenttype, date, url, body):

    stringtosign=restverb +"\n"+contenttype+"\n"+date+"\n"+url+"\n"+body+"\n"

    stringtosign_b = bytearray()
    stringtosign_b.extend(stringtosign)

    secret_b = bytearray()
    secret_b.extend(fc_api_secret)

    digest=hmac.new(secret_b, msg=stringtosign_b, digestmod=hashlib.sha256).digest()

    global signature
    signature=base64.b64encode(digest).decode()


date=(datetime.datetime.utcnow().strftime(date_format)+" GMT")
#print date

calculate_signature(restverb, contenttype, date, fc_subnet_resource, body)
#print signature

authval = "FCRestAPI AccessKey=" + fc_api_key + " SignatureType=HmacSHA256 Signature=" + signature

headers = {'Date': date, 'Authorization': authval }
#print headers

response = requests.get(url, headers=headers)

print "response: "
print response

print "response text: "
print(json.dumps(response.json(), indent=4, sort_keys=True))                                    
