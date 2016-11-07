package vicnode.mf.client.checker;

public abstract class AbstractObjectChecker<A extends ObjectInfo, B extends ObjectInfo>
        implements ObjectChecker<A, B> {

    private A _object1;
    private B _object2;
    private boolean _csumCheck;
    private ResultHandler<A, B> _rh;

    protected AbstractObjectChecker(A object1, B object2, boolean csumCheck,
            ResultHandler<A, B> rh) {
        _object1 = object1;
        _object2 = object2;
        _csumCheck = csumCheck;
        _rh = rh;
    }

    @Override
    public final void run() {
        check(_object1, _object2, _csumCheck, _rh);
    }

}
