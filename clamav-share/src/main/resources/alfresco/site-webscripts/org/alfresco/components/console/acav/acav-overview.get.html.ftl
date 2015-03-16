<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe>
<![endif]-->
<input id="yui-history-field" type="hidden" />

<#if antivirus?? && antivirus.virus_definitions??>
<#assign virus_definitions = antivirus.virus_definitions?datetime?string("yyyy-MM-dd HH:mm:ss")>
<#else>
<#assign virus_definitions = "">
</#if>

<#assign el=args.htmlid?html>
<script type="text/javascript">
   new Redpill.AcavOverview("${el}").setOptions({
      enabled: ${antivirus.enabled?string}
   }).setMessages(
      ${messages}
   );
</script>

<div id="${el}-body" class="console-tool form-generic-tool">

   <div id="${el}-main" class="hidden">
      <h1 class="thin dark">${msg("tool.acav-overview.label")}</h1>
      <div id="${el}-antivirus-form" class="share-form">
         <div id="${el}-antivirus-form-container" class="form-container">
            <div id="${el}-antivirus-form-fields" class="form-fields">
               <div class="set">
                  <div class="set-title">${msg("acav.set.antivirus")}</div>
                  <@renderField "acav.fields.enabled" msg(antivirus.enabled?string) />
                  <@renderField "acav.fields.virus-definitions" virus_definitions />
                  <@renderField "acav.fields.last-scan" antivirus.last_scan?datetime?string("yyyy-MM-dd HH:mm:ss") />
                  <@renderField "acav.fields.status" msg(antivirus.status)?html />
               </div>
               <div class="set">
                  <div id="${el}-antivirus-operations" class="form-field">
                     <div id="${el}-antivirus-operations-buttons">
                        <button type="button" name="${el}-enable-antivirus-button" id="${el}-enable-antivirus-button">${msg("button.enable")}</button>
                        <button type="button" name="${el}-disable-antivirus-button" id="${el}-disable-antivirus-button">${msg("button.disable")}</button>
                     </div>
                  </div>
               </div>
            </div>
         </div>
      </div>

      <div id="${el}-update-form" class="share-form">
         <div id="${el}-update-form-container" class="form-container">
            <div id="${el}-update-form-fields" class="form-fields">
               <div class="set">
                  <div class="set-title">${msg("acav.set.update")}</div>
                  <div class="yui-g">
                     <div class="yui-u first">
                        <div class="form-field">
                           <div class="viewmode-field">
                              <span class="viewmode-label">${msg("acav.fields.cron-update")}:</span>
                              <span class="viewmode-value"><input type="text" value="${update.cron_expression}" id="${el}-acav.fields.cron-update" /></span>
                              <span class="viewmode-value"><button type="button" id="${el}-cron-save-button" /></span>
                           </div>
                        </div>
                     </div>
                  </div>
                  <div class="yui-g">
                     <div class="yui-u first">
                        <div class="form-field">
                           <div class="viewmode-field">
                              <span class="viewmode-label">${msg("acav.fields.online-update")}:</span>
                              <span class="viewmode-value"><button type="button" id="${el}-online-update-button">${msg("button.online-update")}</button></span>
                           </div>
                        </div>
                     </div>
                  </div>
               </div>
            </div>
         </div>
      </div>

   </div>

</div>

<#macro renderField id value>
<div class="yui-g">
   <div class="yui-u first">
      <div class="form-field">
         <div class="viewmode-field">
            <span id="${el}-${id}-label" class="viewmode-label">${msg(id)}:</span>
            <span id="${el}-${id}-value" class="viewmode-value">${value}</span>
         </div>
      </div>
   </div>
</div>
</#macro>