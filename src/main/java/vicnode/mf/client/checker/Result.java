package vicnode.mf.client.checker;

import java.io.PrintStream;
import java.util.logging.Logger;

public class Result<A extends ObjectInfo, B extends ObjectInfo> {

    private A _o1;
    private B _o2;
    private boolean _exist;
    private Boolean _sizeMatch;
    private Boolean _csumMatch;

    public Result(A o1, B o2) {
        _o1 = o1;
        _o2 = o2;
        _exist = o1 != null && o2 != null && o1.exists() && o2.exists();
        if (o1 == null || o2 == null) {
            _sizeMatch = null;
            _csumMatch = null;
        } else {
            _sizeMatch = o1.sizeMatches(o2);
            _csumMatch = o1.checksumMatches(o2);
        }
    }

    public A object1() {
        return _o1;
    }

    public B object2() {
        return _o2;
    }

    public boolean bothExist() {
        return _exist;
    }

    public boolean allMatch() {
        return bothExist() && sizeMatch() != null && sizeMatch()
                && checksumMatch() != null && checksumMatch();
    }

    public Boolean sizeMatch() {
        return _sizeMatch;
    }

    public Boolean checksumMatch() {
        return _csumMatch;
    }

    public String toString() {
        return toCSV();
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(_o1.absolutePath()).append("\",");
        sb.append(_o1.size()).append(",");
        sb.append(_o1.checksum()).append(",");
        sb.append("\"").append(_o2.absolutePath()).append("\",");
        sb.append(_o2.size()).append(",");
        sb.append(_o2.checksum()).append(",");
        sb.append(bothExist()).append(",");
        sb.append(sizeMatch()).append(",");
        sb.append(checksumMatch()).append(",");
        sb.append(allMatch()).append(",");
        return sb.toString();
    }

    public void println(PrintStream ps) {
        ps.println(toString());
    }

    public void log(Logger logger) {
        logger.info(toString());
    }

    public static String getCSVHeader(String type1, String type2) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(type1).append(" path\",");
        sb.append("\"").append(type1).append(" size\",");
        sb.append("\"").append(type1).append(" checksum\",");
        sb.append("\"").append(type2).append(" path\",");
        sb.append("\"").append(type2).append(" size\",");
        sb.append("\"").append(type2).append(" checksum\",");
        sb.append("\"exists?\",");
        sb.append("\"size match?\",");
        sb.append("\"checksum match?\",");
        sb.append("\"all match?\",");
        return sb.toString();
    }

}
