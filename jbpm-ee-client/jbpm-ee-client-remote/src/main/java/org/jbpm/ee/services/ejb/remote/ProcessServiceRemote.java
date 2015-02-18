package org.jbpm.ee.services.ejb.remote;

import javax.ejb.Remote;

import org.jbpm.ee.services.ProcessService;

/**
 * Remote EJB interface for ProcessService.
 * 
 * @author bdavis, abaxter
 *
 */
@Remote
public interface ProcessServiceRemote extends ProcessService {
	
}
