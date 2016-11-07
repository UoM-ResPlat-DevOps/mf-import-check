package vicnode.mf.client.checker;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class AssetFileCollectionChecker
        extends AbstractCollectionChecker<AssetInfo, FileInfo> {

    public static final int PAGE_SIZE = 1000;

    private ServerClient.Connection _cxn;
    private String _baseNamespace;
    private File _baseDir;

    public AssetFileCollectionChecker(ServerClient.Connection cxn,
            String baseNamespace, File baseDir, boolean csumCheck,
            ResultHandler<AssetInfo, FileInfo> rh, int numberOfThreads) {
        super(csumCheck, rh, numberOfThreads);
        _cxn = cxn;
        _baseNamespace = baseNamespace;
        _baseDir = baseDir;
    }

    @Override
    protected void execute(ExecutorService executor, boolean csumCheck,
            ResultHandler<AssetInfo, FileInfo> rh) throws Throwable {
        int idx = 1;
        int size = PAGE_SIZE;
        while (true) {
            XmlStringWriter w = new XmlStringWriter();
            w.add("idx", idx);
            w.add("size", size);
            w.add("action", "get-value");
            w.add("where", "namespace>='" + _baseNamespace + "'");
            w.add("xpath", new String[] { "ename", "namespace" }, "namespace");
            w.add("xpath", new String[] { "ename", "name" }, "name");
            w.add("xpath", new String[] { "ename", "size" }, "content/size");
            w.add("xpath", new String[] { "ename", "csum" },
                    "content/csum");
            XmlDoc.Element re = _cxn.execute("asset.query", w.document(), null,
                    null);
            boolean complete = re.booleanValue("cursor/total/@complete");
            if (re.elementExists("asset")) {
                List<XmlDoc.Element> aes = re.elements("asset");
                for (final XmlDoc.Element ae : aes) {
                    // check individual (pair)
                    String assetId = ae.value("@id");
                    String assetNamespace = ae.value("namespace");
                    String assetName = ae.value("name");
                    Long csize = ae.longValue("size", null);
                    String csum = ae.value("csum");
                    Checksum checksum = csum == null ? null
                            : new Checksum(Checksum.Type.CRC32, csum, 16);
                    AssetFileChecker checker = AssetFileChecker.create(
                            _baseNamespace, _baseDir, assetId, assetNamespace,
                            assetName, csize, checksum, csumCheck, rh);
                    executor.execute(checker);
                }
            }
            if (complete) {
                break;
            } else {
                idx += size;
            }
        }

    }

}
