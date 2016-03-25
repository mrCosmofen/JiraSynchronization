package ru.ssp.synch.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by PakAI on 22.03.2016.
 */
@Component
@ConfigurationProperties(prefix = "synch.local.jira")
public class LocalJiraConfiguration extends JiraConfiguration {

    private List<String[]> logins;

    private List<String[]> passwords;

    private String project;

    private List<String[]> issueTypes;

    private String defaultReporter;

    private String defaultCustomer;

    private String defaultBugType;

    private String defaultBugReason;

    private String temporaryDir;

    private List<String[]>  fixVersions;

    private List<String[]>  priorities;

    /**
     * Sets new passwords.
     *
     * @param passwords New value of passwords.
     */
    public void setPasswords(List<String[]> passwords) {
        this.passwords = passwords;
    }

    /**
     * Gets passwords.
     *
     * @return Value of passwords.
     */
    public List<String[]> getPasswords() {
        return passwords;
    }

    /**
     * Gets logins.
     *
     * @return Value of logins.
     */
    public List<String[]> getLogins() {
        return logins;
    }

    /**
     * Sets new logins.
     *
     * @param logins New value of logins.
     */
    public void setLogins(List<String[]> logins) {
        this.logins = logins;
    }

    /**
     * Gets project.
     *
     * @return Value of project.
     */
    public String getProject() {
        return project;
    }

    /**
     * Sets new project.
     *
     * @param project New value of project.
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * Sets new issueTypes.
     *
     * @param issueTypes New value of issueTypes.
     */
    public void setIssueTypes(List<String[]> issueTypes) {
        this.issueTypes = issueTypes;
    }

    /**
     * Gets issueTypes.
     *
     * @return Value of issueTypes.
     */
    public List<String[]> getIssueTypes() {
        return issueTypes;
    }

    /**
     * Sets new defaultReporter.
     *
     * @param defaultReporter New value of defaultReporter.
     */
    public void setDefaultReporter(String defaultReporter) {
        this.defaultReporter = defaultReporter;
    }

    /**
     * Gets defaultReporter.
     *
     * @return Value of defaultReporter.
     */
    public String getDefaultReporter() {
        return defaultReporter;
    }

    /**
     * Sets new defaultCustomer.
     *
     * @param defaultCustomer New value of defaultCustomer.
     */
    public void setDefaultCustomer(String defaultCustomer) {
        this.defaultCustomer = defaultCustomer;
    }

    /**
     * Gets defaultCustomer.
     *
     * @return Value of defaultCustomer.
     */
    public String getDefaultCustomer() {
        return defaultCustomer;
    }

    /**
     * Gets fixVersions.
     *
     * @return Value of fixVersions.
     */
    public List<String[]> getFixVersions() {
        return fixVersions;
    }

    /**
     * Sets new fixVersions.
     *
     * @param fixVersions New value of fixVersions.
     */
    public void setFixVersions(List<String[]> fixVersions) {
        this.fixVersions = fixVersions;
    }

    /**
     * Gets priorities.
     *
     * @return Value of priorities.
     */
    public List<String[]> getPriorities() {
        return priorities;
    }

    /**
     * Sets new priorities.
     *
     * @param priorities New value of priorities.
     */
    public void setPriorities(List<String[]> priorities) {
        this.priorities = priorities;
    }

    /**
     * Sets new defaultBugType.
     *
     * @param defaultBugType New value of defaultBugType.
     */
    public void setDefaultBugType(String defaultBugType) {
        this.defaultBugType = defaultBugType;
    }

    /**
     * Gets defaultBugType.
     *
     * @return Value of defaultBugType.
     */
    public String getDefaultBugType() {
        return defaultBugType;
    }

    /**
     * Gets defaultBugReason.
     *
     * @return Value of defaultBugReason.
     */
    public String getDefaultBugReason() {
        return defaultBugReason;
    }

    /**
     * Sets new defaultBugReason.
     *
     * @param defaultBugReason New value of defaultBugReason.
     */
    public void setDefaultBugReason(String defaultBugReason) {
        this.defaultBugReason = defaultBugReason;
    }

    /**
     * Gets temporaryDir.
     *
     * @return Value of temporaryDir.
     */
    public String getTemporaryDir() {
        return temporaryDir;
    }

    /**
     * Sets new temporaryDir.
     *
     * @param temporaryDir New value of temporaryDir.
     */
    public void setTemporaryDir(String temporaryDir) {
        this.temporaryDir = temporaryDir;
    }
}
