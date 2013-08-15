<#compress>
{ 
   "result": [
      <#list scanSummaryList as scanSummary>
      {
         "knownViruses": ${scanSummary.knownViruses?c},
         "engineVersion": "${scanSummary.engineVersion?js_string}",
         "scannedDirectories": ${scanSummary.scannedDirectories?c},
         "scannedFiles": ${scanSummary.scannedFiles?c},
         "infectedFiles": ${scanSummary.infectedFiles?c},
         "dataScanned": "${scanSummary.dataScanned?js_string}",
         "dataRead": "${scanSummary.dataRead?js_string}",
         "time": "${scanSummary.time?js_string}",
         "directory": "${scanSummary.directory?js_string}",
         "infectedNodes": [
            <#list scanSummary.infectedList as infected>
               {
                  "virusName": "${infected.virusName?js_string}",
                  "nodeRef": "${infected.nodeRef?js_string}",
                  "name": "${infected.name?js_string}"
               }
               <#if infected_has_next>,</#if>
            </#list>
         ]
      }<#if scanSummary_has_next>,</#if>
      </#list>
   ] 
}
</#compress>