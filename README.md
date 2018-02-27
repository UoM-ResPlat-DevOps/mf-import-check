# mf-import-check
A command line tool to compare remote Mediaflux namespace with local directory. The tool will do up to three comparisons on each file: its existence, the size, and an optional CRC32 checksum. A file is deemed to have passed only if all checks that were run have passed. When complete the script will generate a two report files, one a CSV file reporting file by file its pass or fail status of each check, and two a summary report containing the total number of files and bytes checked and how many passes and failures there were.

## I. Installation

  1. Java 8 (JRE or JDK) is required.
  2. Download latest release from: [https://github.com/UoM-ResPlat-DevOps/mf-import-check/releases](https://github.com/UoM-ResPlat-DevOps/mf-import-check/releases)
  3. Unzip it to the destination directory:
    * ```cd opt; sudo unzip ~/Downloads/mf-import-check-x.x.x.zip```

## II. Tools

### mf-import-check

```
Usage: 
    mf-import-check <options> <namespace> <directory>

Options:
    --mf.host <host>                    Mediaflux server host.
    --mf.port <port>                    Mediaflux server port.
    --mf.transport <http|https|tcp/ip>  Mediaflux server transport. Can be http, https or tcp/ip.
    --mf.auth <domain,user,password>    Mediaflux user credentials. In the comma separated form of domain,user,password. For AAF domains the user field should be prepended with the organisations shortname.
    --mf.token <token>                  Mediaflux secure identity token.
    --local-remote                      If specified, check from local directory to remote mediaflux namespace. Otherwise, check from remote mediaflux namespace to local directory.
    --max-threads <number-of-threads>   Maximum number of threads. Defaults to 1.
    --no-csum-check                     Do not compare (crc32) checksums.
    --output <filename-prefix>          Output file name prefix. The output files will be appended with date & time.
    --quiet                             If specified, no progress message is printed to stdout.
```

### Example
Check a local directory against a Mediaflux namespace using AAF credentials, with checksum comparison and 2 threads on a Windows machine. Note the local Windows directory usage of "/" not "\".

```
mf-import-check --mf.host mediaflux.vicnode.org.au --mf.port 443 --mf.transport https --mf.auth aaf,unimelb:username,password --local-remote --max-threads 2 --output testing.csv "/projects/proj-demonstration-1128.4.15/new/testing 2" "c:/data/testing 2"
```
