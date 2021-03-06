package com.pragbits.stash;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.exception.AuthorisationException;
import com.atlassian.stash.nav.NavBuilder;
import com.pragbits.stash.PluginMetadata;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.avatar.AvatarService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.stash.i18n.I18nService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LesschatGlobalSettingsServlet extends HttpServlet {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2lesschat.globalsettings.hookurl";

    private final PageBuilderService pageBuilderService;
    private final LesschatGlobalSettingsService lesschatGlobalSettingsService;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PermissionValidationService validationService;
    private final I18nService i18nService;

    public LesschatGlobalSettingsServlet(PageBuilderService pageBuilderService,
                                      LesschatGlobalSettingsService lesschatGlobalSettingsService,
                                      SoyTemplateRenderer soyTemplateRenderer,
                                      PermissionValidationService validationService,
                                      I18nService i18nService) {
        this.pageBuilderService = pageBuilderService;
        this.lesschatGlobalSettingsService = lesschatGlobalSettingsService;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.validationService = validationService;
        this.i18nService = i18nService;

    }

    @Override
    protected  void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            validationService.validateForGlobal(Permission.SYS_ADMIN);
        } catch (AuthorisationException e) {
            // Skip form processing
            doGet(req, res);
            return;
        }

        String globalWebHookUrl = req.getParameter("lesschatGlobalWebHookUrl");
        if (null != globalWebHookUrl) {
            lesschatGlobalSettingsService.setWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL, globalWebHookUrl);
        }

        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doView(response);

    }

    private void doView(HttpServletResponse response)
            throws ServletException, IOException {

        validationService.validateForGlobal(Permission.ADMIN);

        String webHookUrl = lesschatGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);
        if (null == webHookUrl || webHookUrl.equals("")) {
            webHookUrl = "";
        }

        render(response,
                "stash.page.lesschat.global.settings.viewGlobalLesschatSettings",
                ImmutableMap.<String, Object>builder()
                        .put("lesschatGlobalWebHookUrl", webHookUrl)
                        .build()
        );
    }

    private void render(HttpServletResponse response, String templateName, Map<String, Object> data)
            throws IOException, ServletException {
        pageBuilderService.assembler().resources().requireContext("plugin.adminpage.lesschat");
        response.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(response.getWriter(), PluginMetadata.getCompleteModuleKey("soy-templates"), templateName, data);
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }

}
