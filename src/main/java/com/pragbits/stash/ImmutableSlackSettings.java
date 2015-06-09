package com.pragbits.stash;

public class ImmutableLesschatSettings implements LesschatSettings {

    private final boolean lesschatNotificationsEnabled;
    private final boolean lesschatNotificationsOpenedEnabled;
    private final boolean lesschatNotificationsReopenedEnabled;
    private final boolean lesschatNotificationsUpdatedEnabled;
    private final boolean lesschatNotificationsApprovedEnabled;
    private final boolean lesschatNotificationsUnapprovedEnabled;
    private final boolean lesschatNotificationsDeclinedEnabled;
    private final boolean lesschatNotificationsMergedEnabled;
    private final boolean lesschatNotificationsCommentedEnabled;
    private final boolean lesschatNotificationsEnabledForPush;
    private final PushNotificationLevel pushNotificationLevel;
    private final String lesschatChannelName;
    private final String lesschatWebHookUrl;

    public ImmutableLesschatSettings(boolean lesschatNotificationsEnabled,
                                  boolean lesschatNotificationsOpenedEnabled,
                                  boolean lesschatNotificationsReopenedEnabled,
                                  boolean lesschatNotificationsUpdatedEnabled,
                                  boolean lesschatNotificationsApprovedEnabled,
                                  boolean lesschatNotificationsUnapprovedEnabled,
                                  boolean lesschatNotificationsDeclinedEnabled,
                                  boolean lesschatNotificationsMergedEnabled,
                                  boolean lesschatNotificationsCommentedEnabled,
                                  boolean lesschatNotificationsEnabledForPush,
                                  PushNotificationLevel pushNotificationLevel,
                                  String lesschatChannelName,
                                  String lesschatWebHookUrl) {
        this.lesschatNotificationsEnabled = lesschatNotificationsEnabled;
        this.lesschatNotificationsOpenedEnabled = lesschatNotificationsOpenedEnabled;
        this.lesschatNotificationsReopenedEnabled = lesschatNotificationsReopenedEnabled;
        this.lesschatNotificationsUpdatedEnabled = lesschatNotificationsUpdatedEnabled;
        this.lesschatNotificationsApprovedEnabled = lesschatNotificationsApprovedEnabled;
        this.lesschatNotificationsUnapprovedEnabled = lesschatNotificationsUnapprovedEnabled;
        this.lesschatNotificationsDeclinedEnabled = lesschatNotificationsDeclinedEnabled;
        this.lesschatNotificationsMergedEnabled = lesschatNotificationsMergedEnabled;
        this.lesschatNotificationsCommentedEnabled = lesschatNotificationsCommentedEnabled;
        this.lesschatNotificationsEnabledForPush = lesschatNotificationsEnabledForPush;
        this.pushNotificationLevel = pushNotificationLevel;
        this.lesschatChannelName = lesschatChannelName;
        this.lesschatWebHookUrl = lesschatWebHookUrl;
    }

    public boolean isLesschatNotificationsEnabled() {
        return lesschatNotificationsEnabled;
    }

    public boolean isLesschatNotificationsOpenedEnabled() {
        return lesschatNotificationsOpenedEnabled;
    }

    public boolean isLesschatNotificationsReopenedEnabled() {
        return lesschatNotificationsReopenedEnabled;
    }

    public boolean isLesschatNotificationsUpdatedEnabled() {
        return lesschatNotificationsUpdatedEnabled;
    }

    public boolean isLesschatNotificationsApprovedEnabled() {
        return lesschatNotificationsApprovedEnabled;
    }

    public boolean isLesschatNotificationsUnapprovedEnabled() {
        return lesschatNotificationsUnapprovedEnabled;
    }

    public boolean isLesschatNotificationsDeclinedEnabled() {
        return lesschatNotificationsDeclinedEnabled;
    }

    public boolean isLesschatNotificationsMergedEnabled() {
        return lesschatNotificationsMergedEnabled;
    }

    public boolean isLesschatNotificationsCommentedEnabled() {
        return lesschatNotificationsCommentedEnabled;
    }

    public boolean isLesschatNotificationsEnabledForPush() {
        return lesschatNotificationsEnabledForPush;
    }

    public PushNotificationLevel getPushNotificationLevel() {
        return pushNotificationLevel;
    }

    public String getLesschatChannelName() {
        return lesschatChannelName;
    }

    public String getLesschatWebHookUrl() {
        return lesschatWebHookUrl;
    }

    @Override
    public String toString() {
        return "ImmutableLesschatSettings {" + "lesschatNotificationsEnabled=" + lesschatNotificationsEnabled +
                ", lesschatNotificationsOpenedEnabled=" + lesschatNotificationsOpenedEnabled +
                ", lesschatNotificationsReopenedEnabled=" + lesschatNotificationsReopenedEnabled +
                ", lesschatNotificationsUpdatedEnabled=" + lesschatNotificationsUpdatedEnabled +
                ", lesschatNotificationsApprovedEnabled=" + lesschatNotificationsApprovedEnabled +
                ", lesschatNotificationsUnapprovedEnabled=" + lesschatNotificationsUnapprovedEnabled +
                ", lesschatNotificationsDeclinedEnabled=" + lesschatNotificationsDeclinedEnabled +
                ", lesschatNotificationsMergedEnabled=" + lesschatNotificationsMergedEnabled +
                ", lesschatNotificationsCommentedEnabled=" + lesschatNotificationsCommentedEnabled +
                ", lesschatNotificationsEnabledForPush=" + lesschatNotificationsEnabledForPush +
                ", pushNotificationLevel=" + pushNotificationLevel +
                ", lesschatChannelName=" + lesschatChannelName +
                ", lesschatWebHookUrl=" + lesschatWebHookUrl + "}";
    }

}
