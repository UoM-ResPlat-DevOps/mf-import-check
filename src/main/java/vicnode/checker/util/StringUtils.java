package vicnode.checker.util;

public class StringUtils {

    public static String trimLeft(String s, String prefix) {
        if (s == null || prefix == null) {
            return s;
        }
        while (s.startsWith(prefix)) {
            s = s.substring(prefix.length());
        }
        return s;
    }

    public static String trimRight(String s, String suffix) {
        if (s == null || suffix == null) {
            return s;
        }
        while (s.endsWith(suffix)) {
            s = s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    public static String trim(String str, String ch) {
        if (str == null || ch == null) {
            return str;
        }
        return trimRight(trimLeft(str, ch), ch);
    }

}
