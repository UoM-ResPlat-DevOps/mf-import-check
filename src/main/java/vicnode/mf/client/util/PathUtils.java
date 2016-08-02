package vicnode.mf.client.util;

import java.io.File;

public class PathUtils {

    public static final String SEPARATOR = "/";

    public static String toUnixPath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("\\\\", "/").replace("\\", "/");
    }

    public static String extractRelativePath(String fullPath,
            String baseDirPath) {
        if (fullPath == null || baseDirPath == null) {
            return toUnixPath(fullPath);
        }
        fullPath = toUnixPath(fullPath);
        baseDirPath = toUnixPath(baseDirPath);
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
            return toUnixPath(path2);
        }
        if (path2 == null) {
            return toUnixPath(path1);
        }
        path1 = toUnixPath(path1);
        path2 = toUnixPath(path2);
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

    public static String extractFileName(String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith("/")) {
            return null;
        }
        return lastComponentOf(path);
    }

    public static String extractParentDirectoryPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        path = trimTrailingSlash(path);
        int idx = path.lastIndexOf('/');
        if (idx < 0) {
            return null;
        } else if (idx == 0) {
            return path.substring(0, 1);
        } else {
            return path.substring(0, idx);
        }
    }

    public static String lastComponentOf(String path) {
        path = trimTrailingSlash(path);
        int idx = path.lastIndexOf('/');
        if (idx < 0) {
            return path;
        } else {
            return path.substring(idx + 1);
        }
    }

    public static String trimTrailingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}
