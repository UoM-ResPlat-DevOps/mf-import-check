package vicnode.checker.cli;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import vicnode.checker.Result;
import vicnode.checker.ResultHandler;
import vicnode.checker.ResultSummary;
import vicnode.checker.file.FileAssetCollectionChecker;
import vicnode.checker.file.FileInfo;
import vicnode.checker.mf.AssetFileCollectionChecker;
import vicnode.checker.mf.AssetInfo;

public class MFImportCheck {

    public static final String PROG = "mf-import-check";

    public static final int PAGE_SIZE = 1000;

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Error: missing arguments.");
            printUsage(System.out);
            System.exit(1);
        }
        String host = null;
        Integer port = null;
        String transport = null;
        String auth = null;
        String token = null;
        boolean localRemote = false;
        Integer maxThreads = null;
        boolean noCsumCheck = false;
        File outputCSVFile = null;
        File outputSummaryFile = null;
        String namespace = args[args.length - 2];
        File directory = new File(args[args.length - 1]);
        boolean quiet = false;

        try {
            if (!directory.exists()) {
                throw new IllegalArgumentException("Local directory: \""
                        + directory.getAbsolutePath() + "\" does not exist.");
            }
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException(
                        "\"" + directory.getAbsolutePath()
                                + "\" is not a directory.");
            }
            for (int i = 0; i < args.length - 2;) {
                if (args[i].equals("--mf.host")) {
                    if (host != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --mf.host argument.");
                    }
                    host = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.port")) {
                    if (port != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --mf.port argument.");
                    }
                    try {
                        port = Integer.parseInt(args[i + 1]);
                    } catch (Throwable e) {
                        throw new IllegalArgumentException(
                                "Invalid --mf.port argument. Expects an integer. Found "
                                        + args[i + 1],
                                e);
                    }
                    i += 2;
                } else if (args[i].equals("--mf.transport")) {
                    if (transport != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --mf.transport argument.");
                    }
                    transport = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--mf.auth")) {
                    if (auth != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --mf.auth argument.");
                    }
                    auth = args[i + 1];
                    if (auth.split(",").length != 3) {
                        throw new IllegalArgumentException(
                                "Invalid --mf.auth argument value: " + auth);
                    }
                    i += 2;
                } else if (args[i].equals("--mf.token")) {
                    if (token != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --mf.token argument.");
                    }
                    token = args[i + 1];
                    i += 2;
                } else if (args[i].equals("--local-remote")) {
                    localRemote = true;
                    i++;
                } else if (args[i].equals("--max-threads")) {
                    if (maxThreads != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --max-threads argument.");
                    }
                    try {
                        maxThreads = Integer.parseInt(args[i + 1]);
                    } catch (Throwable e) {
                        throw new IllegalArgumentException(
                                "Invalid --max-threads argument. Expects an integer. Found "
                                        + args[i + 1],
                                e);
                    }
                    i += 2;
                } else if (args[i].equals("--no-csum-check")) {
                    noCsumCheck = true;
                    i++;
                } else if (args[i].equals("--output")) {
                    if (outputCSVFile != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --output argument.");
                    }
                    String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
                            .format(new Date());
                    outputCSVFile = new File(generateOutputFilePath(args[i + 1],
                            timestamp, ".csv"));
                    outputSummaryFile = new File(generateOutputFilePath(
                            args[i + 1], timestamp, ".summary.txt"));
                    if (outputCSVFile.exists()) {
                        throw new IllegalArgumentException("Output CSV file: \""
                                + outputCSVFile.getAbsolutePath()
                                + "\" already exists.");
                    }
                    i += 2;
                } else if (args[i].equals("--quiet")) {
                    quiet = true;
                    i++;
                } else {
                    throw new IllegalArgumentException(
                            "Unexpected argument: " + args[i]);
                }
            }

            if (outputCSVFile == null) {
                throw new IllegalArgumentException(
                        "Missing --output argument.");
            }

            if (host == null) {
                host = System.getProperty("mf.host");
            }
            if (host == null) {
                host = System.getenv("MFLUX_HOST");
            }
            if (host == null) {
                throw new IllegalArgumentException(
                        "Missing --mf.host argument.");
            }

            if (port == null) {
                port = parsePort(System.getProperty("mf.port"));
            }
            if (port == null) {
                port = parsePort(System.getenv("MFLUX_HOST"));
            }
            if (port == null) {
                throw new IllegalArgumentException(
                        "Missing --mf.port argument.");
            }

            if (transport == null) {
                transport = System.getProperty("mf.transport");
            }
            if (transport == null) {
                transport = System.getenv("MFLUX_TRANSPORT");
            }
            if (transport == null) {
                throw new IllegalArgumentException(
                        "Missing --mf.transport argument.");
            }
            if (!("http".equalsIgnoreCase(transport)
                    || "https".equalsIgnoreCase(transport)
                    || "https".equalsIgnoreCase(transport))) {
                throw new IllegalArgumentException(
                        "Invalid --mf.transport value: " + transport
                                + ". Expects http, https or tcp/ip");
            }

            boolean useHttp = "http".equalsIgnoreCase(transport)
                    || "https".equalsIgnoreCase(transport);
            boolean encrypt = "https".equalsIgnoreCase(transport);

            if (auth == null) {
                auth = System.getProperty("mf.auth");
            }
            if (auth == null) {
                auth = System.getenv("MFLUX_AUTH");
            }

            if (token == null) {
                token = System.getProperty("mf.token");
            }
            if (token == null) {
                token = System.getenv("MFLUX_TOKEN");
            }

            if (auth == null && token == null) {
                throw new IllegalArgumentException(
                        "Missing --mf.transport argument or --mf.token argument.");
            }

            if (maxThreads == null) {
                maxThreads = 1;
            }

            RemoteServer server = new RemoteServer(host, port, useHttp,
                    encrypt);
            ServerClient.Connection cxn = server.open();

            /*
             * Print result csv header line.
             */
            Result.writeCSVHeader("asset", "file", outputCSVFile, noCsumCheck);

            /*
             * Create result csv logger.
             */
            final Logger csvLogger = createLogger(outputCSVFile);
            try {

                if (auth != null) {
                    String[] parts = auth.split(",");
                    if (parts.length != 3) {
                        throw new IllegalArgumentException(
                                "Invalid user credentials: " + auth);
                    }
                    cxn.connect(parts[0], parts[1], parts[2]);
                } else {
                    cxn.connectWithToken(token);
                }
                boolean namespaceExists = cxn.execute("asset.namespace.exists",
                        "<namespace>" + namespace + "</namespace>", null, null)
                        .booleanValue("exists");
                if (!namespaceExists) {
                    throw new IllegalArgumentException(
                            "Namespace \"" + namespace + "\" does not exist.");
                }
                final boolean csumCheck = !noCsumCheck;
                final boolean verbose = !quiet;
                ResultSummary summary = new ResultSummary();
                if (localRemote) {
                    ResultHandler<FileInfo, AssetInfo> rh = new ResultHandler<FileInfo, AssetInfo>() {
                        @Override
                        public void checked(
                                Result<FileInfo, AssetInfo> result) {
                            result.logCSV(csvLogger, !csumCheck);
                        }

                        @Override
                        public void checking(FileInfo object1,
                                AssetInfo object2) {
                            if (verbose) {
                                long threadId = Thread.currentThread().getId();
                                System.out.println("Thread " + threadId
                                        + ": checking asset: \""
                                        + object2.relativePath() + "\"...");
                            }
                        }
                    };
                    summary = new FileAssetCollectionChecker(cxn, namespace,
                            directory, csumCheck, rh, maxThreads).execute();
                    // @formatter:off
                    summary.appendToHeader("                    local directory: " + directory.getAbsolutePath());
                    summary.appendToHeader("          Mediaflux asset namespace: " + namespace);
                    // @formatter:on
                } else {
                    ResultHandler<AssetInfo, FileInfo> rh = new ResultHandler<AssetInfo, FileInfo>() {
                        @Override
                        public void checked(
                                Result<AssetInfo, FileInfo> result) {
                            result.logCSV(csvLogger, !csumCheck);
                        }

                        @Override
                        public void checking(AssetInfo object1,
                                FileInfo object2) {
                            if (verbose) {
                                long threadId = Thread.currentThread().getId();
                                System.out.println("Thread " + threadId
                                        + ": checking file: \""
                                        + object2.relativePath() + "\"...");
                            }
                        }
                    };
                    summary = new AssetFileCollectionChecker(cxn, namespace,
                            directory, csumCheck, rh, maxThreads).execute();
                    // @formatter:off
                    summary.appendToHeader("          Mediaflux asset namespace: " + namespace);
                    summary.appendToHeader("                    local directory: " + directory.getAbsolutePath());
                    // @formatter:on
                }
                summary.save(outputSummaryFile);
                summary.print(System.out);
            } finally {
                closeLogHandlers(csvLogger);
                cxn.close();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            if (e instanceof IllegalArgumentException) {
                printUsage(System.out);
            }
            System.exit(1);
        }
    }

    private static String generateOutputFilePath(String path, String timestamp,
            String extension) {
        String name = path;
        if (path.toLowerCase().endsWith(".csv")
                || path.toLowerCase().endsWith(".txt")) {
            name = name.substring(0, path.length() - 4);
        }
        StringBuilder sb = new StringBuilder(name);
        sb.append(".").append(timestamp);
        if (extension != null) {
            if (extension.startsWith(".")) {
                sb.append(extension);
            } else {
                sb.append(".").append(extension);
            }
        }
        return sb.toString();
    }

    private static Integer parsePort(String portStr) throws Throwable {
        Integer port = null;
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (Throwable e) {
                throw new IllegalArgumentException(
                        "Invalid --mf.port argument. Expects an integer. Found "
                                + portStr,
                        e);
            }
        }
        return port;
    }

    private static Logger createLogger(File outputFile) throws Throwable {
        Logger logger = Logger.getLogger(MFImportCheck.class.getName());
        FileHandler fh = new FileHandler(outputFile.getAbsolutePath(),
                1000000000, 1, true);
        fh.setFormatter(new Formatter() {

            @Override
            public String format(LogRecord record) {
                return record.getMessage();
            }
        });
        logger.setLevel(Level.ALL);
        logger.addHandler(fh);
        return logger;
    }

    private static void closeLogHandlers(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }

    private static void printUsage(PrintStream ps) {
        ps.println("Usage: " + PROG + " <options> <namespace> <directory>");
        ps.println("Options:");
        ps.println(
                "    --mf.host <host>                    Mediaflux server host.");
        ps.println(
                "    --mf.port <port>                    Mediaflux server port.");
        ps.println(
                "    --mf.transport <http|https|tcp/ip>  Mediaflux server transport. Can be http, https or tcp/ip.");
        ps.println(
                "    --mf.auth <domain,user,password>    Mediaflux user credentials. In the comma separated form of domain,user,password");
        ps.println(
                "    --mf.token <token>                  Mediaflux secure identity token.");
        ps.println(
                "    --local-remote                      If specified, check from local directory to remote mediaflux namespace. Otherwise, check from remote mediaflux namespace to local directory.");
        ps.println(
                "    --max-threads <number-of-threads>   Maximum number of threads. Defaults to 1.");
        ps.println(
                "    --no-csum-check                     Do not compare (crc32) checksums.");
        ps.println(
                "    --output <file>                     Output file in CSV format.");
        ps.println(
                "    --quiet                             If specified, no progress message is printed to stdout.");

    }

}
