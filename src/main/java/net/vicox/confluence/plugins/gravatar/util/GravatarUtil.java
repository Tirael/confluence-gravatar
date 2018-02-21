package net.vicox.confluence.plugins.gravatar.util;

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

    public static final int IMAGE_SIZE = 256;

    public static String getGravatarUrlFromMd5(String gravatarServerUrl, String md5) {
        return gravatarServerUrl + md5 + ".png?s=" + IMAGE_SIZE;
    }

    public static String getGravatarUrlFromEmail(String gravatarServerUrl, String email) {
        return getGravatarUrlFromMd5(gravatarServerUrl, DigestUtils.md5Hex(email.trim().toLowerCase()));
    }

    public static byte[] loadGravatarImage(String gravatarServerUrl, String email) throws IOException {
        InputStream is = new URL(GravatarUtil.getGravatarUrlFromEmail(gravatarServerUrl, email)).openStream();
        return IOUtils.toByteArray(is);
    }
}
