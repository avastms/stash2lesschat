package com.pragbits.stash.components;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestActivityEvent;
import com.atlassian.stash.event.pull.PullRequestCommentActivityEvent;
import com.atlassian.stash.event.pull.PullRequestRescopeActivityEvent;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.avatar.AvatarService;
import com.atlassian.stash.avatar.AvatarRequest;
import com.google.gson.Gson;
import com.pragbits.stash.ColorCode;
import com.pragbits.stash.LesschatGlobalSettingsService;
import com.pragbits.stash.LesschatSettings;
import com.pragbits.stash.LesschatSettingsService;
import com.pragbits.stash.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PullRequestActivityListener {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2lesschat.globalsettings.hookurl";
    private static final Logger log = LoggerFactory.getLogger(PullRequestActivityListener.class);

    private final LesschatGlobalSettingsService lesschatGlobalSettingsService;
    private final LesschatSettingsService lesschatSettingsService;
    private final NavBuilder navBuilder;
    private final LesschatNotifier lesschatNotifier;
    private final AvatarService avatarService;
    private final AvatarRequest avatarRequest = new AvatarRequest(true, 16, true);
    private final Gson gson = new Gson();

    public PullRequestActivityListener(LesschatGlobalSettingsService lesschatGlobalSettingsService,
                                             LesschatSettingsService lesschatSettingsService,
                                             NavBuilder navBuilder,
                                             LesschatNotifier lesschatNotifier,
                                             AvatarService avatarService) {
        this.lesschatGlobalSettingsService = lesschatGlobalSettingsService;
        this.lesschatSettingsService = lesschatSettingsService;
        this.navBuilder = navBuilder;
        this.lesschatNotifier = lesschatNotifier;
        this.avatarService = avatarService;
    }

    @EventListener
    public void NotifyLesschatChannel(PullRequestActivityEvent event) {
        // find out if notification is enabled for this repo
        Repository repository = event.getPullRequest().getToRef().getRepository();
        LesschatSettings lesschatSettings = lesschatSettingsService.getLesschatSettings(repository);
        String globalHookUrl = lesschatGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);

        if (lesschatSettings.isLesschatNotificationsEnabled()) {

            String localHookUrl = lesschatSettings.getLesschatWebHookUrl();
            WebHookSelector hookSelector = new WebHookSelector(globalHookUrl, localHookUrl);

            if (!hookSelector.isHookValid()) {
                log.error("There is no valid configured Web hook url! Reason: " + hookSelector.getProblem());
                return;
            }

            String repoName = repository.getSlug();
            String projectName = repository.getProject().getKey();
            long pullRequestId = event.getPullRequest().getId();
            String userName = event.getUser() != null ? event.getUser().getDisplayName() : "unknown user";
            String activity = event.getActivity().getAction().name();
            String avatar = event.getUser() != null ? avatarService.getUrlForPerson(event.getUser(), avatarRequest) : "";

            // Ignore RESCOPED PR events
            if (activity.equalsIgnoreCase("RESCOPED") && event instanceof PullRequestRescopeActivityEvent) {
                return;
            }

            if (activity.equalsIgnoreCase("OPENED") && !lesschatSettings.isLesschatNotificationsOpenedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("REOPENED") && !lesschatSettings.isLesschatNotificationsReopenedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("UPDATED") && !lesschatSettings.isLesschatNotificationsUpdatedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("APPROVED") && !lesschatSettings.isLesschatNotificationsApprovedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("UNAPPROVED") && !lesschatSettings.isLesschatNotificationsUnapprovedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("DECLINED") && !lesschatSettings.isLesschatNotificationsDeclinedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("MERGED") && !lesschatSettings.isLesschatNotificationsMergedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("COMMENTED") && !lesschatSettings.isLesschatNotificationsCommentedEnabled()) {
                return;
            }
            
            String url = navBuilder
                    .project(projectName)
                    .repo(repoName)
                    .pullRequest(pullRequestId)
                    .overview()
                    .buildAbsolute();

            LesschatPayload payload = new LesschatPayload();
            payload.setMrkdwn(true);
            payload.setLinkNames(true);

            LesschatAttachment attachment = new LesschatAttachment();
            attachment.setAuthorName(userName);
            attachment.setAuthorIcon(avatar);

            String color = "";
            String fallback = "";
            String text = "";

            switch (event.getActivity().getAction()) {
                case OPENED:
                    attachment.setColor(ColorCode.BLUE.getCode());
                    attachment.setFallback(String.format("%s opened pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("opened pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));

                    this.addField(attachment, "Description", event.getPullRequest().getDescription());
                    this.addReviewers(attachment, event.getPullRequest().getReviewers());
                    break;

                case REOPENED:
                    attachment.setColor(ColorCode.BLUE.getCode());
                    attachment.setFallback(String.format("%s reopened pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("reopened pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));

                    this.addField(attachment, "Description", event.getPullRequest().getDescription());
                    this.addReviewers(attachment, event.getPullRequest().getReviewers());
                    break;

                case UPDATED:
                    attachment.setColor(ColorCode.PURPLE.getCode());
                    attachment.setFallback(String.format("%s updated pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("updated pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));

                    this.addField(attachment, "Description", event.getPullRequest().getDescription());
                    this.addReviewers(attachment, event.getPullRequest().getReviewers());
                    break;

                case APPROVED:
                    attachment.setColor(ColorCode.GREEN.getCode());
                    attachment.setFallback(String.format("%s approved pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("approved pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));
                    break;

                case UNAPPROVED:
                    attachment.setColor(ColorCode.RED.getCode());
                    attachment.setFallback(String.format("%s unapproved pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("unapproved pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));
                    break;

                case DECLINED:
                    attachment.setColor(ColorCode.RED.getCode());
                    attachment.setFallback(String.format("%s declined pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("declined pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));
                    break;

                case MERGED:
                    attachment.setColor(ColorCode.GREEN.getCode());
                    attachment.setFallback(String.format("%s merged pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("merged pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));
                    break;

                case RESCOPED:
                    attachment.setColor(ColorCode.PURPLE.getCode());
                    attachment.setFallback(String.format("%s rescoped on pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("rescoped on pull request <%s|#%d: %s>",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle()));
                    break;

                case COMMENTED:
                    attachment.setColor(ColorCode.PALE_BLUE.getCode());
                    attachment.setFallback(String.format("%s commented on pull request \"%s\". <%s|(open)>",
                                                            userName,
                                                            event.getPullRequest().getTitle(),
                                                            url));
                    attachment.setText(String.format("commented on pull request <%s|#%d: %s>\n%s",
                                                            url,
                                                            event.getPullRequest().getId(),
                                                            event.getPullRequest().getTitle(),
                                                            ((PullRequestCommentActivityEvent)event).getActivity().getComment().getText()));
                    break;
            }

            LesschatAttachmentField projectField = new LesschatAttachmentField();
            projectField.setTitle("Source");
            projectField.setValue(String.format("_%s — %s_\n`%s`",
                event.getPullRequest().getFromRef().getRepository().getProject().getName(),
                event.getPullRequest().getFromRef().getRepository().getName(),
                event.getPullRequest().getFromRef().getDisplayId()));
            projectField.setShort(true);
            attachment.addField(projectField);

            LesschatAttachmentField repoField = new LesschatAttachmentField();
            repoField.setTitle("Destination");
            repoField.setValue(String.format("_%s — %s_\n`%s`",
                event.getPullRequest().getFromRef().getRepository().getProject().getName(),
                event.getPullRequest().getToRef().getRepository().getName(),
                event.getPullRequest().getToRef().getDisplayId()));
            repoField.setShort(true);
            attachment.addField(repoField);

            payload.addAttachment(attachment);

            // lesschatSettings.getLesschatChannelName might be:
            // - empty
            // - comma separated list of channel names, eg: #mych1, #mych2, #mych3

            if (lesschatSettings.getLesschatChannelName().isEmpty()) {
                lesschatNotifier.SendLesschatNotification(hookSelector.getSelectedHook(), gson.toJson(payload));
            } else {
                // send message to multiple channels
                List<String> channels = Arrays.asList(lesschatSettings.getLesschatChannelName().split("\\s*,\\s*"));
                for (String channel: channels) {
                    payload.setChannel(channel.trim());
                    lesschatNotifier.SendLesschatNotification(hookSelector.getSelectedHook(), gson.toJson(payload));
                }
            }
        }

    }

    private void addField(LesschatAttachment attachment, String title, String message) {
        LesschatAttachmentField field = new LesschatAttachmentField();
        field.setTitle(title);
        field.setValue(message);
        field.setShort(false);
        attachment.addField(field);
    }

    private void addReviewers(LesschatAttachment attachment, Set<PullRequestParticipant> reviewers) {
        if (reviewers.isEmpty()) {
            return;
        }
        String names = "";
        for(PullRequestParticipant p : reviewers) {
            names += String.format("@%s ", p.getUser().getSlug());
        }
        this.addField(attachment, "Reviewers", names);
    }
}
