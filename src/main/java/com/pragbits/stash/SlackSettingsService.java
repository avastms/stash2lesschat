package com.pragbits.stash;

import com.atlassian.stash.repository.Repository;
import javax.annotation.Nonnull;

public interface LesschatSettingsService {

    @Nonnull
    LesschatSettings getLesschatSettings(@Nonnull Repository repository);

    @Nonnull
    LesschatSettings setLesschatSettings(@Nonnull Repository repository, @Nonnull LesschatSettings settings);

}
