/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.utils.TaskIdGenerator;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_INSTANCE_ID;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_INSTANCE_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_JOB_STORE_CLASS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_JOB_STORE_CLASS_VALUE;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_THREAD_COUNT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_THREAD_COUNT_VALUE;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_THREAD_POOL_CLASS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_THREAD_POOL_CLASS_VALUE;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_OBJECT;

/**
 * Abstract class which represents a ballerina task.
 *
 * @since 0.995.0
 */
public abstract class AbstractTask implements Task {

    protected String id = TaskIdGenerator.generate();
    private HashMap<String, ServiceInformation> serviceMap;
    Map<String, JobKey> quartzJobs = new HashMap<>();
    long maxRuns;
    Scheduler scheduler;

    /**
     * Constructor to create a task without a limited (maximum) number of runs.
     */
    AbstractTask() throws SchedulingException {
        this.serviceMap = new HashMap<>();
        this.maxRuns = -1;
        try {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory(createSchedulerProperties());
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot initialize the Task Listener/Scheduler.", e);
        }
    }

    /**
     * Constructor to create a task with limited (maximum) number of runs.
     *
     * @param maxRuns Maximum number of runs allowed.
     */
    AbstractTask(long maxRuns) throws SchedulingException {
        validateMaxRuns(maxRuns);
        this.serviceMap = new HashMap<>();
        this.maxRuns = maxRuns;
        try {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory(createSchedulerProperties());
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot initialize the Task Listener/Scheduler.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addService(ServiceInformation service) { 
        this.serviceMap.put(service.getService().getType().getName(), service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeService(String serviceName) {
        this.serviceMap.remove(serviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, ServiceInformation> getServicesMap() {
        return this.serviceMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInformation getService(String serviceName) {
        return this.serviceMap.get(serviceName);
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Create a job data map using the context and the service.
     *
     * @return JobDataMap consists of context and the <code>ServiceWithParameter</code> object.
     */
    JobDataMap getJobDataMapFromTask() {
        JobDataMap jobData = new JobDataMap();
        jobData.put(TASK_OBJECT, this);
        return jobData;
    }

    private void validateMaxRuns(long maxRuns) throws SchedulingException {
        if (maxRuns < 1) {
            throw new SchedulingException("Task noOfOccurrences should be a positive integer.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws SchedulingException {
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new SchedulingException("Failed to stop the task.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pause() throws SchedulingException {
        try {
            this.scheduler.pauseAll();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot pause the task.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resume() throws SchedulingException {
        try {
            this.scheduler.resumeAll();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot resume the task.", e);
        }
    }

    private Properties createSchedulerProperties() {
        Properties properties = new Properties();
        properties.setProperty(QUARTZ_INSTANCE_NAME, this.id);
        properties.setProperty(QUARTZ_INSTANCE_ID, this.id);
        properties.setProperty(QUARTZ_THREAD_COUNT, QUARTZ_THREAD_COUNT_VALUE);
        properties.setProperty(QUARTZ_THREAD_POOL_CLASS, QUARTZ_THREAD_POOL_CLASS_VALUE);
        properties.setProperty(QUARTZ_JOB_STORE_CLASS, QUARTZ_JOB_STORE_CLASS_VALUE);
        return properties;
    }
}
