package ru.ssp.synch.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * Created by PakAI on 22.03.2016.
 */
@Entity
@javax.persistence.Table(name = "synch_data", schema = "synch",
indexes = {
        @Index(name = "EXT_STATUS_INDEX", columnList = "EXT_JIRA_STATUS"),
        @Index(name = "LOCAL_STATUS_INDEX", columnList = "LOCAL_JIRA_STATUS"),
        @Index(name = "LOCAL_JIRA_KEY_INDEX", columnList = "LOCAL_JIRA_KEY"),
        @Index(name = "EXT_JIRA_KEY_INDEX", columnList = "EXT_JIRA_KEY",unique = true)
})
@DynamicUpdate
public class SyncData {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "guid")
    private String guid;

    @Column(name = "EXT_JIRA_KEY")
    private String extJiraKey;

    @Column(name = "LOCAL_JIRA_KEY")
    private String localJiraKey;

    @Column(name = "EXT_JIRA_LAST_EVENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date extJiraLastEventDate;

    @Column(name = "LOCAL_JIRA_LAST_EVENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date localJiraLastEventDate;

    @Column(name = "EXT_JIRA_STATUS")
    private String extJiraStatus;

    @Column(name = "LOCAL_JIRA_STATUS")
    private String localJiraStatus;

    /**
     * Sets new guid.
     *
     * @param guid New value of guid.
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Gets localJiraKey.
     *
     * @return Value of localJiraKey.
     */
    public String getLocalJiraKey() {
        return localJiraKey;
    }


    /**
     * Sets new localJiraKey.
     *
     * @param localJiraKey New value of localJiraKey.
     */
    public void setLocalJiraKey(String localJiraKey) {
        this.localJiraKey = localJiraKey;
    }

    /**
     * Sets new extJiraKey.
     *
     * @param extJiraKey New value of extJiraKey.
     */
    public void setExtJiraKey(String extJiraKey) {
        this.extJiraKey = extJiraKey;
    }

    /**
     * Gets guid.
     *
     * @return Value of guid.
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Gets extJiraKey.
     *
     * @return Value of extJiraKey.
     */
    public String getExtJiraKey() {
        return extJiraKey;
    }

    /**
     * Gets localJiraLastEventDate.
     *
     * @return Value of localJiraLastEventDate.
     */
    public Date getLocalJiraLastEventDate() {
        return localJiraLastEventDate;
    }

    /**
     * Sets new localJiraLastEventDate.
     *
     * @param localJiraLastEventDate New value of localJiraLastEventDate.
     */
    public void setLocalJiraLastEventDate(Date localJiraLastEventDate) {
        this.localJiraLastEventDate = localJiraLastEventDate;
    }

    /**
     * Gets extJiraLastEventDate.
     *
     * @return Value of extJiraLastEventDate.
     */
    public Date getExtJiraLastEventDate() {
        return extJiraLastEventDate;
    }

    /**
     * Sets new extJiraLastEventDate.
     *
     * @param extJiraLastEventDate New value of extJiraLastEventDate.
     */
    public void setExtJiraLastEventDate(Date extJiraLastEventDate) {
        this.extJiraLastEventDate = extJiraLastEventDate;
    }

    /**
     * Gets extJiraStatus.
     *
     * @return Value of extJiraStatus.
     */
    public String getExtJiraStatus() {
        return extJiraStatus;
    }

    /**
     * Sets new extJiraStatus.
     *
     * @param extJiraStatus New value of extJiraStatus.
     */
    public void setExtJiraStatus(String extJiraStatus) {
        this.extJiraStatus = extJiraStatus;
    }

    /**
     * Sets new localJiraStatus.
     *
     * @param localJiraStatus New value of localJiraStatus.
     */
    public void setLocalJiraStatus(String localJiraStatus) {
        this.localJiraStatus = localJiraStatus;
    }

    /**
     * Gets localJiraStatus.
     *
     * @return Value of localJiraStatus.
     */
    public String getLocalJiraStatus() {
        return localJiraStatus;
    }
}
