#!/bin/bash

mvn archetype:generate                      \
	-DarchetypeGroupId=com.netply           \
	-DarchetypeArtifactId=Zero-Archetype    \
	-DarchetypeVersion=1.0-SNAPSHOT         \
	-DgroupId=com.netply                    \
	-Dversion=1.0-SNAPSHOT                  \
	-DartifactId=Zero-Eventador-Mock               \
	-Dpackage=eventmock                       \
	-DclassPrefix=EventMock
