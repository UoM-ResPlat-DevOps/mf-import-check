package vicnode.checker.file;

import java.io.File;
import java.nio.file.Path;

import arc.mf.client.ServerClient;
import vicnode.checker.AbstractObjectChecker;
import vicnode.checker.Result;
import vicnode.checker.ResultHandler;
import vicnode.checker.mf.AssetInfo;
import vicnode.checker.util.PathUtils;
import vicnode.mf.client.MFSession;

public class FileAssetChecker extends AbstractObjectChecker<FileInfo, AssetInfo> {

    private MFSession _session;

    protected FileAssetChecker(MFSession session, FileInfo object1, AssetInfo object2, boolean csumCheck,
            ResultHandler<FileInfo, AssetInfo> rh) {
        super(object1, object2, csumCheck, rh);
        _session = session;
    }

    @Override
    public void check(FileInfo object1, AssetInfo object2, boolean csumCheck, ResultHandler<FileInfo, AssetInfo> rh) {
        rh.checking(object1, object2);
        try {
            object1.setCRC32();
            ServerClient.Connection cxn = _session.connect();
            try {
                object2.updateMetadata(cxn);
            } finally {
                cxn.close();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        /*
         * result (check happens in the constructor).
         */
        Result<FileInfo, AssetInfo> result = new Result<FileInfo, AssetInfo>(object1, object2);

        /*
         * call result handler
         */
        rh.checked(result);
    }

    public static FileAssetChecker create(MFSession session, String baseNamespace, File baseDir, Path path,
            boolean csumCheck, ResultHandler<FileInfo, AssetInfo> rh) {
        FileInfo file = new FileInfo(baseDir, path.toFile());
        AssetInfo asset = new AssetInfo(baseNamespace, PathUtils.joinPaths(baseNamespace, file.relativePath()));
        return new FileAssetChecker(session, file, asset, csumCheck, rh);
    }

}
