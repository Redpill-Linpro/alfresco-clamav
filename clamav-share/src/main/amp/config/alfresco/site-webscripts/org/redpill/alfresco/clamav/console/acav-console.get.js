/**
 * Admin Console ACAV component
 */

function main() {
   // Check for IMAP server status
   var json = remote.call("/org/redpill/alfresco/clamav/directory");

   var directories = eval('(' + json + ')');

   model.directories = directories;
   
   var currentTime = new Date();
   
   var year = currentTime.getFullYear();
   
   var month = currentTime.getMonth() + 1;
   if (month < 10) {
      month = "0" + month;
   }
   
   var day = currentTime.getDate();
   if (day < 10) {
      day = "0" + day;
   }
   
   var from_hour = currentTime.getHours() - 1;
   if (from_hour < 10) {
      from_hour = "0" + from_hour;
   }
   
   var to_hour = currentTime.getHours() + 1;
   if (to_hour < 10) {
      to_hour = "0" + to_hour;
   }
   
   model.from_date = year + "-" + month + "-" + day;
   model.to_date = model.from_date;
   
   model.from_time = from_hour + ":00";
   model.to_time = to_hour + ":00";
}

main();
