package vicnode.mf.client.checker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractCollectionChecker<A extends ObjectInfo, B extends ObjectInfo>
        implements CollectionChecker<A, B> {

    protected boolean csumCheck = true;
    protected ResultHandler<A, B> resultHandler;
    protected int numberOfThreads = 1;

    protected AbstractCollectionChecker(boolean csumCheck,
            ResultHandler<A, B> resultHandler, int numberOfThreads) {
        this.csumCheck = csumCheck;
        this.resultHandler = resultHandler;
        this.numberOfThreads = numberOfThreads;
    }

    @Override
    public ResultHandler<A, B> resultHandler() {
        return this.resultHandler;
    }

    @Override
    public int numberOfThreads() {
        return this.numberOfThreads;
    }

    @Override
    public boolean csumCheck() {
        return this.csumCheck;
    }

    @Override
    public ResultSummary execute() throws Throwable {
        final ResultSummary rs = new ResultSummary();
        ExecutorService executor = Executors
                .newFixedThreadPool(this.numberOfThreads);
        execute(executor, csumCheck(), new ResultHandler<A, B>() {

            @Override
            public void checked(Result<A, B> result) {
                rs.add(result);
                resultHandler().checked(result);
            }
        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return rs;
    }

    protected abstract void execute(ExecutorService executor, boolean csumCheck,
            ResultHandler<A, B> rh) throws Throwable;

}
