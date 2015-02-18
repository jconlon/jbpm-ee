package org.jbpm.ee.services.ejb.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.jboss.ejb3.annotation.Clustered;
import org.jbpm.ee.services.TaskService;
import org.jbpm.ee.services.ejb.impl.interceptors.JBPMContextEJBBinding;
import org.jbpm.ee.services.ejb.impl.interceptors.JBPMContextEJBInterceptor;
import org.jbpm.ee.services.ejb.local.TaskServiceLocal;
import org.jbpm.ee.services.ejb.remote.TaskServiceRemote;
import org.jbpm.ee.services.ejb.startup.KnowledgeManagerBean;
import org.jbpm.ee.services.model.KieReleaseId;
import org.jbpm.ee.services.model.TaskFactory;
import org.jbpm.ee.services.model.adapter.ClassloaderManager;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;


/***
 * Provides a wrapper implementation for the CDI services in order to support consistent execution by Local, Remote, REST, and SOAP execution.
 * 
 * {@see TaskService}
 * 
 * @author bradsdavis
 *
 */
@JBPMContextEJBBinding
@Interceptors({JBPMContextEJBInterceptor.class})
@Clustered
@Stateless
public class TaskServiceBean implements TaskService, TaskServiceLocal, TaskServiceRemote {

	@EJB
	private KnowledgeManagerBean knowledgeManager;

	@Inject
	private org.kie.api.task.TaskService taskService;	
	
	private org.kie.api.task.TaskService getTaskServiceByTask(long taskId) {
		return knowledgeManager.getRuntimeEngineByTaskId(taskId).getTaskService();
	}
	
	private org.kie.api.task.TaskService getTaskServiceByContent(long contentId) {
		return knowledgeManager.getRuntimeEngineByContentId(contentId).getTaskService();
	}
	@Override
	public void activate(long taskId, String userId) {
		getTaskServiceByTask(taskId).activate(taskId, userId);
	}

	@Override
	public void claim(long taskId, String userId) {
		getTaskServiceByTask(taskId).claim(taskId, userId);
	}

	@Override
	public void claimNextAvailable(String userId, String language) {
		List<TaskSummary> tasks = taskService.getTasksOwned(userId, language);
		if(tasks.isEmpty()) {
			return;
		}
		
		
		TreeSet<TaskSummary> orderedTasks = new TreeSet<TaskSummary>(new Comparator<TaskSummary>() {
			@Override
			public int compare(TaskSummary t1, TaskSummary t2) {
				if(t1.getExpirationTime() == null && t2.getExpirationTime() == null) {
					//check priority.
					Integer p1 = t1.getPriority();
					Integer p2 = t2.getPriority();
					
					return p1.compareTo(p2);
				}
				
				if(t1.getExpirationTime() != null && t2.getExpirationTime() != null) {
					return t1.getExpirationTime().compareTo(t2.getExpirationTime());
				}
				
				if(t1.getExpirationTime() == null && t2.getExpirationTime() != null) {
					return 1;
				}
				
				if(t1.getExpirationTime() != null && t2.getExpirationTime() == null) {
					return -1;
				}
				
				return 0;
			}
		});
		orderedTasks.addAll(tasks);
		
		this.claim(orderedTasks.first().getId(), userId);
	}

	@Override
	public void complete(long taskId, String userId, Map<String, Object> data) {
		getTaskServiceByTask(taskId).complete(taskId, userId, data);
	}

	@Override
	public void delegate(long taskId, String userId, String targetUserId) {
		getTaskServiceByTask(taskId).delegate(taskId, userId, targetUserId);
	}

	@Override
	public void exit(long taskId, String userId) {
		getTaskServiceByTask(taskId).exit(taskId, userId);
	}

	@Override
	public void fail(long taskId, String userId, Map<String, Object> faultData) {
		getTaskServiceByTask(taskId).fail(taskId, userId, faultData);
	}

	@Override
	public void forward(long taskId, String userId, String targetEntityId) {
		getTaskServiceByTask(taskId).forward(taskId, userId, targetEntityId);
	}

	@Override
	public Task getTaskByWorkItemId(long workItemId) {
		return TaskFactory.convert(taskService.getTaskByWorkItemId(workItemId));
	}

	@Override
	public Task getTaskById(long taskId) {
		return TaskFactory.convert(getTaskServiceByTask(taskId).getTaskById(taskId));
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String userId, String language) {
		return TaskFactory.convertTaskSummaries(taskService.getTasksAssignedAsBusinessAdministrator(userId, language));
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, String language) {
		return TaskFactory.convertTaskSummaries(taskService.getTasksAssignedAsPotentialOwner(userId, language));
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatus(String userId, List<Status> status, String language) {
		return TaskFactory.convertTaskSummaries(taskService.getTasksAssignedAsPotentialOwnerByStatus(userId, status, language));
	}

	@Override
	public List<TaskSummary> getTasksOwned(String userId, String language) {
		return TaskFactory.convertTaskSummaries(taskService.getTasksOwned(userId, language));
	}

	@Override
	public List<TaskSummary> getTasksOwnedByStatus(String userId, List<Status> status, String language) {
		return TaskFactory.convertTaskSummaries(taskService.getTasksOwnedByStatus(userId, status, language));
	}

	@Override
	public List<TaskSummary> getTasksByStatusByProcessInstanceId(long processInstanceId, List<Status> status, String language) {
		return TaskFactory.convertTaskSummaries(taskService.getTasksByStatusByProcessInstanceId(processInstanceId, status, language));
	}

	@Override
	public List<Long> getTasksByProcessInstanceId(long processInstanceId) {
		return taskService.getTasksByProcessInstanceId(processInstanceId);
	}


	@Override
	public void release(long taskId, String userId) {
		getTaskServiceByTask(taskId).release(taskId, userId);
	}

	@Override
	public void resume(long taskId, String userId) {
		getTaskServiceByTask(taskId).resume(taskId, userId);
	}

	@Override
	public void skip(long taskId, String userId) {
		getTaskServiceByTask(taskId).skip(taskId, userId);
	}

	@Override
	public void start(long taskId, String userId) {
		getTaskServiceByTask(taskId).start(taskId, userId);
	}

	@Override
	public void stop(long taskId, String userId) {
		getTaskServiceByTask(taskId).stop(taskId, userId);
	}

	@Override
	public void suspend(long taskId, String userId) {
		getTaskServiceByTask(taskId).suspend(taskId, userId);
	}

	@Override
	public void nominate(long taskId, String userId, List<OrganizationalEntity> potentialOwners) {
		getTaskServiceByTask(taskId).nominate(taskId, userId, potentialOwners);
	}

	@Override
	public Content getContentById(long contentId) {
		return TaskFactory.convert(getTaskServiceByContent(contentId).getContentById(contentId), ClassloaderManager.get());
	}

	@Override
	public Attachment getAttachmentById(long attachId) {
		return TaskFactory.convert(taskService.getAttachmentById(attachId));
	}
	
	@Override
	public KieReleaseId getReleaseId(long taskId) {
		return knowledgeManager.getReleaseIdByTaskId(taskId);
	}
	
}
