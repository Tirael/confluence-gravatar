package net.vicox.confluence.plugins.gravatar;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Gravatar URL util functions.
 *
 * @author Georg Schmidl <georg.schmidl@vicox.net>
 */
public class UrlUtil {

    private static final int ICON_SIZE = 48;
    private static final String GRAVATAR_PATH = "/images/icons/profilepics/gravatar.png";

    public static String getGravatarUrlFromMd5(String md5) {
        return "http://www.gravatar.com/avatar/" + md5 + ".png?s=" + ICON_SIZE;
    }

    public static String getGravatarUrlFromEmail(String email) {
        return getGravatarUrlFromMd5(DigestUtils.md5Hex(email));
    }

    public static String getRedirectUrlFromMd5(String md5) {
        return GRAVATAR_PATH + "?md5=" + md5;
    }

    public static String getRedirectUrlFromEmail(String email) {
        return getRedirectUrlFromMd5(DigestUtils.md5Hex(email));
    }

    public static boolean isRedirectUrl(String url) {
        return url.startsWith(GRAVATAR_PATH);
    }
}
