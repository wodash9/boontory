<#import "template.ftl" as layout>
<@layout.registrationLayout bodyClass="boontory-login-screen" displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
  <#if section = "header">
    ${msg("loginAccountTitle")}
  <#elseif section = "form">
    <div id="kc-form" class="boontory-form-area">
      <div id="kc-form-wrapper">
        <#if realm.password>
          <form id="kc-form-login" class="boontory-form" onsubmit="document.getElementById('kc-login').disabled = true; return true;" action="${url.loginAction}" method="post">
            <#if !usernameHidden??>
              <div class="boontory-field ${properties.kcFormGroupClass!}">
                <label for="username" class="boontory-label ${properties.kcLabelClass!}">
                  <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
                </label>
                <input tabindex="2" id="username" class="boontory-input ${properties.kcInputClass!}" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="username" aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" dir="ltr" />

                <#if messagesPerField.existsError('username','password')>
                  <span id="input-error" class="boontory-input-error ${properties.kcInputErrorMessageClass!}" aria-live="polite">
                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                  </span>
                </#if>
              </div>
            </#if>

            <div class="boontory-field ${properties.kcFormGroupClass!}">
              <label for="password" class="boontory-label ${properties.kcLabelClass!}">${msg("password")}</label>
              <div class="boontory-password-wrap ${properties.kcInputGroup!}" dir="ltr">
                <input tabindex="3" id="password" class="boontory-input ${properties.kcInputClass!}" name="password" type="password" autocomplete="current-password" aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" />
                <button class="boontory-password-toggle ${properties.kcFormPasswordVisibilityButtonClass!}" type="button" aria-label="${msg("showPassword")}" aria-controls="password" data-password-toggle tabindex="4" data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}" data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}" data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                  <i class="${properties.kcFormPasswordVisibilityIconShow!}" aria-hidden="true"></i>
                </button>
              </div>

              <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                <span id="input-error" class="boontory-input-error ${properties.kcInputErrorMessageClass!}" aria-live="polite">
                  ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                </span>
              </#if>
            </div>

            <div class="boontory-form-options ${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
              <div id="kc-form-options">
                <#if realm.rememberMe && !usernameHidden??>
                  <label class="boontory-check">
                    <#if login.rememberMe??>
                      <input tabindex="5" id="rememberMe" name="rememberMe" type="checkbox" checked>
                    <#else>
                      <input tabindex="5" id="rememberMe" name="rememberMe" type="checkbox">
                    </#if>
                    <span>${msg("rememberMe")}</span>
                  </label>
                </#if>
              </div>
              <div class="boontory-form-links ${properties.kcFormOptionsWrapperClass!}">
                <#if realm.resetPasswordAllowed>
                  <a tabindex="6" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
                </#if>
              </div>
            </div>

            <div id="kc-form-buttons" class="boontory-submit-row ${properties.kcFormGroupClass!}">
              <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
              <input tabindex="7" class="boontory-submit ${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
            </div>
          </form>
        </#if>
      </div>
    </div>
    <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
  <#elseif section = "info" >
    <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
      <div id="kc-registration-container" class="boontory-registration">
        <div id="kc-registration">
          <span>${msg("noAccount")} <a tabindex="8" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
        </div>
      </div>
    </#if>
  <#elseif section = "socialProviders" >
    <#if realm.password && social?? && social.providers?has_content>
      <section id="kc-social-providers" class="boontory-social ${properties.kcFormSocialAccountSectionClass!}" aria-label="${msg('identity-provider-login-label')}">
        <div class="boontory-divider"><span>${msg("identity-provider-login-label")}</span></div>
        <ul class="boontory-social-list ${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
          <#list social.providers as p>
            <li>
              <a id="social-${p.alias}" class="boontory-social-button ${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>" type="button" href="${p.loginUrl}">
                <#if p.iconClasses?has_content>
                  <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                  <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${p.displayName!}</span>
                <#else>
                  <span class="${properties.kcFormSocialAccountNameClass!}">${p.displayName!}</span>
                </#if>
              </a>
            </li>
          </#list>
        </ul>
      </section>
    </#if>
  </#if>
</@layout.registrationLayout>
