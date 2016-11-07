package vicnode.checker;

import vicnode.checker.util.ObjectUtils;
import vicnode.checker.util.PathUtils;

public abstract class AbsoluteObjectInfo implements ObjectInfo {

    private Checksum _checksum;

    @Override
    public String relativePath() {
        return PathUtils.extractRelativePath(absolutePath(), basePath());
    }

    @Override
    public boolean matches(ObjectInfo o) {
        if (o != null) {
            return exists() && o.exists()
                    && ObjectUtils.equals(name(), o.name()) && sizeMatches(o)
                    && checksumMatches(o);
        }
        return false;
    }

    @Override
    public boolean sizeMatches(ObjectInfo o) {
        if (o != null && o.exists()) {
            return ObjectUtils.equals(size(), o.size());
        }
        return false;
    }

    @Override
    public boolean checksumMatches(ObjectInfo o) {
        if (o != null && o.exists()) {
            return ObjectUtils.equals(checksum(), o.checksum());
        }
        return false;
    }

    @Override
    public Checksum checksum() {
        return _checksum;
    }

    protected void setChecksum(Checksum checksum) {
        _checksum = checksum;
    }

    protected void setChecksum(Checksum.Type type, String csum, int base) {
        _checksum = new Checksum(type, csum, base);
    }

}
