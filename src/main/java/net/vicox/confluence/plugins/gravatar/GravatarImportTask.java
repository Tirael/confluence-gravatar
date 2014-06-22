package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.task.Task;
import com.atlassian.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Imports the user's gravatar if it hasn't been updated in 24 hours.
 *
 * @author Georg Schmidl
 */
public class GravatarImportTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(GravatarImportTask.class);

    private final GravatarImporter gravatarImporter;
    private final UserAccessor userAccessor;

    private final String username;

    public GravatarImportTask(GravatarImporter gravatarImporter, UserAccessor userAccessor, String username) {
        this.gravatarImporter = gravatarImporter;
        this.userAccessor = userAccessor;
        this.username = username;
    }

    @Override
    public void execute() throws Exception {
        log.debug("attempting to update gravatar for user {}", username);

        User user = userAccessor.getUserByName(username);
        if (user != null) {
            Date lastUpdated = gravatarImporter.getLastImportedDate(user);

            if (lastUpdated != null && new Date().getTime() - (24 * 60 * 60 * 1000) > lastUpdated.getTime()) {
                gravatarImporter.importGravatar(user);
            }
        }
    }
}
