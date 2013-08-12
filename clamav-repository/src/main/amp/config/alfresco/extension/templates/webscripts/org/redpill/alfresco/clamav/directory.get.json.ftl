<#compress>
[
   <#list directories as directory>
   "${directory}"<#if directory_has_next>,</#if>
   </#list>
] 
</#compress>