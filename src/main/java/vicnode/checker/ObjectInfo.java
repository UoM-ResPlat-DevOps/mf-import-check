package vicnode.checker;

public interface ObjectInfo {

    String typeName();

    String basePath();

    String absolutePath();

    String relativePath();

    String name();

    Long size();

    Checksum checksum();

    boolean exists();

    boolean matches(ObjectInfo o);

    boolean sizeMatches(ObjectInfo o);

    boolean checksumMatches(ObjectInfo o);

}
