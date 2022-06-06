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
from sqlalchemy import Column, Integer, create_engine
from sqlalchemy.ext.declarative import as_declarative, declared_attr
from sqlalchemy.orm import sessionmaker

@as_declarative()
class DbRecord(object):
	@declared_attr
	def __tablename__(cls):
		return cls.__name__.lower()
	ID = Column(Integer(), primary_key=True)

	def __repr__(self):
		s = '{'
		for k, v in vars(self).items():
			if not k.startswith('_'):
				s += f'"{k}": "{v}", '
		if len(s) > 1:
			s = s[:-2]
		s += '}'
		return s

def dbSession(Table, dbName, user, password, host='localhost', port=5432):
	dbUri = f'postgres://{user}:{password}@{host}:{port}/{dbName}'
	db = create_engine(dbUri, case_sensitive=True)
	Session = sessionmaker(db)
	session = Session()
	Table.metadata.create_all(db)
	return session

def parseArgs(helpMsg, override = lambda p: None, **kw):
	from firmware.test.tools.lib import parseArgs
	def cliArgsOverrides(p):
		override(p)
		p.add('-D', '--db-name', default=kw['db_name'], help='Database name')
		p.add('-d', '--device-id', type=int, default=0, help='Device ID. Default is 0.')
		p.add('-H', '--host', default='nw.do', help='Database host name.')
		p.add('-P', '--password', default='db_background', help='DB user password.')
		p.add('-p', '--port', type=int, default=5432, help='Database host port.')
		p.add('-U', '--user', default='db_background', help='DB user.')
	return parseArgs(helpMsg, cliArgsOverrides)
