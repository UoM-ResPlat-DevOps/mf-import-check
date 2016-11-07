package vicnode.mf.client.checker;

public interface ResultHandler<A extends ObjectInfo, B extends ObjectInfo> {
    void checked(Result<A, B> result);
}
