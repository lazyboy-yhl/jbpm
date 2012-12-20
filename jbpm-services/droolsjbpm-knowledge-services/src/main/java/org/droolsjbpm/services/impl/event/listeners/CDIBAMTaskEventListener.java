/*
 * Copyright 2012 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droolsjbpm.services.impl.event.listeners;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.droolsjbpm.services.impl.model.BAMTaskSummary;
import org.jboss.seam.transaction.Transactional;
import org.jbpm.task.Task;
import org.jbpm.task.TaskEvent;
import org.jbpm.task.events.AfterTaskActivatedEvent;
import org.jbpm.task.events.AfterTaskAddedEvent;
import org.jbpm.task.events.AfterTaskClaimedEvent;
import org.jbpm.task.events.AfterTaskCompletedEvent;
import org.jbpm.task.events.AfterTaskExitedEvent;
import org.jbpm.task.events.AfterTaskFailedEvent;
import org.jbpm.task.events.AfterTaskStartedEvent;
import org.jbpm.task.events.AfterTaskStoppedEvent;
import org.jbpm.task.lifecycle.listeners.TaskLifeCycleEventListener;

/**
 *
 * @author salaboy
 */
@ApplicationScoped
@Transactional
@BAM
public class CDIBAMTaskEventListener implements TaskLifeCycleEventListener{

   
    @Inject 
    private EntityManager em;
    
    public CDIBAMTaskEventListener() {
        
    }
    @Transactional
    public void afterTaskStartedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskStartedEvent Task ti) {
        BAMTaskSummary taskSummaryById = (BAMTaskSummary)em.createQuery("select bts from BAMTaskSummary bts where bts.taskId=:taskId")
                                                    .setParameter("taskId", ti.getId()).getSingleResult();
        taskSummaryById.setStatus("Started");
        taskSummaryById.setStartDate(new Date());
        if(ti.getTaskData().getActualOwner() != null){
            taskSummaryById.setUserId(ti.getTaskData().getActualOwner().getId());
        }
        em.merge(taskSummaryById);
        
    }

    public void afterTaskActivatedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskActivatedEvent Task ti) {
        
    }

    public void afterTaskClaimedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskClaimedEvent Task ti) {
       BAMTaskSummary taskSummaryById = (BAMTaskSummary)em.createQuery("select bts from BAMTaskSummary bts where bts.taskId=:taskId")
                                                    .setParameter("taskId", ti.getId()).getSingleResult();
        taskSummaryById.setStatus("Claimed");
        taskSummaryById.setEndDate(new Date());
        if(ti.getTaskData().getActualOwner() != null){
            taskSummaryById.setUserId(ti.getTaskData().getActualOwner().getId());
        }
        em.merge(taskSummaryById); 
    }

    public void afterTaskSkippedEvent(Task ti) {
        
    }

    public void afterTaskStoppedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskStoppedEvent Task ti ) {
        
    }

    public void afterTaskCompletedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskCompletedEvent Task ti) {
        
        BAMTaskSummary taskSummaryById = (BAMTaskSummary)em.createQuery("select bts from BAMTaskSummary bts where bts.taskId=:taskId")
                                                    .setParameter("taskId", ti.getId()).getSingleResult();
        taskSummaryById.setStatus("Completed");
        Date completedDate = new Date();
        taskSummaryById.setEndDate(completedDate);
        taskSummaryById.setDuration(completedDate.getTime() - taskSummaryById.getStartDate().getTime());
        em.merge(taskSummaryById);
    }

    public void afterTaskFailedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskFailedEvent Task ti) {
        
    }

    public void afterTaskAddedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskAddedEvent Task ti) {
        String actualOwner = "";
        if(ti.getTaskData().getActualOwner()!= null){
            actualOwner = ti.getTaskData().getActualOwner().getId();
        }

        em.persist(new BAMTaskSummary(ti.getId(), ti.getNames().get(0).getText(), "Created", new Date(),  actualOwner, ti.getTaskData().getProcessInstanceId()));
        
    }

    public void afterTaskExitedEvent(@Observes(notifyObserver= Reception.ALWAYS) @AfterTaskExitedEvent Task ti) {
        
    }
    
}
