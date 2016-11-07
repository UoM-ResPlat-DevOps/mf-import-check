package vicnode.checker.file;

import java.io.File;

import vicnode.checker.AbsoluteObjectInfo;
import vicnode.checker.Checksum;
import vicnode.checker.util.ChecksumUtils;

public class FileInfo extends AbsoluteObjectInfo {

    private File _baseDir;
    private File _file;
    private Long _csize = null;

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
        if (_csize == null) {
            if (_file.exists()) {
                _csize = _file.length();
            }
        }
        return _csize;
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
        if (exists()) {
            if (checksum() == null
                    || checksum().type() != Checksum.Type.CRC32) {
                try {
                    setChecksum(calculateCRC32(_file));
                } catch (Throwable e) {
                    setChecksum(null);
                    throw e;
                }
            }
        } else {
            setChecksum(null);
        }
    }

    public static Checksum calculateCRC32(File f) throws Throwable {
        long csum = ChecksumUtils.crc32(f);
        return new Checksum(Checksum.Type.CRC32, csum);
    }

}
