<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new Redpill.AcavConsole("${el}").setMessages(
      ${messages}
   );
//]]></script>

<div id="${el}-body" class="scan-report">
   <div id="${el}-main" class="hidden">
	   <div id="${el}-inputTabs" class="yui-navset">
         <ul class="yui-nav">
            <li class="selected"><a href="#itab1"><em>${msg("tab.label.system-scan")}</em></a></li>
            <li><a href="#itab2"><em>${msg("tab.label.scan-log")}</em></a></li>
            <li><a href="#itab3"><em>${msg("tab.label.found-infected")}</em></a></li>
         </ul>
         
         <div id="${el}-inputContentArea" class="yui-content">
            <div>
               <div>
                  <label for="${el}-directorySelect" id="${el}-directorySelectLabel">${msg("directory.label")}:</label>
                  <select name="${el}-directorySelect" id="${el}-directorySelect">
                     <#list directories as directory>
                     <option value="${directory}">${directory}</option>
                     </#list>
                  </select>
               </div>
               <div>
                  <button type="button" name="${el}-scan-button" id="${el}-scan-button">${msg("button.scan")}</button>
               </div>
               <div class="share-form hidden" id="${el}-details">
                  <div class="form-container">
                     <div class="form-fields">
                        <div class="set">
                           <div class="set-title">${msg("scan-details.label")}</div>
                           <div class="yui-g">
                              <div class="yui-u first">
                                 <@renderField "${msg('known-viruses.label')}:" "" "known-viruses" />
                              </div>
                              <div class="yui-u">
                                 <@renderField "${msg('engine-version.label')}:" "" "engine-version" />
                              </div>
                           </div>
                           <div class="yui-g">
                              <div class="yui-u first">
                                 <@renderField "${msg('scanned-directories.label')}:" "" "scanned-directories" />
                              </div>
                              <div class="yui-u">
                                 <@renderField "${msg('scanned-files.label')}:" "" "scanned-files" />
                              </div>
                           </div>
                           <div class="yui-g">
                              <div class="yui-u first">
                                 <@renderField "${msg('infected-files.label')}:" "" "infected-files" />
                              </div>
                              <div class="yui-u">
                                 <@renderField "${msg('data-scanned.label')}:" "" "data-scanned" />
                              </div>
                           </div>
                           <div class="yui-g">
                              <div class="yui-u first">
                                 <@renderField "${msg('data-read.label')}:" "" "data-read" />
                              </div>
                              <div class="yui-u">
                                 <@renderField "${msg('time.label')}:" "" "time" />
                              </div>
                           </div>
                           <div class="yui-g">
                              <div class="yui-u first">
                                 <@renderField "${msg('directory.label')}:" "" "directory" />
                              </div>
                           </div>
                        </div>
                     </div>
                  </div>
               </div>
               <div>
                  <div id="${el}-scan-result" class="scan-result"></div>
               </div>
            </div>
            
            <div>
               <div class="header-bar">${msg("scan-log-span.label")}</div>
               <div class="datefield">
                  <label for="${el}-scan-log-from-date">${msg("scan-log-from-date.label")}</label>
                  <input id="${el}-scan-log-from-date" type="text" name="scan-log-from-date" class="date-entry" maxlength="10" value="${from_date}" />
                  <input id="${el}-scan-log-from-time" type="text" name="scan-log-from-time" class="time-entry" maxlength="5" value="${from_time}"/>
               </div>
            
               <div class="datefield">
                  <label for="${el}-scan-log-to-date">${msg("scan-log-to-date.label")}</label>
                  <input id="${el}-scan-log-to-date" type="text" name="scan-log-to-date" class="date-entry" maxlength="10" value="${to_date}" />
                  <input id="${el}-scan-log-to-time" type="text" name="scan-log-to-time" class="time-entry" maxlength="5" value="${to_time}" />
               </div>
               <div>
                  <button type="button" name="${el}-scan-log-button" id="${el}-scan-log-button">${msg("button.scan-log")}</button>
               </div>
               <div>
                  <div id="${el}-scan-log-result" class="scan-log-result"></div>
               </div>
            </div>
            
            <div>
            Tab 3
            </div>
         </div>
	   </div>
	</div>
</div>

<#macro renderField label value id>
   <div class="form-field">
      <div class="viewmode-field">
         <span class="viewmode-label">${label}</span>
         <span id="${el}-${id}" class="viewmode-value">${value}</span>
      </div>
   </div>
</#macro>