# Makefile for The jModbus Project 
# 
# The jModbus project is distrubuted under the following license terms
# 
# Copyright (c) 2001 by The Java Modbus Project
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without 
# modification, are permitted provided that the following conditions 
# are met:
# 
#  1.  Redistributions of source code must retain the above copyright 
#      notice, this list of conditions and the following disclaimer. 
#  2.  Redistributions in binary form must reproduce the above copyright 
#      notice, this list of conditions and the following disclaimer in 
#      the documentation and/or other materials provided with the 
#      distribution. 
#  3.  Neither the name of the The Java Modbus Project nor the names of 
#      its contributors may be used to endorse or promote products 
#      derived from this software without specific prior written 
#      permission. 
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
# OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


clean:
	mkdir -p bin
	mkdir -p doc/api
	rm -Rf bin/*.class
	rm -f jmodbus.jar
	rm -f jmodbus.tar.gz
	rm -Rf *~
	rm -Rf doc/api/*.html
	rm -Rf doc/api/*.css
	rm -Rf doc/api/package-list


modbus: 
	mkdir -p bin
	javac -d bin -sourcepath src src/net/sourceforge/jmodbus/*.java

jar: modbus 
	rm -f jmodbus.jar
	jar cf jmodbus.jar -C bin net

tar: modbus jar docs
	rm -f jmodbus.tar.gz
	tar -czf jmodbus.tar.gz -C .. jmodbus/LICENSE jmodbus/WARNING \
		jmodbus/Makefile jmodbus/README jmodbus/ReleaseNotes \
		jmodbus/bin/ jmodbus/dep.txt jmodbus/doc/ \
		jmodbus/examples/ jmodbus/jmodbus.jar jmodbus/src/

docs:
	mkdir -p doc/api
	rm -Rf doc/api/*.html
	rm -Rf doc/api/*.css
	rm -Rf doc/api/package-list
	javadoc -sourcepath src \
		-overview src/overview.html \
		-d doc/api/ \
		-use \
		-windowtitle 'jModbus API version 0.1.1' \
		-doctitle '<H1><CENTRE> The jModbus Project </CENTRE></H1>' \
		net.sourceforge.jmodbus


all: clean modbus jar docs tar













