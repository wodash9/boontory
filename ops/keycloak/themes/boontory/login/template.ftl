<#import "footer.ftl" as loginFooter>
<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html lang="${lang}"<#if realm.internationalizationEnabled> dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>
<head>
  <meta charset="utf-8">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="robots" content="noindex, nofollow">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <#if properties.meta?has_content>
    <#list properties.meta?split(' ') as meta>
      <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
    </#list>
  </#if>

  <title>${msg("loginTitle", (realm.displayName!'Boontory'))}</title>
  <link rel="icon" href="${url.resourcesPath}/img/favicon.svg" />

  <#if properties.stylesCommon?has_content>
    <#list properties.stylesCommon?split(' ') as style>
      <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
    </#list>
  </#if>
  <#if properties.styles?has_content>
    <#list properties.styles?split(' ') as style>
      <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
    </#list>
  </#if>
  <#if properties.scripts?has_content>
    <#list properties.scripts?split(' ') as script>
      <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
    </#list>
  </#if>
  <script type="importmap">
    {
      "imports": {
        "rfc4648": "${url.resourcesCommonPath}/vendor/rfc4648/rfc4648.js"
      }
    }
  </script>
  <script src="${url.resourcesPath}/js/menu-button-links.js" type="module"></script>
  <#if scripts??>
    <#list scripts as script>
      <script src="${script}" type="text/javascript"></script>
    </#list>
  </#if>
  <script type="module">
    import { startSessionPolling } from "${url.resourcesPath}/js/authChecker.js";

    startSessionPolling(
      "${url.ssoLoginInOtherTabsUrl?no_esc}"
    );
  </script>
  <#if authenticationSession??>
    <script type="module">
      import { checkAuthSession } from "${url.resourcesPath}/js/authChecker.js";

      checkAuthSession(
        "${authenticationSession.authSessionIdHash}"
      );
    </script>
  </#if>
</head>

<body class="boontory-login ${bodyClass}">
  <div class="boontory-page" data-page="keycloak-login">
    <section class="boontory-brand-panel" aria-label="${msg('boontoryBrandPanelLabel')}">
      <nav class="boontory-brand-row" aria-label="${msg('boontoryBrandNavigationLabel')}">
        <span class="boontory-mark" aria-hidden="true">B</span>
        <span class="boontory-wordmark">Boontory</span>
      </nav>

      <div class="boontory-copy">
        <p class="boontory-eyebrow">${msg("boontoryLoginEyebrow")}</p>
        <h1>${msg("boontoryLoginHeadline")}</h1>
        <p class="boontory-subheadline">${msg("boontoryLoginSubheadline")}</p>
      </div>

    </section>

    <main class="boontory-auth-panel" aria-label="${msg('boontoryAuthPanelLabel')}">
      <div class="boontory-auth-card">
        <div class="boontory-auth-brand">
          <img src="${url.resourcesPath}/img/logo.svg" alt="" />
          <div>
            <p>Boontory</p>
            <span>${msg("boontorySecureAccess")}</span>
          </div>
        </div>

        <header class="boontory-auth-header">
          <p class="boontory-eyebrow">${msg("boontoryAuthEyebrow")}</p>
          <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
            <h2 id="kc-page-title"><#nested "header"></h2>
          <#else>
            <#nested "show-username">
            <h2 id="kc-page-title"><#nested "header"></h2>
            <div id="kc-username" class="boontory-attempted-user ${properties.kcFormGroupClass!}">
              <span id="kc-attempted-username">${auth.attemptedUsername}</span>
              <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">${msg("restartLoginTooltip")}</a>
            </div>
          </#if>
        </header>

        <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
          <div class="boontory-alert boontory-alert--${message.type}" role="alert" aria-live="polite">
            <span class="boontory-alert-dot" aria-hidden="true"></span>
            <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
          </div>
        </#if>

        <#nested "form">

        <#if auth?has_content && auth.showTryAnotherWayLink()>
          <form id="kc-select-try-another-way-form" class="boontory-try-another-way" action="${url.loginAction}" method="post">
            <input type="hidden" name="tryAnotherWay" value="on"/>
            <button type="submit" id="try-another-way">${msg("doTryAnotherWay")}</button>
          </form>
        </#if>

        <#nested "socialProviders">

        <#if displayInfo>
          <aside id="kc-info" class="boontory-auth-info">
            <#nested "info">
          </aside>
        </#if>
      </div>
      <p class="boontory-privacy-note">${msg("boontoryPrivacyNote")}</p>
    </main>
  </div>
</body>
</html>
</#macro>
