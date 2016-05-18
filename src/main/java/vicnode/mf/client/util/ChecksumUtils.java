package vicnode.mf.client.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class ChecksumUtils {

    public static long crc32(InputStream in) throws Throwable {

        CheckedInputStream cis = new CheckedInputStream(in, new CRC32());
        try {
            while (cis.read() != -1) {
                // read the input stream completely.
            }
            return cis.getChecksum().getValue();
        } finally {
            cis.close();
            in.close();
        }
    }

    public static long crc32(File f) throws Throwable {
        return crc32(new BufferedInputStream(new FileInputStream(f)));
    }

    public static void main(String[] args) throws Throwable {
        System.out.println(crc32(new File("/tmp/t123/hs_err_pid15930.log")));
    }

}
