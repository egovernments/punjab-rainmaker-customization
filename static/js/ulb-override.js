var appOverrides = (function () {
  var INIT_VALUES = {
      pbFireCess: {
          inflammable: false,
          heightAbove36Feet: false,
      },
      customFields: {
      }
  };
  var state = JSON.parse(JSON.stringify(INIT_VALUES));

  var pbCustomization = {
      fireCessOnChange: function (e) {
          state.pbFireCess[e.target.id] = e.target.checked;
      },
      customTextOnChange: function (e) {
          state.customFields[e.target.id] = e.target.value;
      },
      customDateOnChange: function (e) {
          if (e.target.valueAsNumber)
              state.customFields[e.target.id] = e.target.valueAsNumber;
      },
      initForm: function () {
          var basicInfoEle = document.getElementById("basicInformation");
          var newEle = document.getElementById("pbFireCess");

          if (basicInfoEle != null && basicInfoEle != undefined && (newEle == null && newEle == undefined)) {
              var fireCessNode = document.createElement("div");
              var customFieldsNode = document.createElement("div");
              fireCessNode.innerHTML =
                  ' <form id="pbFireCess"> <div class="rainmaker-card clearfix "> <div class="pbFireCess col-xs-12"> <div class="col-xs-6"> <div style="cursor: pointer; position: relative; overflow: visible; display: table; height: auto; width: 100%; margin-bottom: 11px; margin-top: 17px;"> <input id="inflammable" type="checkbox" style="position: absolute;cursor: inherit;pointer-events: all;z-index: 2;left: 0px;box-sizing: border-box;transform: scale(1.5);"> <div style="display: flex; width: 100%; height: 100%;"> <label style="float: left; position: relative; display: block; width: calc(100% - 60px); color: rgb(0, 188, 209); font-family: Roboto, sans-serif; font-weight:100; margin-left:20px;">Do you have any inflammable material stored in your property?</label> </div> </div> </div> <div class="col-xs-6"> <div style="cursor: pointer; position: relative; overflow: visible; display: table; height: auto; width: 100%; margin-bottom: 11px; margin-top: 17px;"> <input id="heightAbove36Feet" type="checkbox" style="position: absolute;cursor: inherit;pointer-events: all;z-index: 2;left: 0px;box-sizing: border-box;transform: scale(1.5);"> <div style="display: flex; width: 100%; height: 100%;"> <div></div> <div style="position: absolute; height: 100%; width: 100%; top: 0px; left: 0px;"></div> </div> <label style="float: left; position: relative; display: block; width: calc(100% - 60px); color: rgb(0, 188, 209); font-family: Roboto, sans-serif; font-weight:100; margin-left:20px;">Height of property more than 36 feet?</label> </div> </div> </div> </div> </div> </form>';
              basicInfoEle.parentNode.insertBefore(fireCessNode, basicInfoEle.nextSibling);

              customFieldsNode.innerHTML = '<form id="pbCustomFields"> <div class="rainmaker-card clearfix "> <div class="pbCustomFields col-xs-12"> <div class="col-sm-6" style="height: 80px;"> <div class="textfield " style="font-size: 16px; line-height: 24px; width: 100%; height: 72px; display: inline-block; position: relative; background-color: rgb(255, 255, 255); font-family: Roboto; transition: height 200ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; cursor: auto;"><label for="vasikaNo" style="position: absolute; line-height: 22px; top: 30px; transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; z-index: 0; transform: scale(1) translate(0px, -16px); transform-origin: left top; pointer-events: none; user-select: none; color: rgba(0, 0, 0, 0.6); font-size: 12px; letter-spacing: 0.6px; font-weight: 500;">Vaski No</label> <div style="position: absolute; opacity: 1; color: rgba(0, 0, 0, 0.38); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; bottom: 12px; font-size: 16px; letter-spacing: 0.7px;"></div><input type="text" maxlength="64" autocomplete="off" numcols="6" id="vasikaNo" value="" style="padding: 0px 0px 10px; position: relative; width: 100%; border: none; outline: none; background-color: rgba(0, 0, 0, 0); color: rgb(72, 72, 72); cursor: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-stretch: inherit; font-size: 16px; line-height: inherit; font-family: inherit; opacity: 1; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); height: 100%; box-sizing: border-box; margin-top: 14px; letter-spacing: 0.7px;"> <div> <hr aria-hidden="true" style="border-top: none rgb(230, 230, 230); border-left: none rgb(230, 230, 230); border-right: none rgb(230, 230, 230); border-bottom: 1px solid rgb(230, 230, 230); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%;"> <hr aria-hidden="true" style="border-top: none rgb(224, 224, 224); border-left: none rgb(224, 224, 224); border-right: none rgb(224, 224, 224); border-bottom: 2px solid rgb(224, 224, 224); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%; transform: scaleX(0); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms;"> </div></div></div><div class="col-sm-6" style="height: 80px;"> <div class="textfield " style="font-size: 16px; line-height: 24px; width: 100%; height: 72px; display: inline-block; position: relative; background-color: rgb(255, 255, 255); font-family: Roboto; transition: height 200ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; cursor: auto;"><label for="vasikaDate" style="position: absolute; line-height: 22px; top: 30px; transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; z-index: 0; transform: scale(1) translate(0px, -16px); transform-origin: left top; pointer-events: none; user-select: none; color: rgba(0, 0, 0, 0.6); font-size: 12px; letter-spacing: 0.6px; font-weight: 500;">Vaski Date</label> <div style="position: absolute; opacity: 1; color: rgba(0, 0, 0, 0.38); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; bottom: 12px; font-size: 16px; letter-spacing: 0.7px;"></div><input type="date" maxlength="64" autocomplete="off" numcols="6" id="vasikaDate" value="" style="padding: 0px 0px 10px; position: relative; width: 100%; border: none; outline: none; background-color: rgba(0, 0, 0, 0); color: rgb(72, 72, 72); cursor: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-stretch: inherit; font-size: 16px; line-height: inherit; font-family: inherit; opacity: 1; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); height: 100%; box-sizing: border-box; margin-top: 14px; letter-spacing: 0.7px;"> <div> <hr aria-hidden="true" style="border-top: none rgb(230, 230, 230); border-left: none rgb(230, 230, 230); border-right: none rgb(230, 230, 230); border-bottom: 1px solid rgb(230, 230, 230); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%;"> <hr aria-hidden="true" style="border-top: none rgb(224, 224, 224); border-left: none rgb(224, 224, 224); border-right: none rgb(224, 224, 224); border-bottom: 2px solid rgb(224, 224, 224); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%; transform: scaleX(0); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms;"> </div></div></div><div class="col-sm-6" style="height: 80px;"> <div class="textfield " style="font-size: 16px; line-height: 24px; width: 100%; height: 72px; display: inline-block; position: relative; background-color: rgb(255, 255, 255); font-family: Roboto; transition: height 200ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; cursor: auto;"><label for="allotmentNo" style="position: absolute; line-height: 22px; top: 30px; transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; z-index: 0; transform: scale(1) translate(0px, -16px); transform-origin: left top; pointer-events: none; user-select: none; color: rgba(0, 0, 0, 0.6); font-size: 12px; letter-spacing: 0.6px; font-weight: 500;">Allotment Letter No</label> <div style="position: absolute; opacity: 1; color: rgba(0, 0, 0, 0.38); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; bottom: 12px; font-size: 16px; letter-spacing: 0.7px;"></div><input type="text" maxlength="64" autocomplete="off" numcols="6" id="allotmentNo" value="" style="padding: 0px 0px 10px; position: relative; width: 100%; border: none; outline: none; background-color: rgba(0, 0, 0, 0); color: rgb(72, 72, 72); cursor: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-stretch: inherit; font-size: 16px; line-height: inherit; font-family: inherit; opacity: 1; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); height: 100%; box-sizing: border-box; margin-top: 14px; letter-spacing: 0.7px;"> <div> <hr aria-hidden="true" style="border-top: none rgb(230, 230, 230); border-left: none rgb(230, 230, 230); border-right: none rgb(230, 230, 230); border-bottom: 1px solid rgb(230, 230, 230); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%;"> <hr aria-hidden="true" style="border-top: none rgb(224, 224, 224); border-left: none rgb(224, 224, 224); border-right: none rgb(224, 224, 224); border-bottom: 2px solid rgb(224, 224, 224); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%; transform: scaleX(0); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms;"> </div></div></div><div class="col-sm-6" style="height: 80px;"> <div class="textfield " style="font-size: 16px; line-height: 24px; width: 100%; height: 72px; display: inline-block; position: relative; background-color: rgb(255, 255, 255); font-family: Roboto; transition: height 200ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; cursor: auto;"><label for="allotmentDate" style="position: absolute; line-height: 22px; top: 30px; transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; z-index: 0; transform: scale(1) translate(0px, -16px); transform-origin: left top; pointer-events: none; user-select: none; color: rgba(0, 0, 0, 0.6); font-size: 12px; letter-spacing: 0.6px; font-weight: 500;">Allotment Date</label> <div style="position: absolute; opacity: 1; color: rgba(0, 0, 0, 0.38); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms; bottom: 12px; font-size: 16px; letter-spacing: 0.7px;"></div><input type="date" maxlength="64" autocomplete="off" numcols="6" id="allotmentDate" value="" style="padding: 0px 0px 10px; position: relative; width: 100%; border: none; outline: none; background-color: rgba(0, 0, 0, 0); color: rgb(72, 72, 72); cursor: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; font-stretch: inherit; font-size: 16px; line-height: inherit; font-family: inherit; opacity: 1; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); height: 100%; box-sizing: border-box; margin-top: 14px; letter-spacing: 0.7px;"> <div> <hr aria-hidden="true" style="border-top: none rgb(230, 230, 230); border-left: none rgb(230, 230, 230); border-right: none rgb(230, 230, 230); border-bottom: 1px solid rgb(230, 230, 230); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%;"> <hr aria-hidden="true" style="border-top: none rgb(224, 224, 224); border-left: none rgb(224, 224, 224); border-right: none rgb(224, 224, 224); border-bottom: 2px solid rgb(224, 224, 224); bottom: 8px; box-sizing: content-box; margin: 0px; position: absolute; width: 100%; transform: scaleX(0); transition: all 450ms cubic-bezier(0.23, 1, 0.32, 1) 0ms;"> </div></div></div></div></div></form>';
              basicInfoEle.parentNode.insertBefore(customFieldsNode, basicInfoEle.nextSibling);

              var inflammable = document.getElementById("inflammable");
              var heightAbove36Feet = document.getElementById("heightAbove36Feet");

              var vasikaNo = document.getElementById("vasikaNo")
              var vasikaDate = document.getElementById("vasikaDate")
              var allotmentNo = document.getElementById("allotmentNo")
              var allotmentDate = document.getElementById("allotmentDate")

              inflammable.checked = state.pbFireCess.inflammable;
              heightAbove36Feet.checked = state.pbFireCess.heightAbove36Feet;

              vasikaNo.value = state.customFields.vasikaNo || "";
              vasikaDate.valueAsNumber = state.customFields.vasikaDate ;
              allotmentNo.value = state.customFields.allotmentNo || "";
              allotmentDate.valueAsNumber = state.customFields.allotmentDate;
              
              var tab = document.getElementsByClassName("form-tab-index")
              if (
                  (tab.length > 0 && tab.item(0).textContent == "4") ||
                  document.getElementsByClassName("active-step-3").length > 0

              ) {
                  inflammable.disabled = 'disabled'
                  heightAbove36Feet.disabled = 'disabled'
                  vasikaNo.disabled = 'disabled'
                  vasikaDate.disabled = 'disabled'
                  allotmentNo.disabled = 'disabled'
                  allotmentDate.disabled = 'disabled'
              }

              vasikaNo.addEventListener("change", pbCustomization.customTextOnChange)
              allotmentNo.addEventListener("change", pbCustomization.customTextOnChange)
              vasikaDate.addEventListener("change", pbCustomization.customDateOnChange)
              allotmentDate.addEventListener("change", pbCustomization.customDateOnChange)

              inflammable.addEventListener("change", pbCustomization.fireCessOnChange);
              heightAbove36Feet.addEventListener("change", pbCustomization.fireCessOnChange);
          }
      },
      resetForm: function (data) {
          state.pbFireCess = JSON.parse(JSON.stringify(INIT_VALUES.pbFireCess));
          state.customFields = JSON.parse(JSON.stringify(INIT_VALUES.customFields));
      },
      updateForm: function (data) {
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

          if (property && property.additionalDetails) { 
              var vasikaNo = property.additionalDetails.vasikaNo;
              var vasikaDate = property.additionalDetails.vasikaDate;
              var allotmentDate = property.additionalDetails.allotmentDate;
              var allotmentNo = property.additionalDetails.allotmentNo;
              
              var vasikaNoElem = document.getElementById("vasikaNo")
              var vasikaDateElem = document.getElementById("vasikaDate")
              var allotmentNoElem = document.getElementById("allotmentNo")
              var allotmentDateElem = document.getElementById("allotmentDate")

              if (vasikaNo !== undefined && vasikaNo !== null && vasikaNoElem !== null) {
                  vasikaNoElem.value = vasikaNo
                  state.customFields.vasikaNo = vasikaNo
              }

              if (allotmentNo !== undefined && allotmentNo !== null && allotmentNoElem !== null) {
                  allotmentNoElem.value = allotmentNo
                  state.customFields.allotmentNo = allotmentNo
              }

              if (vasikaDate !== undefined && vasikaDate !== null && vasikaDateElem !== null) {
                  vasikaDateElem.valueAsNumber = vasikaDate
                  state.customFields.vasikaDate = vasikaDate
              }

              if (allotmentDate !== undefined && allotmentDate !== null && allotmentDateElem !== null) {
                  allotmentDateElem.valueAsNumber = allotmentDate
                  state.customFields.allotmentDate = allotmentDate
              }
          }
      },
      submitForm: function (property) {
          if (property && property[0].propertyDetails) {
              property[0].propertyDetails[0]["additionalDetails"] = Object.assign(property[0].propertyDetails[0]["additionalDetails"] || {}, state.pbFireCess);
          }
          if (property && property[0]) {
              property[0].additionalDetails = Object.assign(property[0].additionalDetails || {}, state.customFields);
          }
      },
  };

  var API = {
      initForm: function (formKey, data) {
          switch (formKey) {
              case "basicInformation":
                  return pbCustomization.initForm(data);
              default:
                  return;
          }
      },
      resetForm: function (formKey, data) {
          switch (formKey) {
              case "basicInformation":
                  return pbCustomization.resetForm(data);
              default:
                  return;
          }
      },
      validateForm: function (formKey, data) {
          switch (formKey) {
              default:
                  return true;
          }
      },
      updateForms: function (data) {
          pbCustomization.updateForm(data);
          return;
      },
      submitForm: function (data) {
          pbCustomization.submitForm(data);
          return;
      },
  };
  return API;
})();