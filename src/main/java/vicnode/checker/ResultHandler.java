package vicnode.checker;

public interface ResultHandler<A extends ObjectInfo, B extends ObjectInfo> {

    void checking(A object1, B object2);

    void checked(Result<A, B> result);
}
