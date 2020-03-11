#!/bin/bash
# Absolute path to this script, e.g. /home/user/bin/foo.sh
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SCRIPT_PATH="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
echo "SCRIPT_PATH: $SCRIPT_PATH"
APP_HOME="$( cd -P "$( dirname "$SCRIPT_PATH/../../.." )" && pwd )"
echo "APP_HOME: $APP_HOME"

gfsh \
-e "connect" \
-e "shutdown --include-locators=true"

ls -la ${APP_HOME}/data
