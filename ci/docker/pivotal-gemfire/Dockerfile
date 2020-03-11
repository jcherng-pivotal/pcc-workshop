# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8-jre-alpine

ADD pivotal-gemfire/pivotal-gemfire-*.tgz /
# runtime dependencies
RUN apk add --no-cache \
		bash \
		ncurses

ENV GEMFIRE_HOME /gemfire
ENV PATH $PATH:$GEMFIRE_HOME/bin

RUN set -eux; \
	mkdir /gemfire; \
	mv /pivotal-gemfire-*/* /gemfire; \
	rm -rf /gemfire/javadoc; \
	ls -la /gemfire; \
	# smoke test to ensure the shell can still run properly after removing temporary deps
	gfsh version

# Default ports:
# RMI/JMX 1099
# REST 8080
# PULE 7070
# LOCATOR 10334
# CACHESERVER 40404
EXPOSE  8080 10334 40404 1099 7070
VOLUME ["/data"]
CMD ["gfsh"]