package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.User;

/**
 * Sets the user's profile picture to special URL
 * which gets redirected to the Gravatar one.
 *
 * @author Georg Schmidl <georg.schmidl@vicox.net>
 */
public class UseAction extends ConfluenceActionSupport {

    protected UserAccessor userAccessor;

    @Override
    public String execute() throws Exception {
        User user = AuthenticatedUserThreadLocal.getUser();
        userAccessor.setUserProfilePicture(user, UrlUtil.getRedirectUrlFromEmail(user.getEmail()));

        return SUCCESS;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }
}
