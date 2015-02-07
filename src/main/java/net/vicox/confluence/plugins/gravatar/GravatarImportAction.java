package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;

/**
 * Action for importing the user's Gravatar picture.
 *
 * @author Georg Schmidl
 */
public class GravatarImportAction extends ConfluenceActionSupport {

    private GravatarImportService gravatarImportService;

    private String delete;

    @Override
    public String execute() throws Exception {
        User user = AuthenticatedUserThreadLocal.get();

        if (delete == null) {
            gravatarImportService.importGravatar(user);

        } else {
            gravatarImportService.removeGravatar(user);
        }
        return SUCCESS;
    }


    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public void setGravatarImportService(GravatarImportService gravatarImportService) {
        this.gravatarImportService = gravatarImportService;
    }
}
