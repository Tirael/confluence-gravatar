package net.vicox.confluence.plugins.gravatar;

import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.core.task.Task;
import com.atlassian.core.task.TaskQueue;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import net.vicox.confluence.plugins.gravatar.service.GravatarImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Listens for the login event and creates {@link GravatarImportTask}s.
 *
 * @author Georg Schmidl
 */
public class GravatarImportListener implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GravatarImportListener.class);

    private final EventPublisher eventPublisher;
    private final MultiQueueTaskManager multiQueueTaskManager;
    private final GravatarImportService gravatarImportService;
    private final UserAccessor userAccessor;

    public GravatarImportListener(EventPublisher eventPublisher,
                                  MultiQueueTaskManager multiQueueTaskManager,
                                  GravatarImportService gravatarImportService,
                                  UserAccessor userAccessor) {
        this.eventPublisher = eventPublisher;
        this.multiQueueTaskManager = multiQueueTaskManager;
        this.gravatarImportService = gravatarImportService;
        this.userAccessor = userAccessor;
        eventPublisher.register(this);
    }

    @EventListener
    public void loginEvent(LoginEvent event) {
        log.debug("adding gravatar import task for user {}", event.getUsername());
        Task task = new GravatarImportTask(gravatarImportService, userAccessor, event.getUsername());

        TaskQueue taskQueue = multiQueueTaskManager.getTaskQueue("task");
        taskQueue.addTask(task);
    }

    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }
}
