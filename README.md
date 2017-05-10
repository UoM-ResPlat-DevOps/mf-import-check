# mf-import-check
A command line tool to compare remote Mediaflux namespace with local directory.

```
Usage: mf-import-check <options> <namespace> <directory>
Options:
    --mf.host <host>                    Mediaflux server host.
    --mf.port <port>                    Mediaflux server port.
    --mf.transport <http|https|tcp/ip>  Mediaflux server transport. Can be http, https or tcp/ip.
    --mf.auth <domain,user,password>    Mediaflux user credentials. In the comma separated form of domain,user,password
    --mf.token <token>                  Mediaflux secure identity token.
    --local-remote                      If specified, check from local directory to remote mediaflux namespace. Otherwise, check from remote mediaflux namespace to local directory.
    --max-threads <number-of-threads>   Maximum number of threads. Defaults to 1.
    --no-csum-check                     Do not compare (crc32) checksums.
    --output <file>                     Output file in CSV format.
    --quiet                             If specified, no progress message is printed to stdout.
```
