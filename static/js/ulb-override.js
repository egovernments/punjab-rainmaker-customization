var appOverrides = (function() {
  var INIT_VALUES = {
    pbFireCess: {
      inflammable: false,
      heightAbove36Feet: false,
    },
  };
  var state = JSON.parse(JSON.stringify(INIT_VALUES));

  var pbFireCess = {
    fireCessOnChange: function(e) {
      state.pbFireCess[e.target.id] = e.target.checked;
    },
    initForm: function() {
      var basicInfoEle = document.getElementById("basicInformation");
      var newEle = document.getElementById("pbFireCess");

      if (basicInfoEle != null && basicInfoEle != undefined && (newEle == null && newEle == undefined)) {
        var fireCessNode = document.createElement("div");
        fireCessNode.innerHTML =
          ' <form id="pbFireCess"> <div class="rainmaker-card clearfix "> <div class="pbFireCess col-xs-12"> <div class="col-xs-6"> <div style="cursor: pointer; position: relative; overflow: visible; display: table; height: auto; width: 100%; margin-bottom: 11px; margin-top: 17px;"> <input id="inflammable" type="checkbox" style="position: absolute;cursor: inherit;pointer-events: all;z-index: 2;left: 0px;box-sizing: border-box;transform: scale(1.5);"> <div style="display: flex; width: 100%; height: 100%;"> <label style="float: left; position: relative; display: block; width: calc(100% - 60px); color: rgb(0, 188, 209); font-family: Roboto, sans-serif; font-weight:100; margin-left:20px;">Do you have any inflammable material stored in your property?</label> </div> </div> </div> <div class="col-xs-6"> <div style="cursor: pointer; position: relative; overflow: visible; display: table; height: auto; width: 100%; margin-bottom: 11px; margin-top: 17px;"> <input id="heightAbove36Feet" type="checkbox" style="position: absolute;cursor: inherit;pointer-events: all;z-index: 2;left: 0px;box-sizing: border-box;transform: scale(1.5);"> <div style="display: flex; width: 100%; height: 100%;"> <div></div> <div style="position: absolute; height: 100%; width: 100%; top: 0px; left: 0px;"></div> </div> <label style="float: left; position: relative; display: block; width: calc(100% - 60px); color: rgb(0, 188, 209); font-family: Roboto, sans-serif; font-weight:100; margin-left:20px;">Height of property more than 36 feet?</label> </div> </div> </div> </div> </div> </form>';
        basicInfoEle.parentNode.insertBefore(fireCessNode, basicInfoEle.nextSibling);

        var inflammable = document.getElementById("inflammable");
        var heightAbove36Feet = document.getElementById("heightAbove36Feet");

        inflammable.checked = state.pbFireCess.inflammable;
        heightAbove36Feet.checked = state.pbFireCess.heightAbove36Feet;
        var tab = document.getElementsByClassName("form-tab-index").item(0)
        if (tab.textContent == "4") {
          inflammable.disabled = 'disabled'
          heightAbove36Feet.disabled = 'disabled'
        }

        inflammable.addEventListener("change", pbFireCess.fireCessOnChange);
        heightAbove36Feet.addEventListener("change", pbFireCess.fireCessOnChange);
      }
    },
    resetForm: function(data) {
      state.pbFireCess = JSON.parse(JSON.stringify(INIT_VALUES.pbFireCess));
    },
    updateForm: function(data) {
      var property = data.Properties[0];
      if (property && property.propertyDetails && property.propertyDetails[0].additionalDetails) {
        var imflammable = property.propertyDetails[0].additionalDetails.inflammable;
        var heightAbove36Feet = property.propertyDetails[0].additionalDetails.heightAbove36Feet;

        if (imflammable !== undefined && imflammable !== null) {
          state.pbFireCess.inflammable = imflammable;
          if (document.getElementById("inflammable")) document.getElementById("inflammable").checked = imflammable;
        }

        if (heightAbove36Feet !== undefined && heightAbove36Feet !== null) {
          state.pbFireCess.heightAbove36Feet = heightAbove36Feet;
          if (document.getElementById("heightAbove36Feet")) document.getElementById("heightAbove36Feet").checked = heightAbove36Feet;
        }
      }
    },
    submitForm: function(property) {
      if (property && property[0].propertyDetails) {
        property[0].propertyDetails[0]["additionalDetails"] = state.pbFireCess;
      }
    },
  };

  var API = {
    initForm: function(formKey, data) {
      switch (formKey) {
        case "basicInformation":
          return pbFireCess.initForm(data);
        default:
          return;
      }
    },
    resetForm: function(formKey, data) {
      switch (formKey) {
        case "basicInformation":
          return pbFireCess.resetForm(data);
        default:
          return;
      }
    },
    validateForm: function(formKey, data) {
      switch (formKey) {
        default:
          return true;
      }
    },
    updateForms: function(data) {
      pbFireCess.updateForm(data);
      return;
    },
    submitForm: function(data) {
      pbFireCess.submitForm(data);
      return;
    },
  };
  return API;
})();

