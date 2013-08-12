/**
 * Redpill root namespace.
 * 
 * @namespace Redpill
 */
// Ensure Redpill root object exists
if (typeof Redpill == "undefined" || !Redpill) {
   var Redpill = {};
}

/**
 * Admin Console ACAV Console
 * 
 * @namespace Redpill
 * @class Redpill.AcavConsole
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   var $html = Alfresco.util.encodeHTML;

   var Bubbling = YAHOO.Bubbling;

   /**
    * AcavConsole constructor.
    * 
    * @param {String}
    *           htmlId The HTML id of the parent element
    * @return {Redpill.AcavConsole} The new AcavConsole instance
    * @constructor
    */
   Redpill.AcavConsole = function(htmlId) {
      this.name = "Redpill.AcavConsole";

      Redpill.AcavConsole.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require([ "button", "container", "datasource", "datatable", "paginator", "json", "history" ], this.onComponentsLoaded, this);

      /* Define panel handlers */
      var parent = this;

      /* File List Panel Handler */
      ListPanelHandler = function ListPanelHandler_constructor() {
         ListPanelHandler.superclass.constructor.call(this, "main");
      };

      YAHOO.extend(ListPanelHandler, Alfresco.ConsolePanelHandler, {

         /**
          * Called by the ConsolePanelHandler when this panel shall be loaded
          * 
          * @method onLoad
          */
         onLoad : function onLoad() {
            parent.widgets.scanButton = Alfresco.util.createYUIButton(parent, "scan-button", parent.onScanClick);

            parent.widgets.directorySelect = Dom.get(parent.id + "-directorySelect");
            parent.widgets.knownViruses = Dom.get(parent.id + "-known-viruses");
            parent.widgets.engineVersion = Dom.get(parent.id + "-engine-version");
            parent.widgets.scannedDirectories = Dom.get(parent.id + "-scanned-directories");
            parent.widgets.scannedFiles = Dom.get(parent.id + "-scanned-files");
            parent.widgets.infectedFiles = Dom.get(parent.id + "-infected-files");
            parent.widgets.dataScanned = Dom.get(parent.id + "-data-scanned");
            parent.widgets.dataRead = Dom.get(parent.id + "-data-read");
            parent.widgets.time = Dom.get(parent.id + "-time");
            parent.widgets.directory = Dom.get(parent.id + "-directory");

            parent.widgets.details = Dom.get(parent.id + "-details");
            parent.widgets.scanResult = Dom.get(parent.id + "-scan-result");
         }
      });

      new ListPanelHandler();

      return this;
   };

   YAHOO.extend(Redpill.AcavConsole, Alfresco.ConsoleTool, {

      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation of YUI widgets and event listener binding.
       * 
       * @method onReady
       */
      onReady : function() {
         var self = this;

         // Call super-class onReady() method
         Redpill.AcavConsole.superclass.onReady.call(this);

         // Hook action events
         var fnActionHandler = function(layer, args) {
            var owner = Bubbling.getOwnerByTagName(args[1].anchor, "div");
            if (owner !== null) {
               if (typeof self[owner.className] == "function") {
                  args[1].stop = true;

                  var asset = self.widgets.dataTable.getRecord(args[1].target.offsetParent).getData();
                  var id = self.widgets.dataTable.getRecord(args[1].target.offsetParent).getId();

                  self[owner.className].call(self, asset, id, owner);
               }
            }
            return true;
         };

         Bubbling.addDefaultAction("action-link", fnActionHandler);
      },

      onScanClick : function() {
         var self = this;

         Dom.addClass(this.widgets.details, "hidden");
         Dom.addClass(this.widgets.scanResult, "hidden");

         var scanningMessage = Alfresco.util.PopupManager.displayMessage({
            displayTime : 0,
            text : '<span class="wait">' + $html(this.msg("message.scanning")) + '</span>',
            noEscape : true
         });

         scanningMessage.show();

         Alfresco.util.Ajax.jsonGet({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/clamav/scan",
            dataObj : {
               "directory" : self.widgets.directorySelect.value,
               "noCache" : new Date().getTime()
            },
            successCallback : {
               fn : function(res) {
                  if (res.json.result.length == 0) {
                     return;
                  }

                  self.updateList(res.json.result[0]);

                  scanningMessage.destroy();
               },
               scope : this
            },
            failureCallback : {
               fn : function() {
                  scanningMessage.destroy();

                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg('foobar'),
                     displayTime : 3
                  });
               },
               scope : this
            },
            cache : false
         });
      },

      updateList : function(scanSummary) {
         this.widgets.knownViruses.innerHTML = scanSummary.knownViruses;
         this.widgets.engineVersion.innerHTML = scanSummary.engineVersion;
         this.widgets.scannedDirectories.innerHTML = scanSummary.scannedDirectories;
         this.widgets.scannedFiles.innerHTML = scanSummary.scannedFiles;
         this.widgets.infectedFiles.innerHTML = scanSummary.infectedFiles;
         this.widgets.dataScanned.innerHTML = scanSummary.dataScanned;
         this.widgets.dataRead.innerHTML = scanSummary.dataRead;
         this.widgets.time.innerHTML = scanSummary.time;
         this.widgets.directory.innerHTML = scanSummary.directory;

         Dom.removeClass(this.widgets.details, "hidden");
         Dom.removeClass(this.widgets.scanResult, "hidden");

         this._displayInfectedNodes(scanSummary.infectedNodes);
      },

      _displayInfectedNodes : function(infectedNodes) {
         var self = this;

         this.widgets.dataSource = new YAHOO.util.FunctionDataSource(function() {
            return infectedNodes;
         });

         this.widgets.dataSource.responseSchema = {
            fields : [ "virusName", "nodeRef", "name" ]
         }

         var renderCellName = function(cell, record, column, data) {
            cell.innerHTML = $html(data);
         };

         var renderCellVirusName = function(cell, record, column, data) {
            cell.innerHTML = $html(data);
         };

         var renderCellNodeRef = function(cell, record, column, data) {
            var docDetailsUrl = Alfresco.constants.URL_PAGECONTEXT + 'site/foobar/document-details?nodeRef=' + data;
            
            cell.innerHTML = '<a href="' + docDetailsUrl + '" target="_blank">' + $html(data) + '</a>';
         };

         var renderCellActions = function(cell, record, column, data) {
            cell.innerHTML = '<div class="onActionHandle" style="display: block;"><a href="" class="action-link" title="' + self.msg("action.handle") + '"><span>' + self.msg("action.handle")
                     + '</span></a></div>';
         };

         var columnDefinitions = [ {
            key : "name",
            label : this.msg("label.column.name"),
            sortable : false,
            formatter : renderCellName,
            width : 70
         }, {
            key : "virusName",
            label : this.msg("label.column.virusName"),
            sortable : false,
            formatter : renderCellVirusName
         }, {
            key : "nodeRef",
            label : this.msg("label.column.nodeRef"),
            sortable : false,
            formatter : renderCellNodeRef
         }, {
            key : "actions",
            label : this.msg("label.column.actions"),
            sortable : false,
            formatter : renderCellActions,
            width : 110
         } ];

         this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-scan-result", columnDefinitions, this.widgets.dataSource, {
            MSG_EMPTY : this.msg("message.empty"),
         });
      },

      onActionHandle : function(asset, id) {
         var self = this;

         var waitingMessage = Alfresco.util.PopupManager.displayMessage({
            displayTime : 0,
            text : '<span class="wait">' + $html(this.msg("message.wait")) + '</span>',
            noEscape : true
         });

         waitingMessage.show();

         Alfresco.util.Ajax.jsonGet({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/clamav/handle",
            dataObj : {
               "nodeRef" : asset.nodeRef,
               "name" : asset.name,
               "virusName" : asset.virusName
            },
            successCallback : {
               fn : function(res) {
                  self.widgets.dataTable.deleteRow(id);

                  waitingMessage.destroy();
               },
               scope : this
            },
            failureCallback : {
               fn : function() {
                  waitingMessage.destroy();

                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg('foobar'),
                     displayTime : 3
                  });
               },
               scope : this
            },
            cache : false
         });
      }

   });
})();
