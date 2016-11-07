package vicnode.checker;

public interface CollectionChecker<A extends ObjectInfo, B extends ObjectInfo> {

    boolean csumCheck();

    ResultHandler<A, B> resultHandler();

    int numberOfThreads();

    ResultSummary execute() throws Throwable;

}
