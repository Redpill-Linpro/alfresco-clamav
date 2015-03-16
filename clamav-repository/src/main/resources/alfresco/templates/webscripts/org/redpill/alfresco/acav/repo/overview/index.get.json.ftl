<#compress>
{
   "antivirus": {
      "enabled": ${enabled?string},
      "virus_definitions": <#if virus_definitions??>${virus_definitions?c}<#else>0</#if>,
      "last_scan": <#if last_scan??>${last_scan?c}<#else>0</#if>,
      "status": "${acavStatus!""}"
   },
   "update": {
      "cron_expression": "${update_cron_expression!""}"
   }
}
</#compress>