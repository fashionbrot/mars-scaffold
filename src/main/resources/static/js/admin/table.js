



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

       /*$("#checkAll").on("change", function () {
            if (this.checked) {
                $("#dataTables-userInfo .odd").find("input").attr("checked","checked");
                $("#dataTables-userInfo .even").find("input").attr("checked","checked");
            } else {
                $("#dataTables-userInfo .odd").find("input").attr("checked",false);
                $("#dataTables-userInfo .even").find("input").attr("checked",false);
            }
        });*/

    $("#checkAll").on("click", function () {
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
    });

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
    $("input[name='abced']:checked").each(function(i){
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

}