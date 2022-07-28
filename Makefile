override DIRS:=$(abspath $(dir $(lastword ${MAKEFILE_LIST}))) ${DIRS}
override DIR:=$(patsubst %/,%,$(firstword ${DIRS}))
override MODULE:=$(notdir ${DIR})

all::compile
clean::device_clean
compile::device_all

ifndef ROOT_DIR
ROOT_DIR:=$(patsubst %/,%,$(dir $(patsubst %/,%,$(dir ${DIR}))))
endif
DEVICE_DIR:=${DIR}

include ${DIR}/security/sensitive-config.ini

device_all:${DIR}

${DIR}:$(addprefix ${DIR}/target/security/,secret.jwt sensitive-config.ini)
	cd $@ && mvn install -DskipTests
	mkdir -p $@/target/security
	cp $(dir $@)security/keystore.p12 $@/target/security/keystore.p12

${DIR}/target/security/%:${DIR}/security/%
	cp -rfpl $< $@

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

device_clean:
	cd ${DEVICE_DIR} && mvn clean
	rm -rf ${DEVICE_DIR}/target

.PRECIOUS: %/nswpkg %/cloud/nswpkg %/cloud/device/nswpkg

override DIRS:=$(subst ${DIR},,${DIRS})
override DIR:=$(firstword $(DIRS))
override MODULE:=$(notdir ${DIR})
