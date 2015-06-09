package com.pragbits.stash;

public interface LesschatGlobalSettingsService {
    String getWebHookUrl(String key);
    void setWebHookUrl(String key, String value);
}
