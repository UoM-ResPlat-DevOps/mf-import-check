package vicnode.mf.client.checker;

import java.io.File;

import vicnode.mf.client.util.ChecksumUtils;

public class FileInfo extends AbsoluteObjectInfo {

    private File _baseDir;
    private File _file;

    public FileInfo(File baseDir, File file) {
        _baseDir = baseDir;
        _file = file;
    }

    @Override
    public String name() {
        return _file.getName();
    }

    @Override
    public Long size() {
        return _file.exists() ? _file.length() : null;
    }

    @Override
    public boolean exists() {
        return _file.exists();
    }

    @Override
    public String basePath() {
        return _baseDir.getAbsolutePath();
    }

    @Override
    public String absolutePath() {
        return _file.getAbsolutePath();
    }

    @Override
    public String typeName() {
        return "file";
    }

    public void setCRC32() throws Throwable {
        if (checksum() == null || checksum().type() != Checksum.Type.CRC32) {
            try {
                setChecksum(calculateCRC32(_file));
            } catch (Throwable e) {
                setChecksum(null);
                throw e;
            }
        }
    }

    public static Checksum calculateCRC32(File f) throws Throwable {
        long csum = ChecksumUtils.crc32(f);
        return new Checksum(Checksum.Type.CRC32, csum);
    }

}
