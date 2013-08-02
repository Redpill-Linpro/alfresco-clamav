(function(_setupMetadataRenderers) {

   Alfresco.DocumentList.prototype._setupMetadataRenderers = function() {
      _setupMetadataRenderers.call(this);

      this.registerRenderer("infectedBanner", function(record, label) {
         var virusName = record.node.properties['acav:virusName'];

         if (!virusName) {
            virusName = this.msg('unknown');
         } else {
            virusName = "'" + virusName + "'";
         }

         var text = this.msg('infected.banner');

         return text + " " + virusName + " virus!";
      });
   };

}(Alfresco.DocumentList.prototype._setupMetadataRenderers));
