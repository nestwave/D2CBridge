#!/usr/bin/env python3
# vim: set fileencoding=utf-8
###############################################################################
# Copyright 2022 - NESTWAVE SAS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to
# deal in the Software without restriction, including without limitation the
# rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
# sell copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
# IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
# DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
# OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
# USE OR OTHER DEALINGS IN THE SOFTWARE.
###############################################################################

'''
This script retrieves a JWT. from NestCloud of by signing in using either
a username and corresponding password or using an old but still valid JWT.

This script is meant to be run by D2CB make file but can be executed by user
as for example
	$0 -u nsw-cloud@nestwave.com -o secret.jwt
'''

from getpass import getpass
from nswpkg.tools.cli.lib import UtStatus, auto, exit
from requests import post

AUTH_URL = 'https://navigation.nextnav.io/authenticate'

class UtStatus(UtStatus):
	INVALID_CREDENTIALS = auto()
	INVALID_JWT = auto()
	UNEXPECTED_REMOTE_ERROR = auto()

def jwtGet(username, password, trust=False):
	params = {
		'username': username,
		'password': password,
	}
	response = post(AUTH_URL, json=params, verify=not trust)
	if response.status_code == 200:
		return response.json()['token']
	elif response.status_code == 401:
		return UtStatus.INVALID_CREDENTIALS
	else:
		return response.status_code

def jwtRenew(jwt, trust=False):
	headers = {
		'Authorization': 'Bearer ' +jwt,
	}
	response = post(AUTH_URL + '/renew', headers=headers, verify=not trust);
	if response.status_code == 200:
		return response.json()['token']
	elif response.status_code == 401:
		return UtStatus.INVALID_JWT
	else:
		return response.status_code

def parseArgs(helpMsg, override = lambda p: None):
	from nswpkg.tools.cli.lib import parseArgs
	def cliArgsOverrides(p):
		override(p)
		p.add('-u', '--username', default='', help='Nestwave account user name (email).')
		p.add('-p', '--password', default='', help='Nestwave account user password (not recommended! May be security hole).')
		p.add('-j', '--jwt', default='', help='Old JWT (should be valid).')
		p.add('-o', '--output', default='', help='Output file. Standard output if empty.')
		p.add('-H', '--hostname', default='', help='Target host name (*.nw.do).')
		p.add('-T', '--trust-ssl-certificate', action='store_true', help='Trust SSL certificate.')
	return parseArgs(helpMsg, cliArgsOverrides)

if __name__ == '__main__':
	op = parseArgs(__doc__)
	if op.trust_ssl_certificate:
		from urllib3.exceptions import InsecureRequestWarning
		from requests.packages.urllib3 import disable_warnings
		# Suppress only the single warning from urllib3 needed.
		disable_warnings(category=InsecureRequestWarning)
	if op.hostname:
		AUTH_URL = f'https://{op.hostname}/authenticate'
	if op.jwt:
		token = jwtRenew(op.jwt, op.trust_ssl_certificate)
	else:
		username = op.username if op.username else input('User name: ')
		password = op.password if op.password else getpass('Password: ')
		token = jwtGet(username, password, op.trust_ssl_certificate)
	if type(token) is UtStatus:
		exit(token)
	elif type(token) is int:
		print(f"Unexpected remote error: {token}")
		exit(UtStatus.UNEXPECTED_REMOTE_ERROR)
	if op.output:
		with open(op.output, 'w') as fd:
			fd.write(token)
	else:
		print(token)
