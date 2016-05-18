package vicnode.mf.client.util;

import java.io.File;

public class PathUtils {

    public static final String SEPARATOR = "/";

    public static String extractRelativePath(String fullPath,
            String baseDirPath) {
        if (fullPath == null || baseDirPath == null) {
            return fullPath;
        }
        if (fullPath.startsWith(baseDirPath)) {
            String relativePath = StringUtils.trimLeft(fullPath, baseDirPath);
            while (relativePath.startsWith(SEPARATOR)) {
                relativePath = relativePath.substring(1);
            }
            return relativePath;
        }
        return fullPath;
    }

    public static String joinPaths(String path1, String path2) {
        if (path1 == null) {
            return path2;
        }
        if (path2 == null) {
            return path1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.trimRight(path1, SEPARATOR));
        sb.append(SEPARATOR);
        sb.append(StringUtils.trimLeft(path2, SEPARATOR));
        return sb.toString();
    }

    public static String extractRelativePath(File file, File baseDirectory) {
        return extractRelativePath(file.getAbsolutePath(),
                baseDirectory.getAbsolutePath());
    }

}
