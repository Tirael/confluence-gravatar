package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.user.User;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for getting {@link GravatarInfo}s.
 *
 * @author Georg Schmidl
 */
@Path("/info")
public class GravatarInfoResource {

    private GravatarImporter gravatarImporter;

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserInfo(@Context HttpServletRequest request) {
        GravatarInfo gravatarInfo = new GravatarInfo();

        User user = AuthenticatedUserThreadLocal.get();
        if (user.getEmail() != null && !StringUtils.isBlank(user.getEmail())) {
            gravatarInfo.setGravatarUrl(GravatarUtil.getGravatarUrlFromEmail(user.getEmail(), request.isSecure()));
        }

        String importedPicturePath = gravatarImporter.getImportedPicturePath(user);
        if (importedPicturePath != null) {
            gravatarInfo.setImportedPicturePath(importedPicturePath);
        }

        return Response.ok(gravatarInfo).build();
    }

    public void setGravatarImporter(GravatarImporter gravatarImporter) {
        this.gravatarImporter = gravatarImporter;
    }
}
