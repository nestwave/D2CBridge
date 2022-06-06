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
This script performs the following action.

This script can be executed as follows, for examples:
  1. to perform first action:
    $0 [options list]
  2. to performw second action
    $0 [othe options list]

Copyright 2014-2022 Nestwave SAS.
'''

classMask = 0x888080C0;
deviceIdShift = [32, 28, 24, 16, 8, 7, 6, 0]
deviceIdMasks = [0xF0000000, 0x0F000000, 0x00FF0000, 0x0000FF00, 0x00000080, 0x00000040, 0x0000003F]
N = len(deviceIdMasks)

def customerId(deviceId: int):
	classIndicator = deviceId & classMask
	for n in range(N):
		if (classIndicator & deviceIdMasks[n]) == 0:
			return deviceId >> deviceIdShift[n + 1]
	return 0

def deviceId(*devId: str):
	deviceId = 0
	if len(devId) == 1:
		devId = devId[0].split('.')
	if 2 <= len(devId) <= N:
		for n in range(len(devId) - 1):
			deviceId += int(devId[n]) << deviceIdShift[n + 1]
		deviceId += int(devId[-1])
	else:
		print("Invalid device ID:", devId)
	return deviceId;

def parseArgs(helpMsg, override = lambda p: None):
	from nswpkg.tools.cli.lib import parseArgs
	def cliArgsOverrides(p):
		p.add('-d', '--device-id', nargs='+', help='Print device ID as an integer.')
		p.add('-c', '--customer-id', nargs='?', type=int, default=0, help='Extract customer ID from device ID.')
		override(p)
	return parseArgs(helpMsg, cliArgsOverrides)

if __name__ == '__main__':
	op = parseArgs(__doc__)
	if op.device_id:
		dId = deviceId(*op.device_id)
		print(hex(dId), '=', dId)
	if op.customer_id is None:
		if op.device_id:
			cId = customerId(dId)
			print(hex(cId), '=', cId)
		else:
			print('Error: Expected device ID to be supplied')
	elif op.customer_id > 0:
		cId = customerId(op.customer_id)
		print(hex(cId), '=', cId)
