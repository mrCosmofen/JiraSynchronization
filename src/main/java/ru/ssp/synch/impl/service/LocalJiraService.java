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
import java.net.URI;
import java.util.*;
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
    private ComponentsConverter componentsConverter;

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
                .field(PRIORITY, prioritiesConverter.convertFromExternal(extIssue.getPriority().getName()))
                .field(CUSTOMER, Collections.singletonList(getCustomer(project).getValue()));

        addfixVersions(extIssue, fluentCreate);

        addComponents(extIssue, fluentCreate);

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
            syncData.setExtJiraLastEventDate(MappingUtils.getUpdatedDate(extIssue));
            syncData.setExtJiraStatus(extIssue.getStatus().getName());
            syncDataRepository.save(syncData);
            LOG.info("Updated issue with key: " + localJiraKey);
        }
    }

    public boolean deleteIssue(Issue issue) throws JiraException {
        String key = issue.getKey();
        try {
            JiraClient jiraClient = getJiraClient();
            java.lang.reflect.Field f = jiraClient.getClass().getDeclaredField("restclient");
            f.setAccessible(true);
            RestClient restclient = (RestClient) f.get(jiraClient);

            URI uri = restclient.buildURI("/rest/api/latest/issue/" + key, new HashMap<String, String>() {{
                put("deleteSubtasks", String.valueOf(true));
            }});
            LOG.info("Deleted issue with key: " + key);
            return (restclient.delete(uri) == null);
        } catch (Exception ex) {
            throw new JiraException("Failed to delete issue " + key, ex);
        }
    }

    private void addComponents(Issue extIssue, Issue.FluentCreate fluentCreate) {
        List<Component> components = extIssue.getComponents();
        if(CollectionUtils.isNotEmpty(components)) {
            List<String> componentNames = components.stream()
                    .map(component -> componentsConverter.convertFromExternal(component.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            fluentCreate.field(COMPONENTS, componentNames);
        }
    }

    private void addfixVersions(Issue extIssue, Issue.FluentCreate fluentCreate) {
        List<Version> fixVersions = extIssue.getFixVersions();
        if(CollectionUtils.isNotEmpty(fixVersions)) {
            List<String> versions = fixVersions.stream()
                    .map(version -> versionConverter.convertFromExternal(version.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            fluentCreate.field(FIX_VERSIONS, versions);
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
                if (tempFile != null) {
                    boolean delete = tempFile.delete();
                    if(!delete){
                        LOG.error("Не удалось удалить файл: " + tempFile.getName());
                    }
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
        String title = "Автор: " + comment.getAuthor();
        Date createdDate = comment.getCreatedDate();
        title += createdDate != null ? " " + createdDate : "";
        return String.format(COMMENT_TEMPLATE, title, comment.getBody());
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
