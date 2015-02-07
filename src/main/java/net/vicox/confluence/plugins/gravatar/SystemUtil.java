package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.user.service.SetProfilePictureFromImageCommandImpl;

import java.lang.annotation.Annotation;

/**
 * @author Georg Schmidl
 */
public class SystemUtil {

    public static boolean profilePictureCommandIsDeprecated() {
        boolean isDeprecated = false;

        for (Annotation annotation : SetProfilePictureFromImageCommandImpl.class.getDeclaredAnnotations()) {
            if (annotation.annotationType().equals(Deprecated.class)) {
                isDeprecated = true;
            }
        }

        return isDeprecated;
    }
}
