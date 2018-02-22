package net.vicox.confluence.plugins.gravatar.rest;

import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
import net.vicox.confluence.plugins.gravatar.service.GravatarSettingsService;
import net.vicox.confluence.plugins.gravatar.util.GravatarUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 * Resource for getting {@link net.vicox.confluence.plugins.gravatar.rest.GravatarInfo}s.
 *
 * @author Georg Schmidl
 */
@Path("/")
public class GravatarResource {

    private GravatarImportService gravatarImportService;
    private GravatarSettingsService gravatarSettingsService;
    private PermissionManager permissionManager;

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
        if (StringUtils.isNotBlank(user.getEmail())) {
            String gravatarServerUrl = gravatarSettingsService.getSettings().get("gravatarServerUrl");
            String gravatarUrl = GravatarUtil.getGravatarUrlFromEmail(gravatarServerUrl, user.getEmail());
            gravatarInfo.setGravatarUrl(gravatarUrl);
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

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("settings")
    public Response getSettings(@Context HttpServletRequest httpServletRequest) {
        if (currentUserIsNotAdministrator()) return Response.status(404).build();
        Map<String, String> settings = gravatarSettingsService.getSettings();
        return Response.ok(settings).build();
    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @Path("settings")
    public Response setSettings(@Context HttpServletRequest httpServletRequest, Map<String, String> newSettings) {
        if (currentUserIsNotAdministrator()) return Response.status(404).build();
        gravatarSettingsService.setSettings(newSettings);
        Map<String, String> settings = gravatarSettingsService.getSettings();
        return Response.ok(settings).build();
    }

    private boolean currentUserIsNotAdministrator() {
        ConfluenceUser user = AuthenticatedUserThreadLocal.get();
        return user == null || !permissionManager.hasPermission(user, Permission.ADMINISTER, PermissionManager.TARGET_SYSTEM);
    }

    public void setGravatarImportService(GravatarImportService gravatarImportService) {
        this.gravatarImportService = gravatarImportService;
    }

    public void setGravatarSettingsService(GravatarSettingsService gravatarSettingsService) {
        this.gravatarSettingsService = gravatarSettingsService;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
}
