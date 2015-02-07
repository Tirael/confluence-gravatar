package net.vicox.confluence.plugins.gravatar.rest;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
import net.vicox.confluence.plugins.gravatar.util.GravatarUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Resource for getting {@link net.vicox.confluence.plugins.gravatar.rest.GravatarInfo}s.
 *
 * @author Georg Schmidl
 */
@Path("/")
public class GravatarResource {

    private GravatarImportService gravatarImportService;

    /**
     * Returns the user's Gravatar URL and the imported profile picture URL.
     *
     * @param request the HTTP request
     * @return {@link net.vicox.confluence.plugins.gravatar.rest.GravatarInfo}
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("info")
    public Response getGravatarInfo(@Context HttpServletRequest request) {
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

    /**
     * Imports the user's Gravatar and returns the Gravatar URL and the imported profile picture URL.
     *
     * @param request the HTTP request
     * @return {@link net.vicox.confluence.plugins.gravatar.rest.GravatarInfo}
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("import")
    public Response importGravatar(@Context HttpServletRequest request) {
        User user = AuthenticatedUserThreadLocal.get();
        try {
            gravatarImportService.importGravatar(user);
            return getGravatarInfo(request);
        } catch (IOException e) {
            return Response.serverError().build();
        }
    }

    public void setGravatarImportService(GravatarImportService gravatarImportService) {
        this.gravatarImportService = gravatarImportService;
    }
}
