package net.vicox.confluence.plugins.gravatar.service;

import com.atlassian.user.User;

import java.io.IOException;
import java.util.Date;

/**
 * Imports the user's Gravatar picture.
 *
 * @author Georg Schmidl
 */
public interface GravatarImportService {

    /**
     * Imports the Gravatar image for a given user.
     *
     * @param user the user
     * @throws IOException
     */
    void importGravatar(User user) throws IOException;

    /**
     * Removes the Gravatar image for a given user.
     *
     * @param user the user
     */
    void removeGravatar(User user);

    /**
     * Returns the imported pictures URL for a given user.
     *
     * @param user the user
     * @return the picture URL
     */
    String getImportedPicturePath(User user);

    /**
     * Returns the date when the Gravatar image was last imported for a given user.
     *
     * @param user the user
     * @return the date of the last import
     */
    Date getLastImportedDate(User user);
}
