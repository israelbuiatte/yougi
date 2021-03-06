/* Yougi is a web application conceived to manage user groups or
 * communities focused on a certain domain of knowledge, whose members are
 * constantly sharing information and participating in social and educational
 * events. Copyright (C) 2011 Hildeberto Mendonça.
 *
 * This application is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This application is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * There is a full copy of the GNU Lesser General Public License along with
 * this library. Look for the file license.txt at the root level. If you do not
 * find it, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA.
 * */
package org.cejug.yougi.web.controller;

import org.cejug.yougi.business.UserAccountBean;
import org.cejug.yougi.entity.*;
import org.cejug.yougi.business.JobSchedulerBean;
import org.cejug.yougi.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Hildeberto Mendonca - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class JobSchedulerMBean {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerMBean.class.getSimpleName());

    @EJB
    private JobSchedulerBean jobSchedulerBean;

    @EJB
    private UserAccountBean userAccountBean;

    private JobScheduler jobScheduler;

    private List<JobScheduler> jobSchedulers;
    private List<String> jobNames;
    private List<UserAccount> userAccounts;

    private String selectedOwner;
    private JobFrequencyType selectedFrequency;
    private Boolean workingDaysOnly;

    @ManagedProperty(value="#{param.id}")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String userId) {
        this.id = userId;
    }

    public JobScheduler getJobScheduler() {
        return jobScheduler;
    }

    public String getSelectedOwner() {
        return selectedOwner;
    }

    public void setSelectedOwner(String selectedOwner) {
        this.selectedOwner = selectedOwner;
    }

    public JobFrequencyType getSelectedFrequency() {
        return selectedFrequency;
    }

    public Boolean getWorkingDaysOnly() {
        return workingDaysOnly;
    }

    public void setWorkingDaysOnly(Boolean workingDaysOnly) {
        this.workingDaysOnly = workingDaysOnly;
    }

    public void setSelectedFrequency(JobFrequencyType selectedFrequency) {
        this.selectedFrequency = selectedFrequency;
    }

    public List<JobScheduler> getJobSchedulers() {
        if(this.jobSchedulers == null) {
            this.jobSchedulers = jobSchedulerBean.findAll();
        }
        return this.jobSchedulers;
    }

    public List<String> getJobNames() {
        if(this.jobNames == null) {
            this.jobNames = jobSchedulerBean.findUnscheduledJobNames();
        }
        return this.jobNames;
    }

    public List<UserAccount> getUserAccounts() {
        if(this.userAccounts == null) {
            this.userAccounts = userAccountBean.findAllActiveAccounts();
        }
        return this.userAccounts;
    }

    @PostConstruct
    public void load() {
        if(EntitySupport.INSTANCE.isIdValid(this.id)) {
            this.jobScheduler = jobSchedulerBean.find(this.id);
        }
        else {
            this.jobScheduler = jobSchedulerBean.getDefaultInstance();
            this.selectedFrequency = this.jobScheduler.getFrequencyType();
            jobScheduler.setActive(true);
        }
    }

    public String save() {
        if(!StringUtils.INSTANCE.isNullOrBlank(this.selectedOwner)) {
            UserAccount owner = userAccountBean.find(this.selectedOwner);
            this.jobScheduler.setDefaultOwner(owner);
        }

        if(this.selectedFrequency == JobFrequencyType.INSTANT) {
            jobScheduler = jobSchedulerBean.getInstance(this.selectedFrequency, JobInstantScheduler.class, this.jobScheduler);
            jobScheduler.setStartDate(Calendar.getInstance().getTime());
        }
        else if(this.selectedFrequency == JobFrequencyType.DAILY) {
            JobDailyScheduler jobDailyScheduler = jobSchedulerBean.getInstance(this.selectedFrequency, JobDailyScheduler.class, this.jobScheduler);
            jobDailyScheduler.setWorkingDay(this.getWorkingDaysOnly());
            jobScheduler = jobDailyScheduler;
        }
        else {
            jobScheduler = jobSchedulerBean.getInstance(this.selectedFrequency, this.jobScheduler);
        }

        jobSchedulerBean.save(jobScheduler);

        return "job_schedulers";
    }

    public String remove() {
        jobSchedulerBean.remove(this.jobScheduler.getId());
        return "job_schedulers";
    }
}