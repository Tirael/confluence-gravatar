package net.vicox.confluence.plugins.gravatar.rest;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.util.GravatarUtil;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
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

    private GravatarImportService gravatarImportService;

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserInfo(@Context HttpServletRequest request) {
        GravatarInfo gravatarInfo = new GravatarInfo();

        User user = AuthenticatedUserThreadLocal.get();
        if (user.getEmail() != null && !StringUtils.isBlank(user.getEmail())) {
            gravatarInfo.setGravatarUrl(GravatarUtil.getGravatarUrlFromEmail(user.getEmail(), request.isSecure()));
        }

        String importedPicturePath = gravatarImportService.getImportedPicturePath(user);
        if (importedPicturePath != null) {
            gravatarInfo.setImportedPicturePath(importedPicturePath);
        }

        return Response.ok(gravatarInfo).build();
    }

    public void setGravatarImportService(GravatarImportService gravatarImportService) {
        this.gravatarImportService = gravatarImportService;
    }
}
