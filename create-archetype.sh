#!/bin/bash

mvn archetype:generate                      \
	-DarchetypeGroupId=com.netply           \
	-DarchetypeArtifactId=Zero-Archetype    \
	-DarchetypeVersion=1.0-SNAPSHOT         \
	-DgroupId=com.netply                    \
	-DartifactId=Zero-Test-gen              \
	-Dversion=1.0-SNAPSHOT              \
	-Dpackage=test              \
	-DclassPrefix=Test
