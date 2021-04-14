#!/usr/bin/env bash
# actors_extractor
#
# This script executes a sql query to export the actors mapping from bonita
# argument 1 - the url of source machine, containing the bonita postgres DB
# argument 2 - the port of the source machine, where postgres resides
# argument 3 - the output folder where the output file will be exported.
# Expected output: a json file containing the actors mapping, based on process definitions.
#
# Usage: ./actors_extractor.sh <psql_host> <psql_port> <output_folder>
# Example: ./actors_extractor.sh 127.0.0.1 5432 ./output
#
# The script uses psql to execute the sql query located in the ./sql folder
# The script checks if psql is installed. If it is not found, the script is aborted.
# In this case, please install psql (check official website for more information: https://www.postgresql.org/download/).
#
# This is a Linux based script. It can also be ran on Windows or other platforms in a bash emulator.
# This was successfully tested locally on a Windows 10 machine in git-bash console (MinTTY emulator).

# check that psql is installed
which psql &>/dev/null && psqlPresent=true
if [ "$psqlPresent" != true ]; then
  echo "psql not found. Please refer to the official website for platform-specific installation of psql. Link: https://www.postgresql.org/"
  exit 1
fi

# check the arguments
if [ "$#" -ne 3 ]; then
  echo "Usage: ./actors_extractor.sh <psql_host> <psql_port> <output_folder>"
  echo "Example: ./actors_extractor.sh 127.0.0.1 5432 ./output"
  exit 1
fi

psql_host=$1
psql_port=$2
output_folder=$3

if [ ! -d $output_folder ]; then
  mkdir -p $output_folder
fi

psql -W -U bonita -d bonita -qtAX -h $psql_host -p $psql_port -f ./sql/exportProcessDefActors.sql >"${output_folder}/"BonitaProcessDefinitionActors.json
