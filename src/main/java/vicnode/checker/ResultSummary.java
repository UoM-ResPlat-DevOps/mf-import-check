package vicnode.checker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ResultSummary {

    private long _totalProcessed = 0;
    private long _totalSizeProcessed = 0;
    private long _totalPassed = 0;
    private long _totalSizePassed = 0;
    private long _totalFailed = 0;
    private long _totalMissing = 0;
    private long _totalSizeDiffer = 0;
    private long _totalCsumDiffer = 0;

    private List<String> _header;
    private List<String> _footer;

    public ResultSummary() {

    }

    public long totalProcessed() {
        return _totalProcessed;
    }

    public long totalProcessedSize() {
        return _totalSizeProcessed;
    }

    public long totalPassed() {
        return _totalPassed;
    }

    public long totalPassedSize() {
        return _totalSizePassed;
    }

    public long totalFailed() {
        return _totalFailed;
    }

    public long totalMissing() {
        return _totalMissing;
    }

    public long totalSizeDiffer() {
        return _totalSizeDiffer;
    }

    public long totalCsumDiffer() {
        return _totalCsumDiffer;
    }

    public synchronized void add(Result<?, ?> r) {
        _totalProcessed++;
        long csize = r.object1().size() == null ? 0 : r.object1().size();
        if (csize > 0) {
            _totalSizeProcessed += r.object1().size();
        }
        if (r.allMatch()) {
            _totalPassed++;
            if (csize > 0) {
                _totalSizePassed += csize;
            }
        } else {
            _totalFailed++;
            if (!r.bothExist()) {
                _totalMissing++;
            }
            if (!r.sizeMatch()) {
                _totalSizeDiffer++;
            }
            if (!r.checksumMatch()) {
                _totalCsumDiffer++;
            }
        }
    }

    public void save(File outputSummaryFile) throws IOException {
        PrintStream ps = new PrintStream(new BufferedOutputStream(
                new FileOutputStream(outputSummaryFile)));
        try {
            print(ps);
        } finally {
            ps.close();
        }
    }

    public void print(PrintStream w) {
        if (_header != null) {
            for (String line : _header) {
                w.println(line);
            }
        }
        // @formatter:off
        w.println("  total number of processed objects: " + totalProcessed());
        w.println("total size of the processed objects: " + totalProcessedSize());
        w.println("     total number of passed objects: " + totalPassed());
        w.println("       total size of passed objects: " + totalPassedSize());
        w.println("     total number of failed objects: " + totalFailed());
        w.println("          number of missing objects: " + totalMissing());
        w.println("    number of size-mismatch objects: " + totalSizeDiffer());
        w.println("number of checksum-mismatch objects: " + totalCsumDiffer());
        // @formatter:on
        if (_footer != null) {
            for (String line : _footer) {
                w.println(line);
            }
        }
    }

    public void appendToHeader(String line) {
        if (_header == null) {
            _header = new ArrayList<String>();
        }
        _header.add(line);
    }

    public void appendToFooter(String line) {
        if (_footer == null) {
            _footer = new ArrayList<String>();
        }
        _footer.add(line);
    }
}
