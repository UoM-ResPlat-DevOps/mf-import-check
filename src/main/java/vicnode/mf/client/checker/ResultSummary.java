package vicnode.mf.client.checker;

public class ResultSummary {

    private long _totalProcessed = 0;
    private long _totalSizeProcessed = 0;
    private long _totalPassed = 0;
    private long _totalSizePassed = 0;
    private long _totalFailed = 0;
    private long _totalMissing = 0;
    private long _totalSizeDiffer = 0;
    private long _totalCsumDiffer = 0;

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
        _totalSizeProcessed += r.object1().size();
        if (r.allMatch()) {
            _totalPassed++;
            _totalSizePassed += r.object1().size();
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
}
