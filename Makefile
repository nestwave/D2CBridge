override DIRS:=$(abspath $(dir $(lastword ${MAKEFILE_LIST}))) ${DIRS}
override DIR:=$(patsubst %/,%,$(firstword ${DIRS}))
override MODULE:=$(notdir ${DIR})

ifndef ROOT_DIR
ROOT_DIR:=$(patsubst %/,%,$(dir $(patsubst %/,%,$(dir ${DIR}))))
endif
all::${MODULE}_all
compile::${MODULE}_compile
clean::${MODULE}_clean

${MODULE}_PKG:=${DIR}/Dockerfile \
	${DIR}/start.sh \
	${DIR}/target/device-0.0.1-SNAPSHOT.jar \
	${DIR}/target/security\

DEVICE_DIR:=${DIR}

include ${DIR}/security/sensitive-config.ini

${MODULE}_all:${MODULE}_compile ${DIR}/target/security
${MODULE}_compile:${DIR}
	cd $< && mvn install -DskipTests

%.jwt:${DIR}/tools/jwtManage.py ${DIR}/nswpkg
	mkdir -p $(@D)
	$< -u '${NSW_USERNAME}' -p '${NSW_PASSWORD}' -o $@

${DIR}/nswpkg/%.py:${ROOT_DIR}/%.py
	mkdir -p $(dir $@)
	cp -rfpl $< $@

${DIR}/nswpkg/%:${ROOT_DIR}/%
	mkdir -p $(dir $@)
	cp -rfpl $< $@

${ROOT_DIR}/nswpkg:
	test -L $@ || ln -s . $@

%/cloud/nswpkg:%/nswpkg
	test -L $@ || ln -s ../nswpkg $@

%/cloud/device/nswpkg:%/cloud/nswpkg
	test -L $@ || ln -s ../nswpkg $@

${MODULE}_clean:${DIR}
	cd $< && mvn clean
	rm -rf $</target

${DIR}/target/security:$(dir ${DIR})security/keystore.p12 $(addprefix ${DIR}/security/,secret.jwt sensitive-config.ini)
	mkdir -p $@
	cp -t $@ $^

.PRECIOUS: %/nswpkg %/cloud/nswpkg %/cloud/device/nswpkg

override DIRS:=$(subst ${DIR},,${DIRS})
override DIR:=$(firstword $(DIRS))
override MODULE:=$(notdir ${DIR})
