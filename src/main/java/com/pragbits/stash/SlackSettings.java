package com.pragbits.stash;

public interface LesschatSettings {

    boolean isLesschatNotificationsEnabled();
    boolean isLesschatNotificationsOpenedEnabled();
    boolean isLesschatNotificationsReopenedEnabled();
    boolean isLesschatNotificationsUpdatedEnabled();
    boolean isLesschatNotificationsApprovedEnabled();
    boolean isLesschatNotificationsUnapprovedEnabled();
    boolean isLesschatNotificationsDeclinedEnabled();
    boolean isLesschatNotificationsMergedEnabled();
    boolean isLesschatNotificationsCommentedEnabled();
    boolean isLesschatNotificationsEnabledForPush();
    PushNotificationLevel getPushNotificationLevel();
    String getLesschatChannelName();
    String getLesschatWebHookUrl();

}
