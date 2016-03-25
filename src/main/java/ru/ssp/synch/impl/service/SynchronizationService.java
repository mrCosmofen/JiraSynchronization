package ru.ssp.synch.impl.service;

import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ssp.synch.impl.config.ExternalJiraConfiguration;
import ru.ssp.synch.impl.persistence.SyncDataRepository;
import ru.ssp.synch.impl.util.ConnectionUtils;
import ru.ssp.synch.impl.util.MappingUtils;
import ru.ssp.synch.model.SyncData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by PakAI on 22.03.2016.
 */
@Service
public class SynchronizationService {

    private final static Logger LOG = LoggerFactory.getLogger(SynchronizationService.class);

    @Autowired
    private ConnectionUtils connectionUtils;

    @Autowired
    private SyncDataRepository syncDataRepository;

    @Autowired
    private ExternalJiraConfiguration extJiraConfig;

    @Autowired
    private LocalJiraService localJiraService;

    public void doSynch() throws JiraException {
        JiraClient jira = connectionUtils.getExtJiraClient();
        Issue.SearchResult searchResult = jira.searchIssues(extJiraConfig.getJql(), "*all");

        Map<String, Issue> extIssues = searchResult.issues.stream()
                .collect(Collectors.toMap(Issue::getKey, Function.identity()));

        Set<String> nonExistentIssueKeys = createNonExistentIssues(extIssues);

        extIssues.keySet().removeAll(nonExistentIssueKeys);

        actualizeLocalDB();

        mergeIssueChanges(extIssues);
    }

    public void synchDB() throws JiraException {
        JiraClient extJira = connectionUtils.getExtJiraClient();
        Issue.SearchResult searchResult = extJira.searchIssues(extJiraConfig.getJql());

        List<Issue> issuesInExtJira = searchResult.issues;
        List<String> extIssueKeys = issuesInExtJira.stream()
                .map(Issue::getKey).collect(Collectors.toList());

        JiraClient localJira = connectionUtils.getLocalJiraClient();

        if (CollectionUtils.isEmpty(extIssueKeys)) {
            return;
        }

        List<SyncData> syncDatas = syncDataRepository.findByExtJiraKeyIn(extIssueKeys);

        List<String> existingExtJiraKeys = syncDatas.stream()
                .map(SyncData::getExtJiraKey)
                .collect(Collectors.toList());

        extIssueKeys.removeAll(existingExtJiraKeys);

        if (CollectionUtils.isEmpty(extIssueKeys)) {
            return;
        }

        extIssueKeys.forEach(String::trim);

        Issue.SearchResult existingIssuesSearchResult = localJira.searchIssues(String.format("labels in (%s)", StringUtils.join(extIssueKeys, ',')));

        Map<String, Issue> existingLocalJiraIssues = existingIssuesSearchResult.issues.stream()
                .collect(Collectors.toMap(
                        issue -> issue.getLabels()
                                .stream()
                                .filter(s -> s.matches("HCS.*")).findFirst().get(),
                        Function.identity()));

        Set<String> localJiraIssueKeys = existingLocalJiraIssues.keySet();
        extIssueKeys.removeAll(localJiraIssueKeys);

        List<SyncData> issuesToAddInDB = issuesInExtJira.stream()
                .filter(issue -> localJiraIssueKeys.contains(issue.getKey()))
                .map(issue -> {
                    SyncData result = new SyncData();
                    String extJiraKey = issue.getKey();
                    result.setExtJiraKey(extJiraKey);
                    Issue localJiraIssue = existingLocalJiraIssues.get(extJiraKey);
                    result.setLocalJiraKey(localJiraIssue.getKey());
                    result.setExtJiraLastEventDate(MappingUtils.getUpdatedDate(issue));
                    result.setLocalJiraLastEventDate(MappingUtils.getUpdatedDate(localJiraIssue));
                    result.setExtJiraStatus(issue.getStatus().getName());
                    result.setLocalJiraStatus(localJiraIssue.getStatus().getName());
                    return result;
                })
                .collect(Collectors.toList());

        syncDataRepository.save(issuesToAddInDB);

    }

    private Set<String> createNonExistentIssues(Map<String, Issue> extIssues) throws JiraException {
        Set<String> extJiraKeys = extIssues.keySet();
        Map<String, SyncData> existingIssues = syncDataRepository.findByExtJiraKeyIn(extJiraKeys)
                .stream().collect(Collectors.toMap(SyncData::getExtJiraKey, Function.identity()));

        Set<String> nonExistentJiraKeys = new HashSet<>(extJiraKeys.stream()
                .filter(s -> !existingIssues.keySet().contains(s))
                .collect(Collectors.toList()));

        for (String nonExistentJiraKey : nonExistentJiraKeys) {
            Issue issue = extIssues.get(nonExistentJiraKey);
            localJiraService.createIssue(issue);
        }

        return nonExistentJiraKeys;
    }

    private void mergeIssueChanges(Map<String, Issue> extIssues) throws JiraException {
        Map<String, SyncData> stringSyncDataMap = syncDataRepository.findByExtJiraKeyIn(extIssues.keySet()).stream()
                .collect(Collectors.toMap(SyncData::getExtJiraKey, Function.identity()));

        Set<Map.Entry<String, Issue>> updatedIssues = extIssues.entrySet().stream()
                .filter(entry -> {
                    Date updatedDate = MappingUtils.getUpdatedDate(entry.getValue());
                    return updatedDate != null && stringSyncDataMap.get(entry.getKey()).getExtJiraLastEventDate().getTime() < updatedDate.getTime();
                })
                .collect(Collectors.toSet());

        for (Map.Entry<String, Issue> updatedIssue : updatedIssues) {
            String key = updatedIssue.getKey();
            localJiraService.updateIssue(extIssues.get(key), stringSyncDataMap.get(key));
        }
    }

    private void actualizeLocalDB() throws JiraException {
        JiraClient localJira = connectionUtils.getLocalJiraClient();

        List<SyncData> syncDatas = (List<SyncData>) syncDataRepository.findAll();

        Issue.SearchResult searchResult = localJira.searchIssues(
                String.format("key in (%s)", StringUtils.join(syncDatas.stream()
                        .map(SyncData::getLocalJiraKey)
                        .collect(Collectors.toList()), ','))
        );

        Map<String, SyncData> syncDataMap = syncDatas.stream()
                .collect(Collectors.toMap(SyncData::getLocalJiraKey, Function.identity()));

        List<SyncData> issuesToUpdate = searchResult.issues.stream()
                .filter(issue -> {
                    SyncData existingSyncData = syncDataMap.get(issue.getKey());
                    Date updatedDate = MappingUtils.getUpdatedDate(issue);
                    return updatedDate != null && (existingSyncData.getLocalJiraLastEventDate().getTime() != updatedDate.getTime()
                            || !existingSyncData.getLocalJiraStatus().equals(issue.getStatus().getName()));
                })
                .map(issue -> {
                    SyncData syncData = syncDataMap.get(issue.getKey());
                    syncData.setLocalJiraLastEventDate(MappingUtils.getUpdatedDate(issue));
                    syncData.setLocalJiraStatus(issue.getStatus().getName());
                    return syncData;
                })
                .collect(Collectors.toList());

        syncDataRepository.save(issuesToUpdate);

    }


}
