
var dictionary_rule_name = new Array;
dictionary_rule_name["AvoidBadRequests"] = "Avoid bad requests";
dictionary_rule_name["AvoidCssImport"] = "Avoid CSS @import";
dictionary_rule_name["DeferParsingJavaScript"] = "Defer parsing of JavaScript";
dictionary_rule_name["EnableGzipCompression"] = "Enable compression";
dictionary_rule_name["EnableKeepAlive"] = "Enable Keep-Alive";
dictionary_rule_name["InlineSmallCss"] = "Inline Small CSS";
dictionary_rule_name["InlineSmallJavaScript"] = "Inline Small JavaScript";
dictionary_rule_name["LeverageBrowserCaching"] = "Leverage browser caching";
dictionary_rule_name["MakeLandingPageRedirectsCacheable"] = "Make landing page redirects cacheable";
dictionary_rule_name["MinifyCss"] = "Minify CSS";
dictionary_rule_name["MinifyHTML"] = "Minify HTML";
dictionary_rule_name["MinifyJavaScript"] = "Minify JavaScript";
dictionary_rule_name["MinimizeRedirects"] = "Minimize redirects";
dictionary_rule_name["MinimizeRequestSize"] = "Minimize request size";
dictionary_rule_name["OptimizeImages"] = "Optimize images";
dictionary_rule_name["OptimizeTheOrderOfStylesAndScripts"] = "Optimize the order of styles and scripts";
dictionary_rule_name["PutCssInTheDocumentHead"] = "Put CSS in the document head";
dictionary_rule_name["RemoveQueryStringsFromStaticResources"] = "Remove query strings from static resources";
dictionary_rule_name["ServeResourcesFromAConsistentUrl"] = "Serve resources from a consistent URL";
dictionary_rule_name["ServeScaledImages"] = "Serve scaled images";
dictionary_rule_name["SpecifyACacheValidator"] = "Specify a cache validator";
dictionary_rule_name["SpecifyAVaryAcceptEncodingHeader"] = "Specify a Vary: Accept-Encoding header";
dictionary_rule_name["SpecifyCharsetEarly"] = "Specify a character set";
dictionary_rule_name["SpecifyImageDimensions"] = "Specify image dimensions";
dictionary_rule_name["SpriteImages"] = "Combine images into CSS sprites";
dictionary_rule_name["PreferAsyncResources"] = "Prefer asynchronous resources";

//////////////////////////////////////
// Execute Script
//////////////////////////////////////

var dictionary_rule_feedle = new Array;
dictionary_rule_feedle["AvoidBadRequests"] = "2";
dictionary_rule_feedle["AvoidCssImport"] =  "";
dictionary_rule_feedle["DeferParsingJavaScript"] =  "";
dictionary_rule_feedle["EnableGzipCompression"] =  "2";
dictionary_rule_feedle["EnableKeepAlive"] =  "";
dictionary_rule_feedle["InlineSmallCss"] =  "";
dictionary_rule_feedle["InlineSmallJavaScript"] =  "";
dictionary_rule_feedle["LeverageBrowserCaching"] =  "2";
dictionary_rule_feedle["MakeLandingPageRedirectsCacheable"] =  "";
dictionary_rule_feedle["MinifyCss"] = "1";
dictionary_rule_feedle["MinifyHTML"] = "";
dictionary_rule_feedle["MinifyJavaScript"] = "1";
dictionary_rule_feedle["MinimizeRedirects"] = "";
dictionary_rule_feedle["MinimizeRequestSize"] = "";
dictionary_rule_feedle["OptimizeImages"] = "1";
dictionary_rule_feedle["OptimizeTheOrderOfStylesAndScripts"] = "";
dictionary_rule_feedle["PutCssInTheDocumentHead"] =  "";
dictionary_rule_feedle["RemoveQueryStringsFromStaticResources"] =  "";
dictionary_rule_feedle["ServeResourcesFromAConsistentUrl"] =  "";
dictionary_rule_feedle["ServeScaledImages"] =  "";
dictionary_rule_feedle["SpecifyACacheValidator"] =  "2";
dictionary_rule_feedle["SpecifyAVaryAcceptEncodingHeader"] =  "";
dictionary_rule_feedle["SpecifyCharsetEarly"] = "";
dictionary_rule_feedle["SpecifyImageDimensions"] =  "";
dictionary_rule_feedle["SpriteImages"] = "1";
dictionary_rule_feedle["PreferAsyncResources"] = "";


var dictionary_sortkey_feedle = new Array;
dictionary_sortkey_feedle["AvoidBadRequests"] = "";
dictionary_sortkey_feedle["AvoidCssImport"] =  "";
dictionary_sortkey_feedle["DeferParsingJavaScript"] =  "";
dictionary_sortkey_feedle["EnableGzipCompression"] =  "1";
dictionary_sortkey_feedle["EnableKeepAlive"] =  "";
dictionary_sortkey_feedle["InlineSmallCss"] =  "";
dictionary_sortkey_feedle["InlineSmallJavaScript"] =  "";
dictionary_sortkey_feedle["LeverageBrowserCaching"] =  "1";
dictionary_sortkey_feedle["MakeLandingPageRedirectsCacheable"] =  "";
dictionary_sortkey_feedle["MinifyCss"] = "1";
dictionary_sortkey_feedle["MinifyHTML"] = "";
dictionary_sortkey_feedle["MinifyJavaScript"] = "1";
dictionary_sortkey_feedle["MinimizeRedirects"] = "";
dictionary_sortkey_feedle["MinimizeRequestSize"] = "";
dictionary_sortkey_feedle["OptimizeImages"] =  "1";
dictionary_sortkey_feedle["OptimizeTheOrderOfStylesAndScripts"] = "";
dictionary_sortkey_feedle["PutCssInTheDocumentHead"] =  "";
dictionary_sortkey_feedle["RemoveQueryStringsFromStaticResources"] =  "";
dictionary_sortkey_feedle["ServeResourcesFromAConsistentUrl"] =  "";
dictionary_sortkey_feedle["ServeScaledImages"] =  "";
dictionary_sortkey_feedle["SpecifyACacheValidator"] =  "1";
dictionary_sortkey_feedle["SpecifyAVaryAcceptEncodingHeader"] =  "";
dictionary_sortkey_feedle["SpecifyCharsetEarly"] = "";
dictionary_sortkey_feedle["SpecifyImageDimensions"] =  "";
dictionary_sortkey_feedle["SpriteImages"] = "1";
dictionary_sortkey_feedle["PreferAsyncResources"] = "";

var dictionary_rule_description = new Array;
dictionary_rule_description["AvoidBadRequests"] = "404 등 잘못되거나 깨진 링크, 요청을 제거하므로써 브라우저와 서버간의 무의미한 요청과 기다림을 없애 줄 수 있습니다. ";
dictionary_rule_description["AvoidCssImport"] =  "@import 보다는 <link>를 사용하여 CSS를 불러온다면 병렬적으로 로딩되며, 기다리는 낭비시간이 줄어들게 됩니다.";
dictionary_rule_description["DeferParsingJavaScript"] =  "만약 스크립트가 지연될 수 있다면, 페이지 하단으로 옮겨 웹페이지의 로드속도를 빠르게 만들어줄 수 있습니다.";
dictionary_rule_description["EnableGzipCompression"] =  "gzip 또는 deflate로 리소스를 압축하면 네트워크를 통해 전송되는 용량을 줄일 수 있습니다.";
dictionary_rule_description["EnableKeepAlive"] =  "브라우저와 서버와의 연결을 지속시켜 놓으므로써 추가적인 파일 요청에 즉각 대응받으며 속도가 향상 될 수 있습니다.";
dictionary_rule_description["InlineSmallCss"] =  "간단한 CSS를 별도 외부파일로 위치시키지 않고 HTML파일에 포함시킴으로써 별도의 다운로드 과정을 줄여 속도를 향상 시킬 수 있습니다.";
dictionary_rule_description["InlineSmallJavaScript"] =  "간단한 Javascript를 별도 외부파일로 위치시키지 않고 HTML파일에 포함시킴으로써 별도의 다운로드 과정을 줄여 속도를 향상 시킬 수 있습니다.";
dictionary_rule_description["LeverageBrowserCaching"] =  "브라우저에 캐싱을 통해 한번 전송받은 파일을 재활용 하도록 함으로써, 방문했던 페이지를 보다 빠르게 로딩 할 수 있습니다.";
dictionary_rule_description["MakeLandingPageRedirectsCacheable"] =  "모바일에서 접속시 m.domain.com 으로 리다이렉트 되는 페이지 등을 캐시가능하도록 제공하여, 브라우저의 페이지 로딩시간을 감축시킬 수 있습니다.";
dictionary_rule_description["MinifyCss"] = "CSS 코드를 압축하면 데이터 용량을 크게 줄여 다운로드 및 파싱 속도를 높일 수 있습니다.";
dictionary_rule_description["MinifyHTML"] = "HTML 코드(본문 자바스크립트 및 그 안에 포함된 CSS 포함)를 압축하면 데이터 용량을 크게 줄여 다운로드 및 파싱 속도를 높일 수 있습니다.";
dictionary_rule_description["MinifyJavaScript"] = "자바스크립트 코드를 압축하면 데이터 용량을 크게 줄여 다운로드 및 파싱 속도를 높일 수 있습니다.";
dictionary_rule_description["MinimizeRedirects"] = "리다이렉트 되는 페이지를 서버 사이드에서 캐시하여 이용자에게 최종 리소스를 바로 제공해 주므로서 리다이렉트로 인한 지연을 감소시킬 수 있습니다.";
dictionary_rule_description["MinimizeRequestSize"] = "쿠키 및 리퀘스트 헤더의 사이즈를 가능한한 작게 유지하여 하나의 패킷으로 리퀘스트 헤더를 보내므로써 부하를 줄일 수 있습니다.";
dictionary_rule_description["OptimizeImages"] =  "이미지의 형식을 적절하게 지정하고 압축하면 데이터 용량을 크게 줄일 수 있습니다.";
dictionary_rule_description["OptimizeTheOrderOfStylesAndScripts"] = "style 태그를 script 태그보다 상단에 위치시킴으로써 하단에 위치시킬 때 보다 페이지 로드 속도를 향상 시킬 수 있습니다.";
dictionary_rule_description["PutCssInTheDocumentHead"] =  "CSS를 문서 상단에 위치시킴으로써 하단에 위치시킬 때 보다 외적인 페이지 로드 속도를 향상 시켜 이용자의 체감 속도를 빠르게 할 수 있습니다.";
dictionary_rule_description["RemoveQueryStringsFromStaticResources"] =  "정적 파일에 쿼리를 사용하지 마세요. 쿼리를 사용하면 캐시컨트롤이 무효화 될 수 있습니다.";
dictionary_rule_description["ServeResourcesFromAConsistentUrl"] =  "리소스를 유니크한 URL로 제공해 주므로써, 데이터 전송시 중복을 제거할 수 있습니다.";
dictionary_rule_description["ServeScaledImages"] =  "이미지가 표현될 크기 만큼으로만 리사이징 된 이미지를 제공해 주므로서 큰 이미지를 모두 전송해야 하던 낭비를 줄여 전송 및 로딩속도를 높일 수 있습니다.";
dictionary_rule_description["SpecifyACacheValidator"] =  "브라우저 캐시를 위한 Last-Modified, ETag header 해더 지정해 준다면, 브라우저에 캐싱을 통해 한번 전송받은 파일을 재활용 하도록 하므로써, 방문했던 페이지를 보다 빠르게 로딩 할 수 있습니다.";
dictionary_rule_description["SpecifyAVaryAcceptEncodingHeader"] =  "유저가 지원하지 않는 압축 방식을 명시하는 오류를 범하지 않기 위해, Accept-Encoding header 에 압축 전과 후의 모든 버전을 명시해줍니다.";
dictionary_rule_description["SpecifyCharsetEarly"] = "브라우저가 빨리 처리 할 수 있는 캐릭터 셋으로 서버에서 명시하여 보내줌으로써 브라우저 처리 속도를 향상 시킬 수 있습니다.";
dictionary_rule_description["SpecifyImageDimensions"] =  "이미지의 너비와 높이를 명시해 줌으로써, 브라우저가 이미지를 다운로드 후 측정해야하던 작업을 줄여 줄 수 있습니다.";
dictionary_rule_description["SpriteImages"] = "웹 페이지의 여러 이미지를 각각 다운받으며 생기는 브라우저의 부하를 합쳐진 하나의 이미지만 다운받으므로서 페이지 로딩 속도를 높일 수 있습니다.";
dictionary_rule_description["PreferAsyncResources"] = "스크립트가 비동기적으로 로드된다면, HTML 렌더링을에 지체되지 않고 바로 이용되므로써 웹페이지 부하를 줄일 수 있습니다.";

var mylang = "ko";
var mydetail = "";

function langChange(){
    mylang = $("#langControl").val();
    detailPrint();
}

function detailPrint(){
    $("#desView_title").text(dictionary_rule_name[mydetail]);
    $("#desView_body").html("최적화 도움말을 불러오고 있습니다.");
    $("#desView").modal("show");
    $.get( "/assets/text/"+mylang+"/"+mydetail+".html" , function(data) {
        $("#desView_body").html(data);
    });
}

function moreClick(value){
    mydetail = value;
    detailPrint();
}

function scoreForColor(value){
    if(value<60) return "danger";
    else if(value<80) return "warning";
    else return "success";
}

function scoreForVisible(value){
    if(value==100) return "perfactHide";
    else return "";
}

function opdatabaseDraw(){

    var drawHtml = "";
    $.each(opdatabase, function(index, value) {

        drawHtml+='<div class="panel panel-default"><div class="panel-heading">';
        drawHtml+='<h3 class="panel-title oplist_title">'+ value.optimizeName +'</h3><br>';
        drawHtml+='<span class="oplist_time">'+value.optimizeDate+'</span>';
        drawHtml+='<button type="button" class="btn btn-default btn-xs oplist_close">X 취소</button></div><div class="panel-body">';
        drawHtml+='<div class="oplist_more"><span class="caret"></span> 최적화 항목</div>';
        drawHtml+='<table class="table table-striped oplist_detailTable">';
        $.each(value.optimizedList, function(index, values) {
            drawHtml+='<tr><td>'+values+'</td></tr>';
        });

        drawHtml+='</table></div></div>';

    });
    $("#sub_menu").html(drawHtml);

}

var opdatabase = [];
function dataLoader(loadKey){

    $.get( "/tool/"+req_project_id+"/optimize/list", function( data ) {
        //data = JSON.parse(data);
        opdatabase = data;
        opdatabaseDraw();
    });

    $.get( "/pagespeed/"+loadKey, function( data ) {
        data = JSON.parse(data)
        pdatabase = data.rule_results;
        pdatabaseDraw();
        $("#loadingView").hide();
    });
}


var pdatabase = [];

function pdatabaseDraw(){

    // initialLize
    $.each(pdatabase, function(index, value) {
        value.sortKey = dictionary_sortkey_feedle[value.rule_name];
    });

    function sortByKey(array, key) {
        return array.sort(function(a, b) {
            var x = a[key]; var y = b[key];
            return ((x > y) ? -1 : ((x < y) ? 1 : 0));
        });
    } pdatabase = sortByKey(pdatabase, 'sortKey');

    $.each(pdatabase, function(index, value) {
        pdatabaseProcess(value);
    });

    $("#main_contents").append("&nbsp;<br><p></p><p></p>");
    $(".perfactHide").hide();
    delegateUpdate();
}

function argsReplace(text, argsArray){
    if(argsArray) $.each(argsArray, function(index, value) {
        text = text.replace("$"+(index+1), value.localized_value);
    });
    return text;
}

function psDraw_header(value){
    var wContents = "";
    //wContents += value.format;

    wContents += argsReplace(value.format, value.args);
    return wContents;
}

function psDraw_urls(value){
    var wContents = "";

    foldTableIndex += 1;
    wContents += '<div class="detailCaption"><a onclick="tableToggle('+foldTableIndex+');"><span class="glyphicon glyphicon-search"></span> 항목 자세히 보기</a></div>';
    wContents += '<table class="table pagespeedTable">';
    wContents += '<tbody id="pagetable_'+foldTableIndex+'" style="display:none;">';
    $.each(value, function(index, newValue) {
        wContents += '<tr><td>'+ argsReplace(newValue.result.format, newValue.result.args) +'</td></tr>';
    });
    wContents += '</tbody></table>';

    return wContents;
}

var foldTableIndex = 0;
function pdatabaseMoreDetail(value){
    var wContents = "";

    if(value.url_blocks){
        wContents += "<br><br>";

        $.each(value.url_blocks, function(index, urlValue) {
            if(urlValue.header) wContents += "<pre>" + psDraw_header(urlValue.header) + "</pre>";
            if(urlValue.urls) wContents += psDraw_urls(urlValue.urls);
        });
    }

    return wContents;
}

function pdatabaseProcess(value){
    var wContents = "";

    wContents += '<div class="panel panel-'+scoreForColor(value.rule_score)+' '+scoreForVisible(value.rule_score)+'">';
    wContents += '<div class="panel-heading"><h3 class="panel-title">';
    wContents += dictionary_rule_name[value.rule_name];
    wContents += '</h3><div class="panel-heading-button">'

    if(value.rule_score!=100 && dictionary_rule_feedle[value.rule_name] != "")
        wContents += '<button type="button" class="btn btn-'+scoreForColor(value.rule_score)+' btn-xs" onclick="easy(\''+value.rule_name+'\');">수정하기</button> ';

    wContents += '<button type="button" class="btn btn-default btn-xs" onclick="moreClick(\''+value.rule_name+'\')">자세히 알아보기</button></div>';
    wContents += '</div><div class="panel-body">';
    wContents += dictionary_rule_description[value.rule_name];
    wContents += pdatabaseMoreDetail(value);
    wContents += '</div></div>';

    $("#main_contents").append(wContents);
}


function VisibleChangeEvent(){

    var ruleswitch = ($("#ruleswitch").is(":checked") ? 1 : 0);
    if(ruleswitch==1) $(".perfactHide").hide();
    else $(".perfactHide").show();

}

function tableToggle(foldTableIndex){
    $('#pagetable_'+foldTableIndex).toggle();
}

function delegateUpdate(){



}

var seletedAuto = "";
var nowJsonRule = "";
function easy(nowSel){
    seletedAuto = nowSel;
    $.each(pdatabase, function(index, value) {
        if(value.rule_name==nowSel) nowJsonRule = value;
    });

    if(dictionary_rule_feedle[seletedAuto]=="1")
    {
        applySetupAction();
        return;
    }

    $("#autoSetup" ).modal("show");
    $("#autoSetupText").text(dictionary_rule_name[nowJsonRule.rule_name]+"를 정말 적용하시겠습니까?");
}

function applySetupAction(){
   if(dictionary_rule_feedle[seletedAuto]=="1") $("#applySetting").attr("action", "./resource");
   if(dictionary_rule_feedle[seletedAuto]=="2") $("#applySetting").attr("action", "./setting");
   $("#applyRule").val(JSON.stringify(nowJsonRule));
   $("#applySetting").submit();
}
