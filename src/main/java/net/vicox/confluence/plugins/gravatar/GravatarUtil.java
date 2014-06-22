package net.vicox.confluence.plugins.gravatar;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Gravatar URL and image util functions.
 *
 * @author Georg Schmidl
 */
public class GravatarUtil {

    private static final int ICON_SIZE = 48;

    public static String getGravatarUrlFromMd5(String md5, boolean secure) {
        return (secure ? "https://secure" : "http://www") + ".gravatar.com/avatar/" + md5 + ".png?s=" + ICON_SIZE;
    }

    public static String getGravatarUrlFromEmail(String email, boolean secure) {
        return getGravatarUrlFromMd5(DigestUtils.md5Hex(email), secure);
    }

    public static byte[] loadGravatarImage(String email) throws IOException {
        InputStream is = new URL(GravatarUtil.getGravatarUrlFromEmail(email, false)).openStream();
        return IOUtils.toByteArray(is);
    }
}
