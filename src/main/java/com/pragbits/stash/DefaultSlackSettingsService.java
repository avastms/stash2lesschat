package com.pragbits.stash;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultLesschatSettingsService implements LesschatSettingsService {

    static final ImmutableLesschatSettings DEFAULT_CONFIG = new ImmutableLesschatSettings(
            false,  // pull requests enabled
            true,   // opened
            true,   // reopened
            true,   // updated
            true,   // approved
            true,   // unapproved
            true,   // declined
            true,   // merged
            true,   // commented
            false,  // push enabled
            PushNotificationLevel.VERBOSE,
            "",     // channel name override
            "");    // webhook override

    static final String KEY_LESSCHAT_NOTIFICATION = "lesschatNotificationsEnabled";
    static final String KEY_LESSCHAT_OPENED_NOTIFICATION = "lesschatNotificationsOpenedEnabled";
    static final String KEY_LESSCHAT_REOPENED_NOTIFICATION = "lesschatNotificationsReopenedEnabled";
    static final String KEY_LESSCHAT_UPDATED_NOTIFICATION = "lesschatNotificationsUpdatedEnabled";
    static final String KEY_LESSCHAT_APPROVED_NOTIFICATION = "lesschatNotificationsApprovedEnabled";
    static final String KEY_LESSCHAT_UNAPPROVED_NOTIFICATION = "lesschatNotificationsUnapprovedEnabled";
    static final String KEY_LESSCHAT_DECLINED_NOTIFICATION = "lesschatNotificationsDeclinedEnabled";
    static final String KEY_LESSCHAT_MERGED_NOTIFICATION = "lesschatNotificationsMergedEnabled";
    static final String KEY_LESSCHAT_COMMENTED_NOTIFICATION = "lesschatNotificationsCommentedEnabled";
    static final String KEY_LESSCHAT_NOTIFICATION_PUSH = "lesschatNotificationsEnabledForPush";
    static final String KEY_LESSCHAT_PUSH_NOTIFICATION_LEVEL = "lesschatPushNotificationLevel";
    static final String KEY_LESSCHAT_CHANNEL_NAME = "lesschatChannelName";
    static final String KEY_LESSCHAT_WEBHOOK_URL = "lesschatWebHookUrl";

    private final PluginSettings pluginSettings;
    private final PermissionValidationService validationService;

    private final Cache<Integer, LesschatSettings> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<Integer, LesschatSettings>() {
                @Override
                public LesschatSettings load(@Nonnull Integer repositoryId) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> data = (Map) pluginSettings.get(repositoryId.toString());
                    return data == null ? DEFAULT_CONFIG : deserialize(data);
                }
            }
    );

    public DefaultLesschatSettingsService(PluginSettingsFactory pluginSettingsFactory, PermissionValidationService validationService) {
        this.validationService = validationService;
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(PluginMetadata.getPluginKey());
    }

    @Nonnull
    @Override
    public LesschatSettings getLesschatSettings(@Nonnull Repository repository) {
        validationService.validateForRepository(checkNotNull(repository, "repository"), Permission.REPO_READ);

        try {
            //noinspection ConstantConditions
            return cache.get(repository.getId());
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    @Nonnull
    @Override
    public LesschatSettings setLesschatSettings(@Nonnull Repository repository, @Nonnull LesschatSettings settings) {
        validationService.validateForRepository(checkNotNull(repository, "repository"), Permission.REPO_ADMIN);
        Map<String, String> data = serialize(checkNotNull(settings, "settings"));
        pluginSettings.put(Integer.toString(repository.getId()), data);
        cache.invalidate(repository.getId());

        return deserialize(data);
    }

    // note: for unknown reason, pluginSettngs.get() is not getting back the key for an empty string value
    // probably I don't know someyhing here. Applying a hack
    private Map<String, String> serialize(LesschatSettings settings) {
        ImmutableMap<String, String> immutableMap = ImmutableMap.<String, String>builder()
                .put(KEY_LESSCHAT_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsEnabled()))
                .put(KEY_LESSCHAT_OPENED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsOpenedEnabled()))
                .put(KEY_LESSCHAT_REOPENED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsReopenedEnabled()))
                .put(KEY_LESSCHAT_UPDATED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsUpdatedEnabled()))
                .put(KEY_LESSCHAT_APPROVED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsApprovedEnabled()))
                .put(KEY_LESSCHAT_UNAPPROVED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsUnapprovedEnabled()))
                .put(KEY_LESSCHAT_DECLINED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsDeclinedEnabled()))
                .put(KEY_LESSCHAT_MERGED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsMergedEnabled()))
                .put(KEY_LESSCHAT_COMMENTED_NOTIFICATION, Boolean.toString(settings.isLesschatNotificationsCommentedEnabled()))
                .put(KEY_LESSCHAT_NOTIFICATION_PUSH, Boolean.toString(settings.isLesschatNotificationsEnabledForPush()))
                .put(KEY_LESSCHAT_PUSH_NOTIFICATION_LEVEL, settings.getPushNotificationLevel().toString())
                .put(KEY_LESSCHAT_CHANNEL_NAME, settings.getLesschatChannelName().isEmpty() ? " " : settings.getLesschatChannelName())
                .put(KEY_LESSCHAT_WEBHOOK_URL, settings.getLesschatWebHookUrl().isEmpty() ? " " : settings.getLesschatWebHookUrl())
                .build();

        return  immutableMap;
    }

    // note: for unknown reason, pluginSettngs.get() is not getting back the key for an empty string value
    // probably I don't know someyhing here. Applying a hack
    private LesschatSettings deserialize(Map<String, String> settings) {
        return new ImmutableLesschatSettings(
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_OPENED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_REOPENED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_UPDATED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_APPROVED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_UNAPPROVED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_DECLINED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_MERGED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_COMMENTED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_LESSCHAT_NOTIFICATION_PUSH)),
                settings.containsKey(KEY_LESSCHAT_PUSH_NOTIFICATION_LEVEL) ? PushNotificationLevel.valueOf(settings.get(KEY_LESSCHAT_PUSH_NOTIFICATION_LEVEL)) : PushNotificationLevel.VERBOSE,
                settings.get(KEY_LESSCHAT_CHANNEL_NAME).toString().equals(" ") ? "" : settings.get(KEY_LESSCHAT_CHANNEL_NAME).toString(),
                settings.get(KEY_LESSCHAT_WEBHOOK_URL).toString().equals(" ") ? "" : settings.get(KEY_LESSCHAT_WEBHOOK_URL).toString()
        );
    }

}
