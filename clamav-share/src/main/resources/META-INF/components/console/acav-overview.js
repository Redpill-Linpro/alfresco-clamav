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
 * Admin Console ACAV Overview
 * 
 * @namespace Redpill
 * @class Redpill.AcavOverview
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   var $html = Alfresco.util.encodeHTML;

   var Bubbling = YAHOO.Bubbling;

   /**
    * AcavOverview constructor.
    * 
    * @param {String}
    *           htmlId The HTML id of the parent element
    * @return {Redpill.AcavOverview} The new AcavOverview instance
    * @constructor
    */
   Redpill.AcavOverview = function(htmlId) {
      this.name = "Redpill.AcavOverview";

      Redpill.AcavOverview.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require([ "button", "container", "datasource", "datatable", "paginator", "json", "history", "tabview", "connection" ], this.onComponentsLoaded, this);

      /* Define panel handlers */
      var parent = this;

      /* File List Panel Handler */
      ListPanelHandler = function() {
         ListPanelHandler.superclass.constructor.call(this, "main");
      };

      YAHOO.extend(ListPanelHandler, Alfresco.ConsolePanelHandler, {
         /**
          * Called by the ConsolePanelHandler when this panel shall be loaded
          * 
          * @method onLoad
          */
         onLoad : function onLoad() {
            // Buttons
            parent.widgets.enableButton = Alfresco.util.createYUIButton(parent, "enable-antivirus-button", parent.onEnableClick);
            parent.widgets.disableButton = Alfresco.util.createYUIButton(parent, "disable-antivirus-button", parent.onDisableClick);
            parent.widgets.cronSaveButton = Alfresco.util.createYUIButton(parent, "cron-save-button", parent.onCronSaveClick);
            parent.widgets.cronSaveButton.addClass("cron-save-button");
            parent.widgets.onlineUpdateButton = Alfresco.util.createYUIButton(parent, "online-update-button", parent.onOnlineUpdateClick);
            parent.widgets.onlineUpdateButton.addClass("online-update-button");

            parent.widgets.enabledText = Dom.get(parent.id + "-acav.fields.enabled-value");

            YAHOO.Bubbling.on("acav.toggle_state", parent.onToggleState, parent);

            YAHOO.Bubbling.fire('acav.toggle_state', {
               enabled : parent.options.enabled
            });
         }
      });

      new ListPanelHandler();

      return this;
   };

   YAHOO.extend(Redpill.AcavOverview, Alfresco.ConsoleTool, {

      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation of YUI widgets and event listener binding.
       * 
       * @method onReady
       */
      onReady : function() {
         var self = this;

         // Call super-class onReady() method
         Redpill.AcavOverview.superclass.onReady.call(this);
      },

      onEnableClick : function() {
         Alfresco.util.Ajax.jsonPost({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/acav/overview/enable",
            dataObj : {
               "noCache" : new Date().getTime()
            },
            successCallback : {
               fn : function() {
                  YAHOO.Bubbling.fire('acav.toggle_state', {
                     enabled : true
                  });
               },
               scope : this
            },
            failureMessage : this.msg("message.enable.failure")
         });
      },

      onDisableClick : function() {
         Alfresco.util.Ajax.jsonPost({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/acav/overview/disable",
            dataObj : {
               "noCache" : new Date().getTime()
            },
            successCallback : {
               fn : function() {
                  YAHOO.Bubbling.fire('acav.toggle_state', {
                     enabled : false
                  });
               },
               scope : this
            },
            failureMessage : this.msg("message.disable.failure")
         });
      },

      onCronSaveClick : function() {
         var cronExpression = Dom.get(this.id + "-acav.fields.cron-update").value;

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/acav/overview/savecron",
            method : Alfresco.util.Ajax.POST,
            dataObj : {
               "cronExpression" : cronExpression,
               "noCache" : new Date().getTime()
            },
            successCallback : {
               fn : function() {
                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("msg.saved"),
                     displayTime : 3
                  });
               },
               scope : this
            },
            failureMessage : this.msg("message.failure")
         });
      },

      onOnlineUpdateClick : function() {
         Alfresco.util.Ajax.jsonPost({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/acav/overview/update",
            dataObj : {
               "noCache" : new Date().getTime()
            },
            successCallback : {
               fn : function() {
                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("msg.update-started"),
                     displayTime : 3
                  });
                  
                  var timer = YAHOO.lang.later(3000, this, function() {
                     this.checkIfUpdateReady(timer);
                  }, null, true);
               },
               scope : this
            },
            failureMessage : this.msg("message.failure")
         });
      },

      checkIfUpdateReady : function(timer) {
         Alfresco.util.Ajax.jsonGet({
            url : Alfresco.constants.PROXY_URI + "org/redpill/alfresco/acav/overview",
            dataObj : {
               "noCache" : new Date().getTime()
            },
            successCallback : {
               fn : function(response) {
                  var status = response.json.antivirus.status;

                  Dom.get(this.id + '-acav.fields.status-value').innerHTML = this.msg(status);

                  if (status != 'UPDATING') {
                     timer.cancel();
                  }
               },
               scope : this
            },
            failureMessage : this.msg("message.enable.failure")
         });
      },

      onToggleState : function(e, args, scope) {
         var enabled = args[1].enabled;

         if (enabled) {
            this.widgets.enableButton.addClass("hidden");
            this.widgets.disableButton.removeClass("hidden");

            Dom.removeClass(this.id + "-update-form", "hidden");

            this.widgets.enabledText.innerHTML = this.msg("true");
         } else {
            this.widgets.enableButton.removeClass("hidden");
            this.widgets.disableButton.addClass("hidden");

            Dom.addClass(this.id + "-update-form", "hidden");

            this.widgets.enabledText.innerHTML = this.msg("false");
         }

         this.options.enabled = enabled;
      }

   });
})();
