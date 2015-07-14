package net.vicox.confluence.plugins.gravatar.task;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.task.Task;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.user.User;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Imports the user's gravatar if it hasn't been updated in 24 hours.
 *
 * @author Georg Schmidl
 */
public class GravatarImportTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(GravatarImportTask.class);

    private final GravatarImportService gravatarImportService;
    private final UserAccessor userAccessor;
    private final TransactionTemplate transactionTemplate;

    private final String username;

    public GravatarImportTask(GravatarImportService gravatarImportService, UserAccessor userAccessor, TransactionTemplate transactionTemplate, String username) {
        this.gravatarImportService = gravatarImportService;
        this.userAccessor = userAccessor;
        this.transactionTemplate = transactionTemplate;
        this.username = username;
    }

    @Override
    public void execute() throws Exception {
        transactionTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction() {
                log.debug("attempting to update gravatar for user '{}'", username);

                User user = userAccessor.getUserByName(username);
                if (user != null) {
                    if (gravatarImportService.usesGravatar(user)) {
                        log.debug("user '{}' is using gravatar", username);

                        Date lastUpdated = gravatarImportService.getLastImportedDate(user);

                        if (lastUpdated != null && new Date().getTime() - (24 * 60 * 60 * 1000) > lastUpdated.getTime()) {
                            log.debug("importing gravatar last updated '{}' for user '{}'", lastUpdated, username);

                            try {
                                gravatarImportService.importGravatar(user);
                            } catch (IOException e) {
                                throw new RuntimeException("error importing gravatar", e);
                            }

                        } else {
                            log.debug("skipping import of gravatar last updated '{}' for user '{}'", lastUpdated, username);
                        }
                    } else {
                        log.debug("user '{}' is not using gravatar", username);
                    }
                }
                return null;
            }
        });
    }
}
