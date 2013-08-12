/**
 * Admin Console ACAV component
 */

function main() {
   // Check for IMAP server status
   var json = remote.call("/org/redpill/alfresco/clamav/directory");

   var directories = eval('(' + json + ')');

   model.directories = directories;
}

main();
