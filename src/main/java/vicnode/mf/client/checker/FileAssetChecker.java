package vicnode.mf.client.checker;

import java.io.File;
import java.nio.file.Path;

import arc.mf.client.ServerClient;
import vicnode.mf.client.util.PathUtils;

public class FileAssetChecker
        extends AbstractObjectChecker<FileInfo, AssetInfo> {

    private ServerClient.Connection _cxn;

    protected FileAssetChecker(ServerClient.Connection cxn, FileInfo object1,
            AssetInfo object2, boolean csumCheck,
            ResultHandler<FileInfo, AssetInfo> rh) {
        super(object1, object2, csumCheck, rh);
        _cxn = cxn;
    }

    @Override
    public void check(FileInfo object1, AssetInfo object2, boolean csumCheck,
            ResultHandler<FileInfo, AssetInfo> rh) {
        rh.checking(object1, object2);
        try {
            object1.setCRC32();
            object2.updateMetadata(_cxn);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        /*
         * result (check happens in the constructor).
         */
        Result<FileInfo, AssetInfo> result = new Result<FileInfo, AssetInfo>(
                object1, object2);

        /*
         * call result handler
         */
        rh.checked(result);
    }

    public static FileAssetChecker create(ServerClient.Connection cxn,
            String baseNamespace, File baseDir, Path path, boolean csumCheck,
            ResultHandler<FileInfo, AssetInfo> rh) {
        FileInfo file = new FileInfo(baseDir, path.toFile());
        AssetInfo asset = new AssetInfo(baseNamespace,
                PathUtils.joinPaths(baseNamespace, file.relativePath()));
        return new FileAssetChecker(cxn, file, asset, csumCheck, rh);
    }

}
