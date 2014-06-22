package net.vicox.confluence.plugins.gravatar;

import com.atlassian.user.User;

import java.io.IOException;
import java.util.Date;

/**
 * Imports the user's Gravatar picture.
 *
 * @author Georg Schmidl
 */
public interface GravatarImporter {

    void importGravatar(User user) throws IOException;

    void removeGravatar(User user);

    String getImportedPicturePath(User user);

    Date getLastImportedDate(User user);
}
