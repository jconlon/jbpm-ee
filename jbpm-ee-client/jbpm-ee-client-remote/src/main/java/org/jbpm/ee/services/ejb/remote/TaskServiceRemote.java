package org.jbpm.ee.services.ejb.remote;

import javax.ejb.Remote;

import org.jbpm.ee.services.TaskService;

/**
 * Remote EJB interface for TaskService.
 * 
 * @author bdavis, abaxter
 *
 */
@Remote
public interface TaskServiceRemote extends TaskService {

}
