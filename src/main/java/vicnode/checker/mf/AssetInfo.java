package vicnode.checker.mf;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;
import vicnode.checker.AbsoluteObjectInfo;
import vicnode.checker.Checksum;
import vicnode.checker.util.PathUtils;

public class AssetInfo extends AbsoluteObjectInfo {

    private String _baseNamespace;
    private String _assetId;
    private String _assetNamespace;
    private String _assetName;
    private String _assetPath;
    private Long _csize;

    public AssetInfo(String baseNamespace, String assetId,
            String assetNamespace, String assetName, Long size, Checksum csum) {
        _baseNamespace = baseNamespace;
        _assetId = assetId;
        _assetNamespace = assetNamespace;
        _assetName = assetName;
        _assetPath = PathUtils.joinPaths(assetNamespace,
                assetName == null ? ("__asset_id__" + assetId) : assetName);
        _csize = size;
        setChecksum(csum);
    }

    public AssetInfo(String baseNamespace, String assetPath) {
        _baseNamespace = baseNamespace;
        _assetId = null;
        _assetNamespace = PathUtils.extractParentDirectoryPath(assetPath);
        _assetName = PathUtils.extractFileName(assetPath);
        _assetPath = assetPath;
        _csize = null;
        setChecksum(null);
    }

    @Override
    public String basePath() {
        return _baseNamespace;
    }

    @Override
    public String absolutePath() {
        return _assetPath;
    }

    @Override
    public String name() {
        return _assetName;
    }

    @Override
    public Long size() {
        return _csize;
    }

    @Override
    public boolean exists() {
        return _assetId != null;
    }

    @Override
    public String typeName() {
        return "asset";
    }

    public void setAssetId(String assetId) {
        _assetId = assetId;
    }

    public void setSize(Long size) {
        _csize = size;
    }

    public XmlDoc.Element resolveMetadata(ServerClient.Connection cxn)
            throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        w.add("id", "path=" + _assetPath);
        XmlDoc.Element ae = cxn.execute("asset.get", w.document(), null, null)
                .element("asset");
        return ae;
    }

    public void updateMetadata(ServerClient.Connection cxn) throws Throwable {
        XmlDoc.Element ae = resolveMetadata(cxn);
        update(ae);
    }

    private void update(XmlDoc.Element ae) throws Throwable {
        _assetId = ae.value("@id");
        if (_assetNamespace == null) {
            _assetNamespace = ae.value("namespace");
        }
        if (_assetName == null) {
            _assetName = ae.value("name");
        }
        if (_assetPath == null) {
            _assetPath = _assetNamespace + "/" + (_assetName == null
                    ? ("__asset_id__" + _assetId) : _assetName);
        }
        if (ae.elementExists("content/size")) {
            _csize = ae.longValue("content/size");
        } else {
            _csize = null;
        }
        if (ae.elementExists("content/csum[@base='16']")) {
            setChecksum(new Checksum(Checksum.Type.CRC32,
                    ae.value("content/csum[@base='16']"), 16));
        } else if (ae.elementExists("content/csum[@base='10']")) {
            setChecksum(new Checksum(Checksum.Type.CRC32,
                    ae.longValue("csum[@base='10']")));
        } else {
            setChecksum(null);
        }

    }

}
