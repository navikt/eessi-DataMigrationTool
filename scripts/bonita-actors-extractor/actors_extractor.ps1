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
# This is a Windows PowerShell based script.
# This was successfully tested locally on a Windows 10 machine in PowerShell.


# check that psql is installed
if ((Get-Command "psql" -ErrorAction SilentlyContinue) -eq $null) {
   Write-Host "psql not found. Please refer to the official website for platform-specific installation of psql. Link: https://www.postgresql.org/"
   exit 1
}

# check the arguments
if ($args.Count -ne 3 ) {
    echo "Usage: ./actors_extractor.ps1 <psql_host> <psql_port> <output_folder>"
    echo "Example: ./actors_extractor.ps1 127.0.0.1 5432 ./output"
    exit 1
} 

$psql_host=$args[0]
$psql_port=$args[1]
$output_folder=$args[2]
$database='bonita'
$user='bonita'

if( -Not (Test-Path -Path $output_folder ) )
{
    New-Item -ItemType directory -Path $output_folder
}

$dburl="postgresql://$user" + "@" + "$psql_host" + ":" + "$psql_port" + "/" + "$database"

psql -W -qtAX -f "./sql/exportProcessDefActors.sql" $dburl | out-file -encoding ASCII $output_folder/BonitaProcessDefinitionActors.json

echo "Finished"