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
This script builds a message for cloud assistanc eand navigation bridge.

This script can be executed as follows, for examples:
  1. to build realtime assistance request message:
    $0 -d 1 -o /tmp/assistParams-v1.6.bin -b 0b1110
  2. to build realtime assistance request message:
    $0 -d 1 -o /tmp/assistParams-v1.6.bin -b 0b1110 -t now-5d
    $0  -d 1 -o /tmp/assistParams-v1.6.bin-b 0b1110 -t now-2w
  3. to build realtime assistance request message:
    $0 -d 1 -o /tmp/assistParams-v1.6.bin -B -p AAAAAA4AAAA=
  4. to build navigation request messages:
    $0 -d 1 -o /tmp/assistParams-v1.6.bin -p /tmp/rawResults.bin
    $0 -d 1 -o /tmp/gnssPosition.bin -B -p \\
uZ09SB4AAMD9dQIACgAAAACLbE9IQAAAABDQRANAAAAAAACAVEAEAQAAAAAAAAfwlyL/TcJB+9CX\\
8xtRvVYdBhcO3KZFTBCkl+7HDn5apAeWB1M/h2q2MRXpZzIHgGezFfNpg8afUiWSyTlJSJ1/xbPL\\
UZ7GDmKWLyEosfoEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAMnonQRaAQYAE/+j+tsA+gDolE8L1gAeBhhRH/wHARAEE22A\\
DtwApQfzgWYOiwC1AWebCA0aAWcDdymUEDEAUgVOaj4NdAB/BUb/Y/UVAGMGAAAAAAAAAAAAAAAA\\
AAAAAAAAAgAAAAEAAAABAAAAAQAAAAEAAAABAAAAAQAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHIa7C3oMVEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGp4dSYhhMQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAE8bQ0/g6klAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAANftTREukSEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAMGMC6km9CQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAEFumGIVaDtAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAf+4IqF4uNUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACk\\
Cy2pvyozQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIV4ik1p\\
AS9AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN0b9FDQOKUAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
'''

from base64 import b64decode
from nswpkg.tools.cli.lib import UtStatus, exit
from nswpkg.tools.time.gps import GpsTime
from numpy import array, frombuffer

def fletcher32(data):
	c0 = 0
	c1 = 0
	# Ensure data size if multiple of uint16 or pad with null character.
	if len(data) % 2:
		data += b'\0'
	# Convert to a buffer of 16 bits
	data = frombuffer(data, 'uint16')
	wQty = len(data)
	x = 0
	while wQty > 0:
		# Use packets of max 360 words
		for i in range(0, min(wQty, 360)):
			c0 += data[x]
			c1 += c0
			x += 1
		# Reduce the results
		c0 %= 65535
		c1 %= 65535
		# Consume reduced packet
		wQty -= 360
	# Return the reduced results
	return int(c1 << 16 | c0)

def buildMessage(deviceId, payload):
	# First prepend the device ID
	message = deviceId.to_bytes(4, 'little') + payload;
	# Then append the fletcher32 computed on the ID + payload
	message += fletcher32(message).to_bytes(4, 'little')
	# Finally return the new message
	return message

def parseArgs(helpMsg, override = lambda p: None):
	from nswpkg.tools.cli.lib import parseArgs
	def cliArgsOverrides(p):
		p.add('-B', '--base64', action='store_true', help='Assume passed payload is encoded in base64 and decode it before building message.')
		p.add('-d', '--device-id', type=int, default=0, help='Device ID. Default is 0.')
		p.add('-t', '--time', default='now', help='Specify the time as argument.')
		p.add('-b', '--bitmap', default=None, help='Bitmap value. Default is no btimap word prepended.')
		p.add('-p', '--payload', default='', help='Payload to be sent (if -B) or file path.')
		p.add('-o', '--output', default=None, help='Output file or standard output if omitted.')
		override(p)
	return parseArgs(helpMsg, cliArgsOverrides)

if __name__ == '__main__':
	op = parseArgs(__doc__)
	if op.payload:
		if op.base64:
			payload = b64decode(op.payload)
		else:
			with open(op.payload, 'rb') as fd:
				payload = fd.read()
	elif op.bitmap:
		gpsTime = int(GpsTime(op.time))
		bitmap = eval(op.bitmap)
		payload = array([gpsTime, bitmap], dtype='int32').tobytes()
	else:
		exit(UtStatus.INVALID_CLI_INVOCATION)
	message = buildMessage(op.device_id, payload)
	if op.output is None:
		print(message)
	else:
		with open(op.output, 'wb') as fd:
			fd.write(message)
