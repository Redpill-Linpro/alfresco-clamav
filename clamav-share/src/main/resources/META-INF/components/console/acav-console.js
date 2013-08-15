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
      Alfresco.util.YUILoaderHelper.require([ "button", "container", "datasource", "datatable", "paginator", "json", "history", "tabview" ], this.onComponentsLoaded, this);

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
            // widgets for the system scan
            parent.setupSystemScanWidgets();

            // widgets for the system log
            parent.setupScanLogWidgets();

            // widgets for the infected nodes list
            parent.setupInfectedNodesWidgets();
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

         this.widgets.inputTabs = new YAHOO.widget.TabView(this.id + "-inputTabs");
      },

      setupSystemScanWidgets : function() {
         this.widgets.scanButton = Alfresco.util.createYUIButton(this, "scan-button", this.onScanClick);
         this.widgets.directorySelect = Dom.get(this.id + "-directorySelect");
         this.widgets.knownViruses = Dom.get(this.id + "-known-viruses");
         this.widgets.engineVersion = Dom.get(this.id + "-engine-version");
         this.widgets.scannedDirectories = Dom.get(this.id + "-scanned-directories");
         this.widgets.scannedFiles = Dom.get(this.id + "-scanned-files");
         this.widgets.infectedFiles = Dom.get(this.id + "-infected-files");
         this.widgets.dataScanned = Dom.get(this.id + "-data-scanned");
         this.widgets.dataRead = Dom.get(this.id + "-data-read");
         this.widgets.time = Dom.get(this.id + "-time");
         this.widgets.directory = Dom.get(this.id + "-directory");
         this.widgets.details = Dom.get(this.id + "-details");
         this.widgets.scanResult = Dom.get(this.id + "-scan-result");
      },

      onScanClick : function() {
         var self = this;

         Dom.addClass(this.widgets.details, "hidden");
         Dom.addClass(this.widgets.scanResult, "hidden");

         var scanningMessage = Alfresco.util.PopupManager.displayMessage({
            displayTime : 0,
            text : '<span class="wait">' + $html(this.msg("message.scanning")) + '</span>',
            noEscape : true,
            modal : true
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
                     scanningMessage.destroy();
                     
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
      },

      setupScanLogWidgets : function() {
         this.widgets.scanLogButton = Alfresco.util.createYUIButton(this, "scan-log-button", this.onScanLogClick);
         this.widgets.scanLogFromDateField = Dom.get(this.id + "-scan-log-from-date");
         this.widgets.scanLogFromTimeField = Dom.get(this.id + "-scan-log-from-time");
         this.widgets.scanLogToDateField = Dom.get(this.id + "-scan-log-to-date");
         this.widgets.scanLogToTimeField = Dom.get(this.id + "-scan-log-to-time");
         
         // Scan Log DataSource
         this.widgets.scanLogDataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "vgr/publishreport", {
            responseType : YAHOO.util.DataSource.TYPE_JSON,
            responseSchema : {
               resultsList : "documents",
               metaFields : {
                  recordOffset : "startIndex",
                  totalRecords : "totalRecords"
               }
            }
         });

         // Setup the main datatable
         // this._setupDataTable();
      },

      onScanLogClick : function() {
         
      },

      setupInfectedNodesWidgets : function() {

      }

   });
})();
