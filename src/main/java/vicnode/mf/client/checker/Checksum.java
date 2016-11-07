package vicnode.mf.client.checker;

public class Checksum {

    public static enum Type {
        CRC32, MD5, SHA1, SHA256
    }

    private Type _type;
    private String _csum;
    private int _base;

    public Checksum(Type type, long csum, int base) {
        _type = type;
        _csum = Long.toString(csum, base);
        _base = base;
    }

    public Checksum(Type type, long csum) {
        this(type, csum, 16);
    }

    public Checksum(Type type, String csum, int base) {
        _type = type;
        _csum = csum;
        _base = base;
    }

    public Checksum(Type type, String csum) {
        this(type, csum, 16);
    }

    public String csum() {
        return _csum;
    }

    public int base() {
        return _base;
    }

    public Type type() {
        return _type;
    }

    @Override
    public final boolean equals(Object o) {
        if (o != null && (o instanceof Checksum) && csum() != null) {
            Checksum co = (Checksum) o;
            return type() == co.type() && base() == co.base()
                    && csum().equalsIgnoreCase(co.csum());
        }
        return false;
    }

    @Override
    public final String toString() {
        return _csum;
    }

}
