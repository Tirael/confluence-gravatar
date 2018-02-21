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

    public static final int IMAGE_SIZE = SystemUtil.profilePictureCommandIsDeprecated() ? 256 : 48;

    public static String getGravatarUrlFromMd5(String md5) {
        return "https://www.gravatar.com/avatar/" + md5 + ".png?s=" + IMAGE_SIZE;
    }

    public static String getGravatarUrlFromEmail(String email) {
        return getGravatarUrlFromMd5(DigestUtils.md5Hex(email.trim().toLowerCase()));
    }

    public static byte[] loadGravatarImage(String email) throws IOException {
        InputStream is = new URL(GravatarUtil.getGravatarUrlFromEmail(email)).openStream();
        return IOUtils.toByteArray(is);
    }
}
