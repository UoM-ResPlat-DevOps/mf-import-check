package vicnode.checker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

public class Result<A extends ObjectInfo, B extends ObjectInfo> {

    private A _o1;
    private B _o2;

    public Result(A o1, B o2) {
        _o1 = o1;
        _o2 = o2;
    }

    public A object1() {
        return _o1;
    }

    public B object2() {
        return _o2;
    }

    public boolean bothExist() {
        boolean exist = _o1 != null && _o2 != null && _o1.exists()
                && _o2.exists();
        return exist;
    }

    public boolean allMatch() {
        return bothExist() && sizeMatch() != null && sizeMatch()
                && checksumMatch() != null && checksumMatch();
    }

    public Boolean sizeMatch() {
        return _o1 != null && _o2 != null && _o1.sizeMatches(_o2);
    }

    public Boolean checksumMatch() {
        return _o1 != null && _o2 != null && _o1.checksumMatches(_o2);
    }

    public String toString() {
        return toCSV(false);
    }

    public String toCSV(boolean noCsumCheck) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(_o1.absolutePath()).append("\",");
        sb.append(_o1.size()).append(",");
        if (!noCsumCheck) {
            sb.append(_o1.checksum()).append(",");
        }
        sb.append("\"").append(_o2.absolutePath()).append("\",");
        sb.append(_o2.size()).append(",");
        if (!noCsumCheck) {
            sb.append(_o2.checksum()).append(",");
        }
        sb.append(bothExist()).append(",");
        sb.append(sizeMatch()).append(",");
        if (!noCsumCheck) {
            sb.append(checksumMatch()).append(",");
            sb.append(allMatch()).append(",");
        }
        return sb.toString();
    }

    public void printCSV(PrintStream ps, boolean noCsumCheck) {
        ps.println(toCSV(noCsumCheck));
    }

    public void logCSV(Logger logger, boolean noCsumCheck) {
        logger.info(toCSV(noCsumCheck) + "\n");
    }

    public static String getCSVHeader(String type1, String type2,
            boolean noCsumCheck) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(type1).append(" path\",");
        sb.append("\"").append(type1).append(" size\",");
        if (!noCsumCheck) {
            sb.append("\"").append(type1).append(" checksum\",");
        }
        sb.append("\"").append(type2).append(" path\",");
        sb.append("\"").append(type2).append(" size\",");
        if (!noCsumCheck) {
            sb.append("\"").append(type2).append(" checksum\",");
        }
        sb.append("\"exists?\",");
        sb.append("\"size match?\",");
        if (!noCsumCheck) {
            sb.append("\"checksum match?\",");
            sb.append("\"all match?\",");
        }
        return sb.toString();
    }

    public static void printCSVHeader(String type1, String type2,
            PrintStream ps, boolean noCsumCheck) {
        ps.println(getCSVHeader(type1, type2, noCsumCheck));
    }

    public static void writeCSVHeader(String type1, String type2, File out,
            boolean noCsumCheck) throws Throwable {
        PrintStream ps = new PrintStream(
                new BufferedOutputStream(new FileOutputStream(out)));
        try {
            printCSVHeader(type1, type2, ps, noCsumCheck);
        } finally {
            ps.close();
        }
    }

}
