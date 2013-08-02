(function(onReady) {

   Alfresco.component.NodeHeader.prototype.onReady = function() {
      onReady.call(this);

      YAHOO.Bubbling.on("metadataRefresh", this._renderInfectedBanner, this);

      this._renderInfectedBanner();
   };

   Alfresco.component.NodeHeader.prototype._renderInfectedBanner = function() {
      var ms = new Date().getTime();

      Alfresco.util.Ajax.jsonGet({
         url : Alfresco.constants.PROXY_URI + 'slingshot/doclib2/node/' + this.options.nodeRef.replace('://', '/') + "?noCache=" + ms,
         successCallback : {
            fn : function(response) {
               this._constructInfectedBannerHTML(response);
            },
            scope : this
         },
         failureMessage : this.msg("message.failure")
      });
   };

   Alfresco.component.NodeHeader.prototype._constructInfectedBannerHTML = function(response) {
      var status = response.json.item.node.properties['acav:scanStatus'];
      var virusName = response.json.item.node.properties['acav:virusName'];

      if (status != 'INFECTED') {
         return;
      }

      var nodeHeader = YAHOO.util.Selector.query('div.node-header')[0];
      var statusBanner = YAHOO.util.Selector.query('div.status-banner')[0];
      var nextElement = statusBanner ? statusBanner : nodeHeader.firstChild;

      var infectedBanner = new YAHOO.util.Element(document.createElement('div'));
      infectedBanner.addClass('status-banner');
      infectedBanner.addClass('theme-bg-color-2');
      infectedBanner.addClass('theme-border-4');
      YAHOO.util.Dom.insertBefore(infectedBanner, nextElement);

      var span = new YAHOO.util.Element(document.createElement('span'));
      span.addClass('infected');
      YAHOO.util.Dom.get(span).innerHTML = this._getInfectedBannerText(virusName);
      infectedBanner.appendChild(span);
   };

   Alfresco.component.NodeHeader.prototype._getInfectedBannerText = function(virusName) {
      if (!virusName) {
         virusName = this.msg('unknown');
      } else {
         virusName = "'" + virusName + "'";
      }

      return this.msg('infected.banner') + " " + virusName + " virus!";
   };

}(Alfresco.component.NodeHeader.prototype.onReady));
