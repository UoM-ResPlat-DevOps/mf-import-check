package vicnode.checker.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;

import vicnode.checker.AbstractCollectionChecker;
import vicnode.checker.ResultHandler;
import vicnode.checker.mf.AssetInfo;
import vicnode.mf.client.MFSession;

public class FileAssetCollectionChecker extends AbstractCollectionChecker<FileInfo, AssetInfo> {

    private MFSession _session;
    private String _baseNamespace;
    private File _baseDir;

    public FileAssetCollectionChecker(MFSession session, String baseNamespace, File baseDir, boolean csumCheck,
            ResultHandler<FileInfo, AssetInfo> resultHandler, int numberOfThreads) {
        super(csumCheck, resultHandler, numberOfThreads);
        _session = session;
        _baseNamespace = baseNamespace;
        _baseDir = baseDir;
    }

    @Override
    protected void execute(final ExecutorService executor, final boolean csumCheck,
            final ResultHandler<FileInfo, AssetInfo> rh) throws Throwable {
        Files.walkFileTree(Paths.get(_baseDir.getAbsolutePath()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                FileAssetChecker checker = FileAssetChecker.create(_session, _baseNamespace, _baseDir, path, csumCheck, rh);
                executor.execute(checker);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException ioe) {
                ioe.printStackTrace(System.err);
                return FileVisitResult.SKIP_SUBTREE;
            }
        });

    }

}
