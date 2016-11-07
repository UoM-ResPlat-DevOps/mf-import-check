package vicnode.checker;

public interface ObjectChecker<A extends ObjectInfo, B extends ObjectInfo>
        extends Runnable {

    void check(A a, B b, boolean csumCheck, ResultHandler<A, B> rh);

}
