package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.user.User;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Provides information about the user's Gravatar.
 *
 * @author Georg Schmidl <georg.schmidl@vicox.net>
 */
@Path("/userinfo")
public class UserInfoResource {

    private UserAccessor userAccessor;

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserInfo() {
        UserInfo userInfo = new UserInfo();

        User user = AuthenticatedUserThreadLocal.getUser();
        if (user.getEmail() != null && !StringUtils.isBlank(user.getEmail())) {
            userInfo.setUrl(UrlUtil.getRedirectUrlFromEmail(user.getEmail()));
        }
        userInfo.setUsing(UrlUtil.isRedirectUrl(userAccessor.getUserProfilePicture(user).getDownloadPath()));

        return Response.ok(userInfo).build();
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public class UserInfo {

        @XmlElement()
        private String url;

        @XmlElement()
        private boolean using;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isUsing() {
            return using;
        }

        public void setUsing(boolean using) {
            this.using = using;
        }
    }
}
