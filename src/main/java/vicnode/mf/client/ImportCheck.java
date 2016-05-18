package vicnode.mf.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class ImportCheck {

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
        Boolean localRemote = null;
        Integer maxThreads = null;
        Boolean noCsumCheck = null;
        File outputFile = null;
        String namespace = args[args.length - 2];
        File directory = new File(args[args.length - 1]);

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
                    if (localRemote != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --local-remote argument.");
                    }
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
                    if (noCsumCheck != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --no-csum-check argument.");
                    }
                    noCsumCheck = true;
                    i++;
                } else if (args[i].equals("--output")) {
                    if (outputFile != null) {
                        throw new IllegalArgumentException(
                                "Expects only one --output argument.");
                    }
                    outputFile = new File(args[i + 1]);
                    if (outputFile.exists()) {
                        throw new IllegalArgumentException("Output file: \""
                                + args[i + 1] + "\" already exists.");
                    }
                    i += 2;
                } else {
                    throw new IllegalArgumentException(
                            "Unexpected argument: " + args[i]);
                }
            }

            if (outputFile == null) {
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

            if (localRemote == null) {
                localRemote = false;
            }

            if (noCsumCheck == null) {
                noCsumCheck = false;
            }

            if (maxThreads == null) {
                maxThreads = 1;
            }

            RemoteServer server = new RemoteServer(host, port, useHttp,
                    encrypt);
            ServerClient.Connection cxn = server.open();
            final PrintStream out = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(outputFile, false)));
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
                ResultHandler rh = new ResultHandler() {
                    @Override
                    public void checked(Result result) {
                        result.println(out);
                    }
                };
                if (localRemote) {
                    check(cxn, directory, namespace, maxThreads, noCsumCheck,
                            rh);
                } else {
                    check(cxn, namespace, directory, maxThreads, noCsumCheck,
                            rh);
                }
            } finally {
                cxn.close();
                out.close();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            if (e instanceof IllegalArgumentException) {
                printUsage(System.out);
            }
            System.exit(1);
        }
    }

    private static void check(Connection cxn, final String namespace,
            final File directory, int maxThreads, final boolean noCsumCheck,
            final ResultHandler rh) throws Throwable {
        final ExecutorService executor = Executors
                .newFixedThreadPool(maxThreads);
        int idx = 1;
        int size = PAGE_SIZE;
        while (true) {
            XmlStringWriter w = new XmlStringWriter();
            w.add("idx", idx);
            w.add("size", size);
            w.add("action", "get-value");
            w.add("where", "namespace>='" + namespace + "'");
            w.add("xpath", new String[] { "ename", "namespace" }, "namespace");
            w.add("xpath", new String[] { "ename", "name" }, "name");
            w.add("xpath", new String[] { "ename", "size" }, "content/size");
            w.add("xpath", new String[] { "ename", "csum" }, "content/csum");
            XmlDoc.Element re = cxn.execute("asset.query", w.document(), null,
                    null);
            boolean complete = re.booleanValue("cursor/total/@complete");
            if (re.elementExists("asset")) {
                List<XmlDoc.Element> aes = re.elements("asset");
                for (final XmlDoc.Element ae : aes) {
                    executor.execute(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Result result = new Result(ae, namespace,
                                        directory, noCsumCheck);
                                if (rh != null) {
                                    rh.checked(result);
                                }
                            } catch (Throwable e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    });

                }
            }
            if (complete) {
                break;
            } else {
                idx += size;
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {

        }
    }

    private static void check(final Connection cxn, final File directory,
            final String namespace, int maxThreads, final boolean noCsumCheck,
            final ResultHandler rh) throws Throwable {
        final ExecutorService executor = Executors
                .newFixedThreadPool(maxThreads);
        Files.walkFileTree(Paths.get(directory.getAbsolutePath()),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path filePath,
                            BasicFileAttributes attrs) throws IOException {
                        final File file = filePath.toFile();
                        executor.execute(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Result result = new Result(file, cxn,
                                            directory, namespace, noCsumCheck);
                                    if (rh != null) {
                                        rh.checked(result);
                                    }
                                } catch (Throwable e) {
                                    e.printStackTrace(System.err);
                                }
                            }
                        });
                        return FileVisitResult.CONTINUE;
                    }
                });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
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

    }

}
