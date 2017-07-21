TINIBIN=/usr/java/tini/bin
TINICLASSES=$(TINIBIN)/tiniclasses.jar
OWCLASSES=$(TINIBIN)/owapi_dependencies_TINI_001.jar
jMODBUS_JAR=../../../jmodbus.jar
jMODBUS_DEPS=../../../dep.txt

clean:
	rm -f build/*.tini
	rm -f build/*.class
	rm -Rf *~
	rm -f src/*.class

temp_mon: 
	javac -classpath $(TINICLASSES):$(OWCLASSES):$(jMODBUS_JAR):src src/TemperatureWorker.java
	javac -classpath $(TINICLASSES):$(OWCLASSES):$(jMODBUS_JAR):src src/TempMonitor.java
	rm -f build/*.tini
	rm -f build/*.class
	cp src/TemperatureWorker.class build
	cp src/TempMonitor.class build
	java -classpath $(TINIBIN)/tini.jar BuildDependency \
                -debug \
		-x "$(TINIBIN)/owapi_dep.txt;$(jMODBUS_DEPS)" \
		-p "$(OWCLASSES);$(jMODBUS_JAR)" \
		-f build \
		-o build/TempMonitor.tini \
		-d $(TINIBIN)/tini.db -add "jModbus_TCP_Slave;OneWireContainer10"


