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
 EXAMPLES:
	$0 -d 1
'''

from nswpkg.cloud.device.interface.db.lib import DbRecord, dbSession
from sqlalchemy import Column, DATE, Integer, String

class AssetInfoRecord(DbRecord):
	__tablename__ = 'AssetInfo'
	ID = Column(Integer(), primary_key=True)
	Vendor = Column(String())
	Model = Column(String())
	ModelNumber = Column(Integer)
	SerialNumber = Column(String())
	IMEI = Column(Integer)
	Description = Column(String())
	DateAcquired = Column(DATE())
	DateNextMaintenance = Column(DATE())
	Comments = Column(String())
	adcBits = Column(Integer)
	chip = Column(String())
	clockDoppler = Column(Integer)
	debugPort = Column(Integer)
	hostname = Column(String())
	name = Column(String())
	playbackDevice = Column(String())
	playbackDevicePort = Column(Integer)
	port = Column(Integer)

def parseArgs(helpMsg, override = lambda p: None, **kw):
	from cloud.device.interface.db.lib import parseArgs
	def cliArgsOverrides(p):
		override(p)
		p.add('-o', '--output', default=None, help='Output file or standard output if omitted.')
	return parseArgs(helpMsg, cliArgsOverrides, **kw)

if __name__ == '__main__':
	op = parseArgs(__doc__, db_name='LabBoards')

	session = dbSession(AssetInfoRecord, op.db_name, op.user, op.password, op.host, op.port)

	# Read
	assetInfoRecords = session.query(AssetInfoRecord).filter(AssetInfoRecord.ID == op.device_id)
	if op.output is None:
		print(assetInfoRecords.all())
	else:
		with open(op.output, 'wb') as fd:
			fd.write(str(assetInfoRecords))
	session.close()
