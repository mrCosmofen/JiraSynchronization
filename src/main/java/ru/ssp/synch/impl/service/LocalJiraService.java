package ru.ssp.synch.impl.service;

import net.rcarz.jiraclient.*;
import net.sf.json.JSONNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ssp.synch.impl.config.ExternalJiraConfiguration;
import ru.ssp.synch.impl.config.LocalJiraConfiguration;
import ru.ssp.synch.impl.persistence.SyncDataRepository;
import ru.ssp.synch.impl.util.*;
import ru.ssp.synch.model.SyncData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.rcarz.jiraclient.Field.*;
import static ru.ssp.synch.api.enums.CustomFields.*;

/**
 * Created by PakAI on 23.03.2016.
 */
@Service
public class LocalJiraService extends AbstractJiraService {

    private final static Logger LOG = LoggerFactory.getLogger(LocalJiraService.class);
    public static final String COMMENT_TEMPLATE = "{panel:title=%s|borderStyle=dashed|borderColor=#ccc|bgColor=#F5F5F5}\n" +
            "%s\n" +
            "{panel}";

    @Override
    protected JiraClient getJiraClient() {
        return connectionUtils.getLocalJiraClient();
    }

    @Autowired
    private LocalJiraConfiguration localJiraConfig;

    @Autowired
    private ExternalJiraConfiguration extJiraConfig;

    @Autowired
    private IssueTypeConverter issueTypeConverter;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private VersionConverter versionConverter;

    @Autowired
    private PrioritiesConverter prioritiesConverter;

    @Autowired
    private SyncDataRepository syncDataRepository;

    @Autowired
    private TransitionHelper transitionHelper;


    public void createIssue(Issue extIssue) throws JiraException {
        String issueType = issueTypeConverter.convertFromExternal(extIssue.getIssueType().getName());
        String extIssueKey = extIssue.getKey();
        String project = localJiraConfig.getProject();

        String description = extJiraConfig.getUrl() + "/browse/" + extIssueKey + "\n";
        String issueDescription = extIssue.getDescription();
        description += StringUtils.isNotEmpty(issueDescription) ? issueDescription : "";

        Issue.FluentCreate fluentCreate = jiraClient.createIssue(project,
                issueType)
                .field(SUMMARY, "(" + extIssueKey + ") " + extIssue.getSummary())
                .field(ASSIGNEE, userConverter.convertFromExternal(extIssue.getAssignee().getName()))
                .field(REPORTER, localJiraConfig.getDefaultReporter())
                .field(DESCRIPTION, description)
                .field(LABELS, getLabels(extIssue))
                .field(FIX_VERSIONS, versionConverter.convertFromExternal(extIssue.getFixVersions()))
                .field(PRIORITY, prioritiesConverter.convertFromExternal(extIssue.getPriority().getName()))
                .field(CUSTOMER, Collections.singletonList(getCustomer(project).getValue()));

        Object environment = extIssue.getField(ENVIRONMENT);
        if (notNull(environment)) {
            fluentCreate.field(ENVIRONMENT, environment);
        }

        if (issueType.equals(localJiraConfig.getDefaultBugType())) {
            List<CustomFieldOption> bugReasonsAllowedValues = jiraClient.getCustomFieldAllowedValues(BUG_REASON, project, localJiraConfig.getDefaultBugType());
            fluentCreate.field(BUG_REASON, Collections.singletonList(bugReasonsAllowedValues.stream()
                    .filter(opt -> opt.getValue().equals(localJiraConfig.getDefaultBugReason())).findFirst().get().getValue()));
        }

        Issue createdIssue = fluentCreate.execute();

        String firstComment = getFirstComment(extIssue);
        if (StringUtils.isNotEmpty(firstComment)) {
            createdIssue.addComment(firstComment);
        }
        addComments(createdIssue, extIssue.getComments());
        addAttachments(createdIssue, extIssue.getAttachments());
        createdIssue.transition().execute("Готова к работе");

        saveSyncData(extIssue, createdIssue);

        LOG.info("Created issue with key: " + createdIssue.getKey());
    }

    public void updateIssue(Issue extIssue, SyncData syncData) throws JiraException {

        List<Attachment> newAttachments = extIssue.getAttachments().stream()
                .filter(attachment -> attachment.getCreatedDate().getTime() > syncData.getExtJiraLastEventDate().getTime())
                .collect(Collectors.toList());

        List<Comment> newComments = extIssue.getComments().stream()
                .filter(comment -> comment.getCreatedDate().getTime() > syncData.getExtJiraLastEventDate().getTime())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(newAttachments)
                || CollectionUtils.isNotEmpty(newComments)) {
            String localJiraKey = syncData.getLocalJiraKey();
            transitionHelper.flowToReadyState(localJiraKey, true);
            Issue issue = jiraClient.getIssue(localJiraKey);
            addComments(issue, newComments);
            addAttachments(issue, newAttachments);
            LOG.info("Updated issue with key: " + localJiraKey);
            syncData.setExtJiraLastEventDate(MappingUtils.getUpdatedDate(extIssue));
            syncData.setExtJiraStatus(extIssue.getStatus().getName());
            syncDataRepository.save(syncData);
        }
    }

    private boolean notNull(Object jsonObj) {
        return !JSONNull.getInstance().equals(jsonObj);
    }

    private CustomFieldOption getCustomer(String project) throws JiraException {
        List<CustomFieldOption> allowedValues = jiraClient.getCustomFieldAllowedValues(CUSTOMER, project, "Story");
        return allowedValues.stream()
                .filter(opt -> opt.getValue().equals(localJiraConfig.getDefaultCustomer())).findFirst().get();
    }

    private void addAttachments(Issue targetIssue, List<Attachment> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }

        for (Attachment attachment : attachments) {
            File tempFile = null;
            try {
                tempFile = getTempFile(attachment.getFileName());
                FileUtils.writeByteArrayToFile(tempFile, attachment.download());
                targetIssue.addAttachment(tempFile);
            } catch (JiraException | IOException e) {
                LOG.error("Не удалось загрузить файл по ссылке: ", attachment.getContentUrl());
            } finally {
                if(tempFile != null){
                    tempFile.delete();
                }
            }
        }

    }

    private File getTempFile(String fileName) throws IOException {
        String temporaryDir = localJiraConfig.getTemporaryDir();
        File tempFile = new File(temporaryDir + "/" + fileName);
        tempFile.createNewFile();
        tempFile.deleteOnExit();
        return tempFile;
    }

    private void saveSyncData(Issue issue, Issue createdIssue) {
        SyncData newSyncData = new SyncData();
        String extJiraKey = issue.getKey();
        newSyncData.setExtJiraKey(extJiraKey);
        newSyncData.setLocalJiraKey(createdIssue.getKey());
        newSyncData.setExtJiraLastEventDate(MappingUtils.getUpdatedDate(issue));
        newSyncData.setLocalJiraLastEventDate(MappingUtils.getUpdatedDate(createdIssue));
        newSyncData.setExtJiraStatus(issue.getStatus().getName());
        newSyncData.setLocalJiraStatus(createdIssue.getStatus().getName());

        syncDataRepository.save(newSyncData);
    }

    private void addComments(Issue targetIssue, List<Comment> comments) throws JiraException {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        for (Comment comment : comments) {
            String body = getComment(comment);
            if (StringUtils.isNotEmpty(body)) {
                targetIssue.addComment(body);
            }
        }
    }

    private String getComment(Comment comment) {
        return String.format(COMMENT_TEMPLATE, "Автор: " + comment.getAuthor(), comment.getBody());
    }

    private String getFirstComment(Issue issue) {
        String scenario = (String) issue.getField(TZ_SCENARIO);
        if (StringUtils.isEmpty(scenario)) {
            return null;
        }
        return String.format(COMMENT_TEMPLATE, "Код сценария из ЧТЗ", scenario);
    }

    private List<String> getLabels(Issue issue) {
        List<String> labels = new ArrayList<>();
        labels.add(issue.getKey());


        List<String> issueLabels = issue.getLabels();
        if (CollectionUtils.isNotEmpty(issueLabels)) {
            labels.addAll(issueLabels);
        }

        return labels;
    }

}
