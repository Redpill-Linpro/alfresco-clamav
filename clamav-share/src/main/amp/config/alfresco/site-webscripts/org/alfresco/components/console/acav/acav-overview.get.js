function main() {
   var json = remote.call("/org/redpill/alfresco/clamav/overview");

   var overview = eval('(' + json + ')');
   var av = overview.antivirus;
   var update = overview.update;

   av.virus_definitions = av.virus_definitions != 0 ? new Date(av.virus_definitions) : null;
   av.last_scan = av.last_scan != 0 ? new Date(av.last_scan) : null;

   model.antivirus = av;
   model.update = {
      "cron_expression" : update.cron_expression
   };
}

main();
