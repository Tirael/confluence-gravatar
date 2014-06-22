package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.User;

/**
 * Action for importing the user's Gravatar picture.
 *
 * @author Georg Schmidl
 */
public class GravatarImportAction extends ConfluenceActionSupport {

    private GravatarImporter gravatarImporter;

    private String delete;

    @Override
    public String execute() throws Exception {
        User user = AuthenticatedUserThreadLocal.get();

        if (delete == null) {
            gravatarImporter.importGravatar(user);

        } else {
            gravatarImporter.removeGravatar(user);
        }
        return SUCCESS;
    }


    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public void setGravatarImporter(GravatarImporter gravatarImporter) {
        this.gravatarImporter = gravatarImporter;
    }
}
