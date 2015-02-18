package org.jbpm.ee.services.ejb.impl;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.jboss.ejb3.annotation.Clustered;
import org.jbpm.ee.exception.InactiveProcessInstance;
import org.jbpm.ee.services.ProcessService;
import org.jbpm.ee.services.ejb.impl.interceptors.JBPMContextEJBBinding;
import org.jbpm.ee.services.ejb.impl.interceptors.JBPMContextEJBInterceptor;
import org.jbpm.ee.services.ejb.local.ProcessServiceLocal;
import org.jbpm.ee.services.ejb.remote.ProcessServiceRemote;
import org.jbpm.ee.services.ejb.startup.KnowledgeManagerBean;
import org.jbpm.ee.services.model.KieReleaseId;
import org.jbpm.ee.services.model.ProcessInstanceFactory;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Provides a wrapper implementation for the CDI services in order to support consistent execution by Local, Remote, REST, and SOAP execution.
 * 
 * {@see ProcessService}
 * 
 * @author bradsdavis
 *
 */
@JBPMContextEJBBinding
@Interceptors({JBPMContextEJBInterceptor.class})
@Clustered
@Stateless
public class ProcessServiceBean implements ProcessService, ProcessServiceLocal, ProcessServiceRemote {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProcessServiceBean.class);

	@EJB
	private KnowledgeManagerBean knowledgeManager;

	private KieSession getSessionByProcess(Long processInstanceId) {
		return knowledgeManager.getRuntimeEngineByProcessId(processInstanceId).getKieSession();
	}
	
	private KieSession getNewSession(KieReleaseId releaseId) {
		return knowledgeManager.getRuntimeEngine(releaseId).getKieSession();
	}
	
	@Override
	public ProcessInstance startProcess(KieReleaseId releaseId, String processId) {
		KieSession session = getNewSession(releaseId);
		
		return ProcessInstanceFactory.convert(session.startProcess(processId));
	}

	@Override
	public ProcessInstance startProcess(KieReleaseId releaseId, String processId, Map<String, Object> parameters) {
		KieSession session = getNewSession(releaseId);
		return ProcessInstanceFactory.convert(session.startProcess(processId, parameters));
	}

	@Override
	public void signalEvent(long processInstanceId, String type, Object event) {
		getSessionByProcess(processInstanceId).signalEvent(type, event);
	}

	@Override
	public ProcessInstance getProcessInstance(long processInstanceId) {
		try {
			return ProcessInstanceFactory.convert(getSessionByProcess(processInstanceId).getProcessInstance(processInstanceId, true));
		} 
		catch(InactiveProcessInstance e) {
			return null;
		}
	}

	@Override
	public void abortProcessInstance(final long processInstanceId) {
		final KieSession kieSession = getSessionByProcess(processInstanceId);
		kieSession.abortProcessInstance(processInstanceId);
	}

	@Override
	public void setProcessInstanceVariable(long processInstanceId, String variableName, Object variable) {
		WorkflowProcessInstance pi = (WorkflowProcessInstance) getSessionByProcess(processInstanceId).getProcessInstance(processInstanceId);
		pi.setVariable(variableName, variable);
	}

	@Override
	public Object getProcessInstanceVariable(long processInstanceId, String variableName) {
		WorkflowProcessInstance pi = (WorkflowProcessInstance) getSessionByProcess(processInstanceId).getProcessInstance(processInstanceId);
		return pi.getVariable(variableName);
	}

	@Override
	public Map<String, Object> getProcessInstanceVariables(long processInstanceId) {
		WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) getSessionByProcess(processInstanceId).getProcessInstance(processInstanceId);
		LOG.debug("Process variable size: "+pi.getVariables().size());
		return pi.getVariables();
	}


	@Override
	public KieReleaseId getReleaseId(long processInstanceId) {
		return knowledgeManager.getReleaseIdByProcessId(processInstanceId);
	}
	
}
