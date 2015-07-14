package net.vicox.confluence.plugins.gravatar.listener;

import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.core.task.Task;
import com.atlassian.core.task.TaskQueue;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
import net.vicox.confluence.plugins.gravatar.task.GravatarImportTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Listens for the login event and creates {@link net.vicox.confluence.plugins.gravatar.task.GravatarImportTask}s.
 *
 * @author Georg Schmidl
 */
public class GravatarImportListener implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GravatarImportListener.class);

    private final EventPublisher eventPublisher;
    private final MultiQueueTaskManager multiQueueTaskManager;
    private final GravatarImportService gravatarImportService;
    private final UserAccessor userAccessor;
    private final TransactionTemplate transactionTemplate;

    public GravatarImportListener(EventPublisher eventPublisher,
                                  MultiQueueTaskManager multiQueueTaskManager,
                                  GravatarImportService gravatarImportService,
                                  UserAccessor userAccessor, TransactionTemplate transactionTemplate) {
        this.eventPublisher = eventPublisher;
        this.multiQueueTaskManager = multiQueueTaskManager;
        this.gravatarImportService = gravatarImportService;
        this.userAccessor = userAccessor;
        this.transactionTemplate = transactionTemplate;
        eventPublisher.register(this);
    }

    @EventListener
    public void loginEvent(LoginEvent event) {
        log.debug("adding gravatar import task for user {}", event.getUsername());
        Task task = new GravatarImportTask(gravatarImportService, userAccessor, transactionTemplate, event.getUsername());

        TaskQueue taskQueue = multiQueueTaskManager.getTaskQueue("task");
        taskQueue.addTask(task);
    }

    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }
}
