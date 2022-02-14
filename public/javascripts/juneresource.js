
var filterIndex=0;
var selectedItem, selectedIndex;
var groupChecked = new Array;
var resourceData;

var resourceData = [];

function mergeItem(cssdata){
    $.each(resourceData, function(index, editData){
        $.each(cssdata, function(index, value){
            if(editData.objectId == value.objectId){
                editData.top = value.top;
                editData.right = value.right;
                editData.left = value.left;
                editData.bottom = value.bottom;
                editData.width = value.width;
                editData.height = value.height;
                editData.spritable = 1;
            }
        });
    });
}

function isCdnChecker(cssdata){
    $.each(resourceData, function(index, editData){
        $.each(cssdata, function(index, value){
            if(editData.objectId == value.objectId){
                editData.cdn = true;
            }
        });
    });
}

function loadingModal(who){
    $("#processBox" ).modal("show");

    if(who=="cdn"){
        $("#processBoxTitle" ).text("파일 CDN 설정");
        $("#processBoxTitle2" ).text("선택된 파일을 CDN에 업로드하고 있습니다.");
    }

    if(who=="merge"){
        $("#processBoxTitle" ).text("JavaScript/CSS Merge");
        $("#processBoxTitle2" ).text("선택된 파일을 합치고 있습니다.");
    }

    if(who=="minify"){
        $("#processBoxTitle" ).text("JavaScript/CSS Minify");
        $("#processBoxTitle2" ).text("선택된 파일 최적화를 진행하고 있습니다.");
    }

    if(who=="quality"){
        $("#processBoxTitle" ).text("이미지 품질 조절");
        $("#processBoxTitle2" ).text("이미지 압축을 진행하고 있습니다.");
    }
}

var nowMet;
function finalCheck(met){
    nowMet = met;
    if(nowMet=="minify") $("#applyTitle").text("JavaScript/CSS Minify");
    if(nowMet=="merge") $("#applyTitle").text("JavaScript/CSS Merge");
    if(nowMet=="cdn") $("#applyTitle").text("파일 CDN 설정");
    if(nowMet=="quality") $("#applyTitle").text("이미지 압축");
    if(nowMet=="cache") $("#applyTitle").text("Header Cache-Control");

    $('#applyList tbody').html(""); //
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            $('#applyList tbody').append('<tr><td width="50" height="64" valign="middle"><img src="'+ value.thumb +'" width="45" class="splitImageListImg"></td> <td>'+value.url+'</td></tr>');
        }
    });

    $("#applyModal" ).modal("show");

}


function finalCheckAction(){

    $("#applyModal" ).modal("hide");
    if(nowMet=="minify") setup_minify();
    if(nowMet=="merge") setup_merge();
    if(nowMet=="cdn") setup_cdn();
    if(nowMet=="quality") setup_quality();
    if(nowMet=="cache") setup_cache();

}


function updateRule(){
    var met = "";
    var mytitle = "";
    if(magic.rule_name=="MinifyCss"){
        met = "minify";
        $.each(resourceData, function(index, value){
            if(value.contentTypeValue == 1) {
                value.checked = true;
                itemCheckProcess(value);
            }
        });
    }

    if(magic.rule_name=="MinifyJavaScript"){
        met = "minify";
        $.each(resourceData, function(index, value){
            if(value.contentTypeValue == 2) {
                value.checked = true;
                itemCheckProcess(value);
            }
        });
    }

    if(magic.rule_name=="OptimizeImages"){
        met = "quality";
        $.each(resourceData, function(index, value){
            if(value.contentTypeValue == 3) {
                value.checked = true;
                itemCheckProcess(value);
            }
        });
    }

    if(magic.rule_name=="SpriteImages"){
        $.each(resourceData, function(index, value){
            if(value.spritable==1) {
                value.checked = true;
                itemCheckProcess(value);
            }
        });
    }

    if(magic.rule_name=="SpriteImages") cssSpriteLoad();
    else finalCheck(met);


    $("#applyLoading" ).modal("hide");
    //$("#applyModal" ).modal("show");
}





// 프록시파일캐시
function US_isproxy(updateData){
    $.each(resourceData, function(index, editData){
        $.each(updateData, function(index, value){
            if(editData.objectId == value.objectId){
                editData.proxy = true;
            }
        });
    });
}

// 캐시 적용여부
function US_iscache(updateData){
    $.each(resourceData, function(index, editData){
        $.each(updateData, function(index, value){
            if(editData.objectId == value.objectId){
                editData.cacheControl = value.option.substring(8)/86400;
            }
        });

    });
}

//변경
function US_isauto(updateData){
    $.each(resourceData, function(index, editData){
        $.each(updateData, function(index, value){
            if(editData.originUrlStr == value.option){
                editData.auto = 1;
            }
        });
    });
}

function US_isautoinfo(updateData){
    $.each(resourceData, function(index, editData){
        $.each(updateData, function(index, value){
            if(editData.objectId == value.objectId){
                editData.autoinfo = true;
                editData.fromArray = value.fromArray;
            }
        });
    });
}



function updatePage(jsonPath){

    $("#loadingBox").show();
    $("#itemListView").hide();

    $.getJSON( "/tool/"+req_project_id+"/resource/list" , function(data) {
        $.getJSON( "/sprite/info/"+req_project_id , function(cssdata) {
            $.getJSON( "/tool/"+req_project_id+"/resource/iscdn" , function(cdndata) {


                $.getJSON( "/tool/"+req_project_id+"/resource/isproxy" , function(isproxyData) {
                $.getJSON( "/tool/"+req_project_id+"/resource/iscache" , function(iscacheData) {
                $.getJSON( "/tool/"+req_project_id+"/resource/isauto" , function(isautoData) {
                $.getJSON( "/tool/"+req_project_id+"/resource/isautoinfo" , function(isautoinfoData) {

                    resourceData = data;
                    mergeItem(cssdata);
                    isCdnChecker(cdndata);

                    US_isauto(isautoData);
                    US_isautoinfo(isautoinfoData);
                    US_iscache(iscacheData);
                    US_isproxy(isproxyData);
                    drawEditor();

                    $("#loadingBox").hide();
                    $("#itemListView").show();
                    if(magic.rule_name) setTimeout(function(){ updateRule(); },500);

                });
                });
                });
                });

            });
        });
    });
}

// 헤더캐시컨트롤저장
function setup_cache(){

    var updateValue = $("#cacheControlBox").val() * 86400;
    var editData = [];
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            if(updateValue==0) editData.push({"objectId":value.objectId});
                else editData.push({"objectId":value.objectId,"option":"max-age="+updateValue});
            value.cacheControl = $("#cacheControlBox").val();
        }
    });

    $.post( "/tool/"+req_project_id+"/resource/cache",{"list":JSON.stringify(editData)}).done(function(data) {
        //$("#processBox" ).modal("hide");
        drawEditor();
        refreshItemBgColorAll();
    });
}

// 헤더캐시컨트롤저장
function setup_proxyCache(){

    var updateValue = $("#switch_proxycache").is(":checked") ? 1 : 0;
    var editData = [];
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            if(updateValue==0) editData.push({"objectId":value.objectId});
            else editData.push({"objectId":value.objectId,"option":true});
            value.proxy = updateValue;
        }
    });

    $.post( "/tool/"+req_project_id+"/resource/proxycache",{"list":JSON.stringify(editData)}).done(function(data) {
        //$("#processBox" ).modal("hide");
        drawEditor();
        refreshItemBgColorAll();
    });
}



// 헤더캐시컨트롤저장
function setup_proxy(){

    var updateValue = $("#switch_proxy").is(":checked") ? 1 : 0;
    var editData = [];
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            if(updateValue==0) editData.push({"objectId":value.objectId,"url":value.url,"fromArray":value.fromArray,"remove":true});
            else editData.push({"objectId":value.objectId,"url":value.url,"fromArray":value.fromArray});
            value.auto = updateValue;
        }
    });

    $.post( "/tool/"+req_project_id+"/resource/auto",{"list":JSON.stringify(editData)}).done(function(data) {
        //$("#processBox" ).modal("hide");
        drawEditor();
        refreshItemBgColorAll();
    });
}






function setup_quality(){

    var compresslevel = 9;
    if( $("input[id=qoption1]:checked").val()) compresslevel = 1;
    if( $("input[id=qoption2]:checked").val()) compresslevel = 3;
    if( $("input[id=qoption3]:checked").val()) compresslevel = 5;
    if( $("input[id=qoption4]:checked").val()) compresslevel = 7;
    if( $("input[id=qoption5]:checked").val()) compresslevel = 9;

    loadingModal("quality");
    var editData = [];
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            editData.push({"object_id":value.objectId});
        }
    });
    var items = {"compressLevel":compresslevel, "items":editData};
    $.post( "/tool/"+req_project_id+"/resource/quality",{"list":JSON.stringify(items)}).done(function(data) {
        $("#processBox" ).modal("hide");
        drawEditor();
        refreshItemBgColorAll();
    });

}


// 파일CDN설정
function setup_cdn(){

    loadingModal("cdn");
    var editData = {"items":[]};
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            value.cdn = 1;
            editData.items.push({"objectId":value.objectId});
        }
    });

    drawEditor();
    refreshItemBgColorAll();

    $.post( "/tool/"+req_project_id+"/resource/cdnupload",{"list":JSON.stringify(editData)}).done(function(data) {
        $("#processBox" ).modal("hide");
    });


}

// Merge
function setup_merge(){

    loadingModal("merge");
    var editData = [];
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            editData.push({"objectId":value.objectId});
        }
    });

    $.post( "/tool/"+req_project_id+"/resource/merge",{"list":JSON.stringify(editData)}).done(function(data) {
        $("#processBox" ).modal("hide");
        drawEditor();
        refreshItemBgColorAll();
    });

}

// Minify
function setup_minify(){

    loadingModal("minify");
    var editData = [];
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            editData.push({"objectId":value.objectId});
        }
    });

    $.post( "/tool/"+req_project_id+"/resource/minify",{"list":JSON.stringify(editData)}).done(function(data) {
        $("#processBox" ).modal("hide");
        drawEditor();
        refreshItemBgColorAll();
    });

}













function clearMyList(){

    $('#allCheckbox').prop('checked', false);

    $.each(resourceData, function(index, value){
        value.checked = $('#allCheckbox').is(":checked");
        groupChecked[value.dir] = $('#allCheckbox').is(":checked");

    });

    refreshItemBgColorAll();
    optimizeEnable();

}


function allCheck(){

    $.each(resourceData, function(index, value){
        if(value.visible==1){
            value.checked = $('#allCheckbox').is(":checked");
            groupChecked[value.dir] = $('#allCheckbox').is(":checked");
        }
    });
    refreshItemBgColorAll();
    optimizeEnable();
}

function searchStart(){
    var searchText = $("#searchBox").val();

    filterSelect(filterIndex);
    $.each(resourceData, function(index, value){
        if(value.visible==1)
        {
            if( (value.url).indexOf(searchText) == -1) value.visible = -1;
            else value.visible = 1;
        }
    });

    drawEditor();
    refreshItemBgColorAll();

}

function filterSelect(no){

    filterIndex = no;
    for(var i=0; i<4; i++) $("#filterButton"+i).removeClass("active");
    $("#filterButton"+no).addClass("active");

    $.each(resourceData, function(index, value){

        if(no==0) value.visible = 1;
        else { value.visible = -1; }
        if(no==1 && value.contentTypeValue == 3) value.visible = 1;
        if(no==2 && (value.contentTypeValue == 1 || value.contentTypeValue == 2)) value.visible = 1;
        if(no==3 && (value.originUrlStr.indexOf('http://static.feedle.kr/') == 0 || value.originUrlStr.indexOf('http://www.feedle.kr/') == 0)) value.visible = 1;

    });

    drawEditor();
    refreshItemBgColorAll();
}

///////////////////////////////////////////////////////
// IMAGE DATA LOAD
///////////////////////////////////////////////////////



//resourceData = [{"objectId":951,"contentType":2,"width":8,"height":13,"url":"https://www.hyundaicard.com/img/com/arrow_tooltip2.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5951,"contentType":3,"width":8,"height":13,"url":"https://www.hyundaicard.com/img/com/arrow_tooltip2.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5960,"contentType":3,"width":41,"height":34,"url":"https://www.hyundaicard.com/img/com/btn_sitemap.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5783,"contentType":3,"width":78,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_mc_5.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5765,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_cl_0202.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5756,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/lifestyle_library.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5777,"contentType":3,"width":55,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_mc_1.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5732,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/con_financial03.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5759,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/event_20140529.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5786,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_ecivil_1115.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5723,"contentType":3,"width":98,"height":12,"url":"https://img.hyundaicard.com/img/main/140328_txt_cardapp.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5771,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_cl_0218.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5726,"contentType":3,"width":200,"height":309,"url":"https://img.hyundaicard.com/img/main/140328_appcard_zero.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5968,"contentType":3,"width":4,"height":1,"url":"https://www.hyundaicard.com/img/com/line_dot_b9b9bb.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5753,"contentType":3,"width":230,"height":230,"url":"https://img.hyundaicard.com/img/main/t_library.jpg","top":0,"bottom":0,"left":0,"right":0},{"objectId":5953,"contentType":3,"width":200,"height":304,"url":"https://www.hyundaicard.com/img/main/visual_card.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5729,"contentType":3,"width":224,"height":224,"url":"https://img.hyundaicard.com/img/main/140328_offerbox_v1.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5962,"contentType":3,"width":50,"height":11,"url":"https://www.hyundaicard.com/img/com/family_com02.gif","top":0,"bottom":-1,"left":-1,"right":-1},{"objectId":5956,"contentType":3,"width":49,"height":11,"url":"https://www.hyundaicard.com/img/com/family_com01.gif","top":0,"bottom":-1,"left":-1,"right":-1},{"objectId":5779,"contentType":3,"width":68,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_mc_2.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5788,"contentType":3,"width":41,"height":40,"url":"https://img.hyundaicard.com/img/com/logo_kwacc.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5950,"contentType":3,"width":3,"height":5,"url":"https://www.hyundaicard.com/img/main/bul_arrow_2.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5782,"contentType":3,"width":68,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_mc_4.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5773,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_cl_0220.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5755,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/lifestyle_tile01.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5728,"contentType":3,"width":63,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_card4.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5722,"contentType":3,"width":230,"height":50,"url":"https://img.hyundaicard.com/img/main/140328_cardreceive.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5973,"contentType":3,"width":58,"height":12,"url":"https://www.hyundaicard.com/img/com/family_com03.gif","top":0,"bottom":-1,"left":-1,"right":-1},{"objectId":5767,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_cl_0219.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5785,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_mpoint_0910.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5758,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/event_20140507_3.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5958,"contentType":3,"width":3,"height":5,"url":"https://www.hyundaicard.com/img/com/arrow2_3x5_ffffff.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5734,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/con_point.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5725,"contentType":3,"width":202,"height":303,"url":"https://img.hyundaicard.com/img/main/140328_appcard_x.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5761,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/event_20140507_1.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5952,"contentType":3,"width":1,"height":30,"url":"https://www.hyundaicard.com/img/com/bar_1x30_f0f0f0.gif","top":-1,"bottom":-1,"left":-1,"right":-1},{"objectId":5970,"contentType":3,"width":5,"height":3,"url":"https://www.hyundaicard.com/img/com/arrow1_5x3_bfbfbf.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5955,"contentType":3,"width":350,"height":250,"url":"https://www.hyundaicard.com/img/com/icon_set01.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5964,"contentType":3,"width":22,"height":9,"url":"https://www.hyundaicard.com/img/main/btn_slider_direct_type1.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5787,"contentType":3,"width":85,"height":40,"url":"https://img.hyundaicard.com/img/com/logo_bsi.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5769,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_cl_0217.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5745,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/con_cash.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5754,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/lifestyle_tile05.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5727,"contentType":3,"width":63,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_card2.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5763,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/con_appcard.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5781,"contentType":3,"width":68,"height":11,"url":"https://img.hyundaicard.com/img/main/txt_mc_3.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5784,"contentType":3,"width":20,"height":20,"url":"https://img.hyundaicard.com/img/main/btn_close_mc.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5748,"contentType":3,"width":230,"height":230,"url":"https://img.hyundaicard.com/img/main/t_citybreak_21.jpg","top":0,"bottom":0,"left":0,"right":0},{"objectId":5775,"contentType":3,"width":104,"height":104,"url":"https://img.hyundaicard.com/img/btn/ac_ma_0411.png","top":0,"bottom":0,"left":0,"right":0},{"objectId":5954,"contentType":3,"width":113,"height":28,"url":"https://www.hyundaicard.com/img/com/btn_total_menu_off.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5972,"contentType":3,"width":140,"height":31,"url":"https://www.hyundaicard.com/img/com/logo_header.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5721,"contentType":3,"width":105,"height":20,"url":"https://img.hyundaicard.com/img/main/txt_myaccount.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5757,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/notice_20140513_1.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5730,"contentType":3,"width":230,"height":110,"url":"https://img.hyundaicard.com/img/main/con_financial.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5724,"contentType":3,"width":200,"height":294,"url":"https://img.hyundaicard.com/img/main/140328_appcard_m.gif","top":0,"bottom":0,"left":0,"right":0},{"objectId":5966,"contentType":3,"width":300,"height":180,"url":"https://www.hyundaicard.com/img/com/icon_mark.gif","top":0,"bottom":0,"left":0,"right":0}];

function drawEditor(){

    // Name and URL Split.
    $.each(resourceData, function(index, value){

        value.url = value.originUrlStr;

        value.name = value.url.split('/').pop();
        value.dir = value.url.substring(0, value.url.lastIndexOf("/"))+"/";

        value.thumb = value.url;
        if(value.contentTypeValue == 1) value.thumb = "/assets/images/icon_css.png";
        if(value.contentTypeValue == 2) value.thumb = "/assets/images/icon_js.png";


        if(!value.width) value.width = 0;
        if(!value.height) value.height = 0;

        if(!value.left) value.left = 0;
        if(!value.right) value.right = 0;
        if(!value.top) value.top = 0;
        if(!value.bottom) value.bottom = 0;
        if(!value.lower) value.lower = false;
        if(!value.checked) value.checked = false;

        if(!value.auto) value.auto = 0;
        if(!value.autoinfo) value.autoinfo = 0;
        if(!value.proxy) value.proxy = 0;


        if(!value.cdn) value.cdn = false;
        if(!value.cacheControl) value.cacheControl = 0;

        if(!value.visible) value.visible = 1;
        if(value.contentTypeValue == 0) value.visible = 0;

        if(value.bottom == -1) value.lower = true;
        value.bottom = 0;

    });

    // DIR Sort
    function sortByKey(array, key) {
        return array.sort(function(a, b) {
            var x = a[key]; var y = b[key];
            return ((x < y) ? -1 : ((x > y) ? 1 : 0));
        });
    } resourceData = sortByKey(resourceData, 'url');

    var listDocument = "";
    var beforeDir = "";

    $.each(resourceData, function(index, value){


        if(value.visible==1){

            if(beforeDir != value.dir){
                if(beforeDir!="") {
                    listDocument += "</div>";
                }
                listDocument += '<div class="row"><div class="itemListGroup">';
                listDocument += value.dir;
                listDocument += '</div>';
            }

            value.id = "itemList" + index;
            listDocument += '<div class="text-center thumbnail-div">';


            // 파일상태 표시하는 부분 추가
            listDocument += '<div style="height:25px; oveflow:hidden; width:180px;">';

            listDocument += '<div style="float:left; width:80px; text-align:left;">';

                if(value.spritable==1){
                    listDocument += '<div class="label label-success"><span class="glyphicon glyphicon-th-large" title="Available Image Split"></span></div>&nbsp;';
                }

                // 오토프록시
                if(value.autoinfo !=0) {
                    if(value.auto !=0) listDocument += '<div class="label label-primary"><span class="glyphicon glyphicon-refresh"></span></div> ';
                    else listDocument += '<div class="label label-default"><span class="glyphicon glyphicon-refresh"></span></div>';
                }

            listDocument += '</div>';


            listDocument += '<div style="float:right">';

            //HEADER
            if(value.cacheControl>0) {
                listDocument += '<div class="label label-info"><span class="glyphicon glyphicon-floppy-disk"></span> '+value.cacheControl+'day</div>' ;
            }

            // 프록시캐시
            if(value.proxy!=0) {
                listDocument += '&nbsp;<div class="label label-warning"><span class="glyphicon glyphicon-flash"></span></div>';
            }

            //CDN
            if(value.cdn!=0) {
                listDocument += '&nbsp;<div class="label label-warning"><span class="glyphicon glyphicon-globe"></span></div>' ;
            }


            listDocument += '</div>';
            listDocument += '</div>';
            // 파일 상태추가 부분 끝

            listDocument += '<a class="thumbnail" id="' + value.id + '">';
            listDocument += '<img src="'+value.thumb+'" style="max-width:180px; max-height:180px;">';

            listDocument += '</a>';
            listDocument += '<b>' +value.name + '</b><br><br>'; //+ value.width + 'x' + value.height;

            listDocument += '</div>';

            beforeDir = value.dir;

        }

    });

    if(beforeDir!="") listDocument += "<div>";
    $('#itemListView').html(listDocument);

    delegateUpdate();
    optimizeEnable();
} // Draw End



///////////////////////////////////////////////////////
// EVENT
///////////////////////////////////////////////////////


function refreshItemBgColorAll() {
    $.each(resourceData, function(index, value){
        refreshItemBgColor(value);
    });
}

function refreshItemBgColor(value) {
    if(value.checked == true) $('#'+value.id).css("background-color","#b0dbff");
    else $('#'+value.id).css("background-color","white");
}

function itemCheckProcess(value) {

    refreshItemBgColor(value);
    optimizeEnable();
}

function optimizeEnable(){
    var includeImg = 0;
    var includeText = 0;
    var selectedItem = 0;
    var allCheckedMonitor = 1;

    var proxyAuto = 1;
    var mergeEnable = -1;
    var cdnEnable = 1;
    var cacheControlEnable = 1;
    var spritableTemp = 1;
    //$('#tab_itemList' ).hide();
    $('#tab_merge' ).hide();
    $('#tab_sprite' ).hide();
    $('#tab_intel' ).hide();
    $('#tab_cdn' ).hide();

    $('#tab_proxy' ).hide();

        //
    var proxycaches =false;
    var proxys =false;

    $.each(resourceData, function(index, value){

        if(value.checked==true){
            if(mergeEnable == -1) mergeEnable = value.contentTypeValue;
            if(mergeEnable != value.contentTypeValue) mergeEnable = 0;

            if(value.autoinfo==0) proxyAuto = 0;
            if(value.cdn!=0) cdnEnable = 0;
            if(value.cacheControl!=0) cacheControlEnable = 0;
            if(value.spritable!=1) spritableTemp = 0;

            if(value.proxy==1) proxycaches = true;
            if(value.auto==1) proxys = true;

            selectedItem = selectedItem + 1;
            if(value.contentTypeValue == 3) includeImg = 1;
            if((value.contentTypeValue == 1 || value.contentTypeValue == 2)) includeText = 1;
        } else {
            if(value.visible==1) allCheckedMonitor = 0;
        }
    });

    if(selectedItem>0) {
        if (cdnEnable == 1) $('#tab_cdn').show();
        if (proxyAuto == 1) $('#tab_proxy').show();
    }

    if(selectedItem==1) {

        $('#switch_proxycache').prop('checked', proxycaches);
        $('#switch_proxy').prop('checked', proxys);

    }


    $('#allCheckbox').prop('checked', allCheckedMonitor);
    if(selectedItem>=0) {

        $('#itemHelper' ).text("총 "+selectedItem+"개의 항목이 선택됨.");
        $('#itemHelperTable tbody').html(""); //

        $.each(resourceData, function(index, value) {
            if(value.checked == true){

                $('#itemHelperTable tbody').append("<tr><td>"+ value.url +"</td></tr>");
                if(selectedItem==1) {
                    $("#cacheControlBox").val(value.cacheControl);
                }
            }

        });

        //$('#tab_itemList').show();
    }

    if(selectedItem>1 && mergeEnable > 0){
        $("#mergeButton").removeAttr("disabled");
    } else {
        $("#mergeButton").attr("disabled", "disabled");
    }



    if(includeImg==1 && includeText == 0){
        if(selectedItem>1 && spritableTemp == 1) $('#tab_sprite' ).show();
        $('#tab_intel' ).show();
    } else if(includeImg==0 && includeText == 1) {
        $('#tab_merge' ).show();
    }


}

function delegateUpdate(){

    // Thumbnail Click Event
    $(".thumbnail").bind("click", function() {
        var parent_id = this.id;

        $.each(resourceData, function(index, value){
            if(value.id == parent_id){
                value.checked = !value.checked;
                itemCheckProcess(value);

            }
        });
    });

// Group Click
    $(".itemListGroup").bind("click", function() {
        parentDir = $(this).text();

        var CheckResearcher = 1;
        $.each(resourceData, function(index, value){
            if(value.dir == parentDir && value.visible == 1){
                if(value.checked==false) CheckResearcher = 0;
            }
        });

        groupChecked[parentDir] = !CheckResearcher; //!groupChecked[parentDir];

        $.each(resourceData, function(index, value){
            if(value.dir == parentDir && value.visible == 1){
                value.checked = groupChecked[value.dir];
                itemCheckProcess(value);
            }
        });

    });


///////////////////////////////////////////////////////
// Edit Item
///////////////////////////////////////////////////////


    function indexOf(list, id) {
        for (var i = 0; i < list.length; i++) {
            if (list[i].id === id) { return i; }
        }
        return -1;
    }


// Thumnail Right Button Click
    $(".thumbnail").bind("contextmenu", function() {
        var selectedItem = this.id;
        var selectedIndex = indexOf(resourceData,selectedItem);


        $('#editItem_thumbnail').attr("src",resourceData[selectedIndex].thumb);
        $('#editItem_left').val(resourceData[selectedIndex].left);
        $('#editItem_right').val(resourceData[selectedIndex].right);

        $('#editItem_bottom').val(resourceData[selectedIndex].bottom);
        $('#editItem_top').val(resourceData[selectedIndex].top);
        $('#editItem_width').val(resourceData[selectedIndex].width);
        $('#editItem_height').val(resourceData[selectedIndex].height);
        $('#editItem_lower').attr('checked',resourceData[selectedIndex].lower);

        $('#editItem').modal("show");
        return false;
    });

} // Delegate


// Save Button Click
function editForm_Save(){
    resourceData[selectedIndex].left = $('#editItem_left').val();
    resourceData[selectedIndex].right = $('#editItem_right').val();
    resourceData[selectedIndex].bottom = $('#editItem_bottom').val();
    resourceData[selectedIndex].top = $('#editItem_top').val();

    resourceData[selectedIndex].width = $('#editItem_width').val();
    resourceData[selectedIndex].height = $('#editItem_height').val();
    resourceData[selectedIndex].lower = $('#editItem_lower').attr('checked');
    $('#editItem').modal("hide");
}




///////////////////////////////////////////////////////
// Submit
///////////////////////////////////////////////////////
function deepCopy(o) {
    var copy = o,k;
    if (o && typeof o === 'object') {
        copy = Object.prototype.toString.call(o) === '[object Array]' ? [] : {};
        for (k in o) {
            copy[k] = deepCopy(o[k]);
        }
    }
    return copy;
}


function submitButton(){

    var editData = deepCopy(resourceData);
    $.each(editData, function(index, value){
        if(value.lower == true) value.bottom = -1;
        if(value.checked != true) delete editData[index];
    });

    $.post(spriteHost+"api/sprite/result", {imageData:JSON.stringify(editData)}).done(function(data) {
        $("#resultView_Content").text(data);
        $('#resultView').modal("show");
    });


}

var nowselectedIndex = 0;

function cssSpriteDetailByIndex(selectedIndex){
    nowselectedIndex = selectedIndex;
    $('#cssSprite_name').text(resourceData[selectedIndex].url);
    $('#cssSprite_size').text(resourceData[selectedIndex].width+" x "+resourceData[selectedIndex].height);
    $('#cssSprite_thumbnail').attr("src",resourceData[selectedIndex].thumb);

    $('#cssSprite_left').val(resourceData[selectedIndex].left);
    $('#cssSprite_right').val(resourceData[selectedIndex].right);
    $('#cssSprite_bottom').val(resourceData[selectedIndex].bottom);
    $('#cssSprite_top').val(resourceData[selectedIndex].top);
}

function cssSpriteLoad(){

    var firstItem = 0;
    var counter = 0;

    $('#cssSpriteList tbody').html(""); //
    $.each(resourceData, function(index, value) {
        if (value.checked == true) {
            $('#cssSpriteList tbody').append('<tr><td><img src="'+ value.url +'" width="45" class="splitImageListImg"></td> <td class="cssSpriteTd">'+value.url+'</td></tr>');

            if(firstItem==0){
                cssSpriteDetailByIndex(counter);
                firstItem = 1;
            }
        }
        counter += 1;
    });

    $(".cssSpriteTd").bind("click", function() {
        var selectedItem = $(this).text();
        var selectedIndex = -1;
        for (var i = 0; i < resourceData.length; i++) {
            if (resourceData[i].url === selectedItem) { selectedIndex = i; }
        }
        cssSpriteDetailByIndex(selectedIndex);
    });



    $("#modalSprite").modal("show");





}

function realtimeSave(){
    resourceData[nowselectedIndex].left = $('#cssSprite_left').val();
    resourceData[nowselectedIndex].right = $('#cssSprite_right').val();
    resourceData[nowselectedIndex].bottom = $('#cssSprite_bottom').val();
    resourceData[nowselectedIndex].top = $('#cssSprite_top').val();
}


// Save Button Click
function cssSpriteSave(){

    //var editData = deepCopy(resourceData);
    //$.each(editData, function(index, value){
    //    if(value.lower == true) value.bottom = -1;
    //    if(value.checked != true) delete editData[index];
    //});

    var editData = '{';
    var i = 0;

    $.each(resourceData, function(index, value){
        if (value.checked == true) {
            if(i!=0) editData += ',';

            editData += '\"';
            editData += value.objectId;
            editData += '":{';
            editData += '"top":';
            editData += value.top;
            editData += ',"right":';
            editData += value.right;
            editData += ',"left":';
            editData += value.left;
            editData += ',"bottom":';
            editData += value.bottom;
            editData += ',"width":';
            editData += value.width;
            editData += ',"height":';
            editData += value.height;
            editData += '}';

            //var newAddItem = {};
            //newAddItem.objectId = value.objectId;
            //newAddItem.top = value.top;
            //newAddItem.right = value.right;
            //newAddItem.left = value.left;
            //newAddItem.bottom = value.bottom;
            //newAddItem.width = value.width;
            //newAddItem.height = value.height;
            //editData.push(newAddItem);
            i += 1;
        }
    });

    editData += '}';

    $("#modalSprite").modal("hide");
    $.post("../../sprite/create/"+req_project_id, {"list":editData} ).done(function(data) {

    });


}

