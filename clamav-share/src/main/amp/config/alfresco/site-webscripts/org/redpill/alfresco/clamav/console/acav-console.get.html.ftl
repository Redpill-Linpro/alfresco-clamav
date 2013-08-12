<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new Redpill.AcavConsole("${el}").setMessages(
      ${messages}
   );
//]]></script>

<div id="${el}-body" class="scan-report">
	<div id="${el}-main" class="hidden">
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