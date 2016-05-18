package vicnode.mf.client;

import java.io.File;
import java.io.PrintStream;

import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import vicnode.mf.client.util.ChecksumUtils;
import vicnode.mf.client.util.PathUtils;

public class Result {

    private String _baseNamespace;
    private File _baseDirectory;

    private String _assetId;
    private String _assetNamespace;
    private String _assetName;
    private String _assetPath;
    private Long _assetContentSize;
    private Long _assetContentChecksum;

    private String _relativePath;

    private File _file;
    private Long _fileChecksum;

    private boolean _noCsumCheck;

    Result(XmlDoc.Element ae, String baseNamespace, File baseDirectory,
            boolean noCsumCheck) throws Throwable {
        _baseNamespace = baseNamespace;
        _baseDirectory = baseDirectory;
        _noCsumCheck = noCsumCheck;

        _assetId = ae.value("@id");
        _assetNamespace = ae.value("namespace");
        _assetName = ae.value("name");
        _assetPath = _assetNamespace + "/" + (_assetName == null
                ? ("__asset_id__" + _assetId) : _assetName);
        _assetContentSize = ae.longValue("size", null);
        _assetContentChecksum = ae.longValue("csum", 0, 16);
        if (_assetContentChecksum == 0) {
            _assetContentChecksum = null;
        }
        _relativePath = PathUtils.extractRelativePath(_assetPath,
                _baseNamespace);
        _file = new File(PathUtils.joinPaths(baseDirectory.getAbsolutePath(),
                _relativePath));
        if (!_file.exists()) {
            _file = null;
        }
        if (_file != null && !noCsumCheck) {
            _fileChecksum = ChecksumUtils.crc32(_file);
        }
    }

    Result(File file, Connection cxn, File baseDirectory, String baseNamespace,
            boolean noCsumCheck) throws Throwable {
        _baseNamespace = baseNamespace;
        _baseDirectory = baseDirectory;
        _noCsumCheck = noCsumCheck;

        _file = file;
        if (!noCsumCheck) {
            _fileChecksum = ChecksumUtils.crc32(_file);
        }
        _relativePath = PathUtils.extractRelativePath(file, baseDirectory);
        _assetPath = PathUtils.joinPaths(_baseNamespace, _relativePath);

        XmlDoc.Element ae = cxn.execute("asset.get",
                "<id>path=" + _assetPath + "</id>", null, null)
                .element("asset");
        if (ae != null) {
            _assetId = ae.value("@id");
            _assetNamespace = ae.value("namespace");
            _assetName = ae.value("name");
            _assetContentSize = ae.longValue("content/size", null);
            _assetContentChecksum = ae.longValue("content/csum", 0, 16);
            if (_assetContentChecksum == 0) {
                _assetContentChecksum = null;
            }
        }
    }

    public String baseNamespace() {
        return _baseNamespace;
    }

    public File baseDirectory() {
        return _baseDirectory;
    }

    public String assetId() {
        return _assetId;
    }

    public String assetName() {
        return _assetName;
    }

    public String assetNamespace() {
        return _assetNamespace;
    }

    public String assetPath() {
        return _assetPath;
    }

    public Long assetContentSize() {
        return _assetContentSize;
    }

    public Long assetContentChecksum() {
        return _assetContentChecksum;
    }

    public String relativePath() {
        return _relativePath;
    }

    public File file() {
        return _file;
    }

    public String filePath() {
        if (_file != null) {
            return _file.getAbsolutePath();
        }
        return null;
    }

    public Long fileSize() {
        return _file == null ? null : _file.length();
    }

    public Long fileChecksum() {
        return _fileChecksum;
    }

    public boolean exists() {
        return _assetId != null && _file != null;
    }

    public boolean sizeMatch() {
        if (exists()) {
            return _file.length() == _assetContentSize.longValue();
        }
        return false;
    }

    public boolean checksumMatch() {
        if (sizeMatch() && _fileChecksum != null
                && _assetContentChecksum != null) {
            return _fileChecksum.longValue() == _assetContentChecksum
                    .longValue();
        }
        return false;
    }

    public boolean match() {
        if (_noCsumCheck) {
            return sizeMatch();
        } else {
            return checksumMatch();
        }
    }

    public void println(PrintStream out) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(_assetPath).append("\",");
        sb.append(_assetContentSize).append(",");
        if (!_noCsumCheck) {
            sb.append(_assetContentChecksum == null ? null
                    : Long.toHexString(_assetContentChecksum.longValue()))
                    .append(",");
        }
        sb.append("\"").append(filePath()).append("\",");
        sb.append(fileSize()).append(",");
        if (!_noCsumCheck) {
            sb.append(_fileChecksum == null ? null
                    : Long.toHexString(_fileChecksum.longValue())).append(",");
        }
        sb.append(sizeMatch() ? "Y" : "N").append(",");
        if (!_noCsumCheck) {
            sb.append(checksumMatch() ? "Y" : "N").append(",");
        }
        out.println(sb.toString());
    }
}
