package com.pragbits.stash;

import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.exception.AuthorisationException;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.stash.i18n.I18nService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.pragbits.stash.soy.SelectFieldOptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LesschatSettingsServlet extends HttpServlet {
    private final PageBuilderService pageBuilderService;
    private final LesschatSettingsService lesschatSettingsService;
    private final RepositoryService repositoryService;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PermissionValidationService validationService;
    private final I18nService i18nService;
    
    private Repository repository = null;

    public LesschatSettingsServlet(PageBuilderService pageBuilderService,
                                    LesschatSettingsService lesschatSettingsService,
                                    RepositoryService repositoryService,
                                    SoyTemplateRenderer soyTemplateRenderer,
                                    PermissionValidationService validationService,
                                    I18nService i18nService) {
        this.pageBuilderService = pageBuilderService;
        this.lesschatSettingsService = lesschatSettingsService;
        this.repositoryService = repositoryService;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.validationService = validationService;
        this.i18nService = i18nService;
    }

    @Override
    protected  void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        try {
            validationService.validateForRepository(this.repository, Permission.REPO_ADMIN);
        } catch (AuthorisationException e) {
            // Skip form processing
            doGet(req, res);
            return;
        }

        boolean enabled = false;
        if (null != req.getParameter("lesschatNotificationsEnabled") && req.getParameter("lesschatNotificationsEnabled").equals("on")) {
          enabled = true;
        }

        boolean openedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsOpenedEnabled") && req.getParameter("lesschatNotificationsOpenedEnabled").equals("on")) {
            openedEnabled = true;
        }

        boolean reopenedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsReopenedEnabled") && req.getParameter("lesschatNotificationsReopenedEnabled").equals("on")) {
            reopenedEnabled = true;
        }

        boolean updatedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsUpdatedEnabled") && req.getParameter("lesschatNotificationsUpdatedEnabled").equals("on")) {
            updatedEnabled = true;
        }

        boolean approvedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsApprovedEnabled") && req.getParameter("lesschatNotificationsApprovedEnabled").equals("on")) {
            approvedEnabled = true;
        }

        boolean unapprovedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsUnapprovedEnabled") && req.getParameter("lesschatNotificationsUnapprovedEnabled").equals("on")) {
            unapprovedEnabled = true;
        }

        boolean declinedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsDeclinedEnabled") && req.getParameter("lesschatNotificationsDeclinedEnabled").equals("on")) {
            declinedEnabled = true;
        }

        boolean mergedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsMergedEnabled") && req.getParameter("lesschatNotificationsMergedEnabled").equals("on")) {
            mergedEnabled = true;
        }

        boolean commentedEnabled = false;
        if (null != req.getParameter("lesschatNotificationsCommentedEnabled") && req.getParameter("lesschatNotificationsCommentedEnabled").equals("on")) {
            commentedEnabled = true;
        }

        boolean enabledPush = false;
        if (null != req.getParameter("lesschatNotificationsEnabledForPush") && req.getParameter("lesschatNotificationsEnabledForPush").equals("on")) {
            enabledPush = true;
        }

        PushNotificationLevel pushNotificationLevel = PushNotificationLevel.VERBOSE;
        if (null != req.getParameter("lesschatPushNotificationLevel")) {
            pushNotificationLevel = PushNotificationLevel.valueOf(req.getParameter("lesschatPushNotificationLevel"));
        }

        String channel = req.getParameter("lesschatChannelName");
        String webHookUrl = req.getParameter("lesschatWebHookUrl");
        lesschatSettingsService.setLesschatSettings(
                repository,
                new ImmutableLesschatSettings(
                        enabled,
                        openedEnabled,
                        reopenedEnabled,
                        updatedEnabled,
                        approvedEnabled,
                        unapprovedEnabled,
                        declinedEnabled,
                        mergedEnabled,
                        commentedEnabled,
                        enabledPush,
                        pushNotificationLevel,
                        channel,
                        webHookUrl));

        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String[] pathParts = pathInfo.substring(1).split("/");
        if (pathParts.length != 2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String projectKey = pathParts[0];
        String repoSlug = pathParts[1];
        
        this.repository = repositoryService.getBySlug(projectKey, repoSlug);
        if (repository == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        doView(repository, response);

    }

    private void doView(Repository repository, HttpServletResponse response)
            throws ServletException, IOException {
        validationService.validateForRepository(repository, Permission.REPO_ADMIN);
        LesschatSettings lesschatSettings = lesschatSettingsService.getLesschatSettings(repository);
        render(response,
                "stash.page.lesschat.settings.viewLesschatSettings",
                ImmutableMap.<String, Object>builder()
                        .put("repository", repository)
                        .put("lesschatSettings", lesschatSettings)
                        .put("pushNotificationLevels", new SelectFieldOptions(PushNotificationLevel.values()).toSoyStructure())
                        .build()
        );
    }

    private void render(HttpServletResponse response, String templateName, Map<String, Object> data)
            throws IOException, ServletException {
        pageBuilderService.assembler().resources().requireContext("plugin.page.lesschat");
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
