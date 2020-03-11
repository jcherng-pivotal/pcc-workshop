#!/usr/bin/env bash
set -eux

cp pivnet/pivotal-gemfire-*.tgz pivotal-gemfire/
cat pivnet/metadata.json | jq --raw-output '.Release.Version' >> pivotal-gemfire/version