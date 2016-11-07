package vicnode.checker.util;

public class ObjectUtils {

    public static boolean equals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null && b != null) {
            return a.equals(b);
        }
        return false;
    }
}
