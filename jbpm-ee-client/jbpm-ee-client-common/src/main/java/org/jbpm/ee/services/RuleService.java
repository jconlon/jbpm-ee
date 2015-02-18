package org.jbpm.ee.services;

import java.util.Collection;
import org.jbpm.ee.services.ejb.annotations.LazilyDeserialized;
import org.jbpm.ee.services.ejb.annotations.PreprocessClassloader;
import org.jbpm.ee.services.ejb.annotations.ProcessInstanceId;
import org.jbpm.ee.services.model.rules.FactHandle;


/**
 * 
 * @author bdavis, abaxter
 * 
 *
 * For inserting facts and firing rules.
 * 
 * Note: fireAllRules must be run before any rules will be
 * executed, including rules in ruleflow-groups
 *
 */
public interface RuleService {


	/**
	 * Set rules to fire for a particular process 
	 * 
	 * @param processInstanceId The process instance's unique identifier
	 * @return The number of rules fired
	 */
	int fireAllRules(@ProcessInstanceId Long processInstanceId);
	

    /**
     * Set up to max rules to fire for a particular process 
     * 
     * @param processInstanceId The process instance's unique identifier
     * @param max The maximum number of rules to fire
     * @return The number of rules fired
     */
	int fireAllRules(@ProcessInstanceId Long processInstanceId, int max);
	

    /**
     * Insert a fact into the process's rule runtime
     * 
     * @param processInstanceId The process instance's unique identifier
     * @param object The fact to be inserted
     */
	@PreprocessClassloader
    FactHandle insert(@ProcessInstanceId Long processInstanceId, @LazilyDeserialized Object object);

    /**
     * Delete a fact from the process's rule runtime
     * 
     * @param processInstanceId The process instance's unique identifier
     * @param object The fact to be inserted
     */
	void delete(@ProcessInstanceId Long processInstanceId, FactHandle factHandle);
	
    /**
     * Retrieve a fact from the process's rule runtime
     * 
     * @param processInstanceId The process instance's unique identifier
     * @param object The fact to be inserted
     */
	Object getObject(@ProcessInstanceId Long processInstanceId, FactHandle factHandle);
	
    /**
     * Retrieve all facts from the process's rule runtime
     * 
     * @param processInstanceId The process instance's unique identifier
     * @param object The fact to be inserted
     */
	Collection<? extends Object> getObjects(@ProcessInstanceId Long processInstanceId);
}
