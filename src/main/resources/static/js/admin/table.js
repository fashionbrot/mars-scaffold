



function showModal(id) {
    $("#userInfoId").val(id);
    $('#userInfoDelModal').modal('show');

}




var queryByUserId = function (id) {

    loading();
    $.ajax({
        url: "./env/queryById",
        type: "post",
        data: {"id": id},
        dataType: "json",
        success: function (data) {
            loaded();
            $("#envId").val(id);
            $("#editEnvCode").attr("readonly","readonly");
            $("#editEnvName").val(data.envName);
            $("#editEnvCode").val(data.envCode);
            $("#editStatus").val(data.status);
            $("#userInfoEditModal").modal("show");
        }
    });
}

//查询所有用户
$(document).ready(function () {

    loadData();



    /*$("#checkAll").on("click", function () {
        if (this.checked) {
            $(this).attr('checked', 'checked')
            $("input[name='abced']").each(function () {
                this.checked = true;
            });
        } else {
            $(this).removeAttr('checked')
            $("input[name='abced']").each(function () {
                this.checked = false;

            });
        }
    });*/

$("#swaggerStatus").on("click",function (){
    if (this.checked) {
        $("#swaggerStatus").val("1");
    }else{
        $("#swaggerStatus").val("0");
    }
})
    $("#packagePath").on("blur", function(){
       var pp= $("#packagePath").val();
       var dp=$("#daoPackage").val();
       var sp=$("#servicePackage").val();
       var cp=$("#commonPackage").val();
       /*if (!dp){
           $("#daoPackage").val(pp);
       }else{
           if (pp!=dp){
               $("#daoPackage").val(pp+"."+dp);
           }
       }
        if (!sp){
            $("#servicePackage").val(pp);
        }else{
            if (pp!=sp){
                $("#servicePackage").val(pp+"."+sp);
            }
        }
        if (!cp){
            $("#commonPackage").val(pp);
        }else{
            if (pp!=cp ) {
                $("#commonPackage").val(pp + "." + cp);
            }
        }*/
    });

});


function code() {
    var tables ="";
    $("input[name='listId']:checked").each(function(i){
        if (tables==""){
            tables=$(this).attr("data");
        }else{
            tables+=","+$(this).attr("data");
        }
    })

    if (tables==""){
        alert("请选择")
        return false;
    }
    var packagePath = $("#packagePath").val();
    var excludePrefix = $("#excludePrefix").val();
    var author =$("#author").val();
    var version =$("#version").val();
    var email=$("#email").val();
    var swaggerStatus=$("#swaggerStatus").val();
    var controllerPackage=$("#controllerPackage").val();
    var commonPackage=$("#commonPackage").val();
    var servicePackage=$("#servicePackage").val();
    var daoPackage=$("#daoPackage").val();

    window.location.href="./scaffold/code?tables="+tables+"&packagePath="+packagePath+"&excludePrefix="+excludePrefix
    +"&author="+author+"&version="+version+"&email="+email+"&swaggerStatus="+swaggerStatus+"&controllerPackage="+controllerPackage
    +"&commonPackage="+commonPackage+"&servicePackage="+servicePackage+"&daoPackage="+daoPackage;
    /*loading();
    $.ajax({
        url: "./scaffold/code",
        type: "post",
        data: {"tables": tables},
        dataType: "json",
        success: function (data) {
            loaded();

        },error: function (data) {
            if (data.code==-1){
                alert(data.msg);
            }
        }
    });*/
}
/*

function loadData() {
    var tableName=$("#tableName").val();
    $('#dataTables-userInfo').dataTable().fnDestroy();
    var table =  $('#dataTables-userInfo').DataTable({
        ajax:{
            url: "./scaffold/list?v="+new Date().getTime(),
            type: "post",
            dataType: "json",
            data:{tableName:tableName},
        },
        language:dataTable.language(),
        serverSide:true,
        ordering:true,
        stateSave: true,
        searching: false,
        paging: true,
        info: true,
        bAutoWidth: false,
        lengthMenu: [[20, 30, 50, -1], [20, 30, 50, "All"]],
        columnDefs: [
            {
                targets: 0, render: function (data, type, full, meta) {
                    return "<input type='checkbox' name='abced' data='"+full.tableName+"' />";
                }
            },{
                targets: 1, render: function (data, type, full, meta) {
                    return full.tableName;
                }
            },
            {
                targets: 2, render: function (data, type, full, meta) {
                    return full.comments;
                }
            },
            {
                targets: 3, render: function (data, type, full, meta) {
                    return full.createTime;
                }
            }
        ]
    });

}*/


function loadData() {

    var tableId = "#dataTableId";
    $(tableId).dataTable().fnDestroy();
    $(tableId)
        .on('xhr.dt', function( e, settings, json, xhr ){
            if (json.data!=null && json.data.length>0){
                //将后台总数，赋值给 分页工具
                json.recordsTotal = json.itotalDisplayRecords;
                json.recordsFiltered = json.itotalDisplayRecords;
            }else{
                json.recordsTotal=0;
                json.recordsFiltered =0;
            }
        })
        .DataTable({
            ajax:{
                url: "./scaffold/list?v="+new Date().getTime(),
                type: "post",
                dataType: "json",
                data: function(data){
                    data.page = data.start / data.length + 1;
                    data.pageSize = data.length;
                    data.tableName = $("#tableName").val();
                    delete  data.columns;
                    delete  data.search;
                },
                dataType: "json",
                dataSrc : function(result) {
                    /*if (result.code != 0) {
                        alert("获取数据失败:"+result.msg);
                        return false;
                    }*/
                    return  result.data ;
                },
                error : function(XMLHttpRequest, textStatus, errorThrown) {
                    alert("获取列表失败");
                }
            },
            dom: '<fB<t>ip>',
            stripeClasses: ["odd", "even"],
            paginationType: "full_numbers",
            responsive: true,//自适应
            serverSide:true,
            language: dataTable.language(),
            stateSave: true,
            searching: false,
            paging: true,
            info: true,
            bAutoWidth: false,
            order:[],
            orderable: true,
            lengthMenu: [[25, 50, 100, -1], [25, 50, 100, "All"]],
            columns : [
                {
                    className: "td-checkbox",
                    orderable : false,
                    bSortable : false,
                    data : "id",
                    width : '10px',
                    render : function(data, type, row, meta) {
                        var content = '<label class="mt-checkbox mt-checkbox-single mt-checkbox-outline">';
                        content += '	<input type="checkbox" name="listId" class="group-checkable" data="'+row.tableName+'" value="' + data + '" />';
                        content += '	<span></span>';
                        content += '</label>';
                        return content;
                    }
                },
                {
                    data : 'tableName',
                    bSortable : true,
                    width : "25px",
                    className : "text-center",
                    render : dataTableConfig.DATA_TABLES.RENDER.ELLIPSIS
                },
                {
                    data : 'comments',
                    bSortable : true,
                    width : "20px",
                    className : "text-center",
                    render : dataTableConfig.DATA_TABLES.RENDER.ELLIPSIS
                },{
                    data : 'createTime',
                    bSortable : true,
                    width : "20px",
                    render : dataTableConfig.DATA_TABLES.RENDER.ELLIPSIS
                }
            ]
        });
    $(tableId+'_wrapper').on("change", ":checkbox", function() {
        // 列表复选框
        if ($(this).is("[name='topCheckboxName']")) {
            // 全选
            $(":checkbox", '#dataTableId').prop("checked",$(this).prop("checked"));
        }else{
            // 一般复选
            var checkbox = $("tbody :checkbox", '#dataTableId');
            $(":checkbox[name='cb-check-all']", '#dataTableId').prop('checked', checkbox.length == checkbox.filter(':checked').length);
        }
    }).on('preXhr.dt', function(e, settings, data) {
        // ajax 请求之前事件
        data.page = data.start / data.length + 1;
        data.limit = data.length;
        delete data.start;
        delete data.order;
        delete data.search;
        delete data.length;
        delete data.columns;
    });

}