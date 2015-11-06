package hacktm.haufe.initial;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * A utility class which provides helper methods for encoding strings.
 *
 * @author Oliver Weiler (weiler@predic8.de)
 */
public class EncodingUtils {
    /**
     * Base64 encodes the given string.
     *
     * @param text the string to encode
     * @return the encoded string
     * @throws java.io.UnsupportedEncodingException if the encoding isnt't supported
     */
    public static String encodeBase64(String text) throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(text.getBytes("UTF-8")), "UTF-8");
    }
}