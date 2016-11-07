package vicnode.mf.client.checker;

import java.io.File;

import vicnode.mf.client.util.PathUtils;

public class AssetFileChecker
        extends AbstractObjectChecker<AssetInfo, FileInfo> {

    protected AssetFileChecker(AssetInfo object1, FileInfo object2,
            boolean csumCheck, ResultHandler<AssetInfo, FileInfo> rh) {
        super(object1, object2, csumCheck, rh);
    }

    @Override
    public void check(AssetInfo object1, FileInfo object2, boolean csumCheck,
            ResultHandler<AssetInfo, FileInfo> rh) {
        rh.checking(object1, object2);
        /*
         * calculate crc32 checksum
         */
        try {
            object2.setCRC32();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        /*
         * result (check happens in the constructor).
         */
        Result<AssetInfo, FileInfo> result = new Result<AssetInfo, FileInfo>(
                object1, object2);
        /*
         * call result handler
         */
        rh.checked(result);
    }

    public static AssetFileChecker create(String baseNamespace, File baseDir,
            String assetId, String assetNamespace, String assetName, Long size,
            Checksum csum, boolean csumCheck,
            ResultHandler<AssetInfo, FileInfo> rh) {
        AssetInfo asset = new AssetInfo(baseNamespace, assetId, assetNamespace,
                assetName, size, csum);
        FileInfo file = new FileInfo(baseDir, new File(PathUtils
                .joinPaths(baseDir.getAbsolutePath(), asset.relativePath())));
        return new AssetFileChecker(asset, file, csumCheck, rh);
    }

}
