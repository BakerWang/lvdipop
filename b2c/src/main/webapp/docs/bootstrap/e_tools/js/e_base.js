/**
 * Created by Andste on 2016/5/11.
 */
$(function(){
    lteIE8 ? initLow() : initHei();
    function initHei(){
        createHeiModal();  //  创建标准modal
    };

    function initLow(){
        createLowModal();  //  创建兼容modal
    };

    /* 标准modal创建
     ============================================================================ */
    function createHeiModal() {
        var messageModalHtml = '<div class="modal fade" id="messageModal" role="dialog" style="z-index: 1060;">'
                + '<div class="modal-dialog" style="width: 265px;">'
                + '<div class="modal-content">'
                + '<div class="modal-header" style="padding: 10px;">'
                + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
                + '<h4 class="modal-title" style="text-align: center; font-size: 14px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;user-select: none;">提示信息</h4>'
                + '</div>'
                + '<div class="modal-body" style="text-align: center; line-height: 20px;"></div>'
                + '</div>'
                + '</div>'
                + '</div>'
            ,

            confirmModalHtml = '<div class="modal fade" id="confirmModal" role="dialog">'
                + '<div class="modal-dialog">'
                + '<div class="modal-content" style="width: 330px; margin: 0 auto;">'
                + '<div class="modal-header">'
                + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
                + '<h4 class="modal-title" style="text-align: center; font-size: 14px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;user-select: none;">提示信息</h4>'
                + '</div>'
                + '<div class="modal-body" style="text-align: center; line-height: 20px;">'
                + '</div>'
                + '<div class="modal-footer" style="padding: 10px;">'
                + '<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>'
                + '<button type="button" class="btn btn-primary true-btn">确定</button>'
                + '</div>'
                + '</div><!-- /.modal-content -->'
                + '</div><!-- /.modal-dialog -->'
                + '</div><!-- /.modal -->'
            ,

            dialogModalHtml = '<div class="modal fade" id="dialogModal" role="dialog">'
                + '<div class="modal-dialog" role="document">'
                + '<div class="modal-content">'
                + '<div class="modal-header">'
                + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
                + '<h4 class="modal-title" style="text-align: center; font-size: 14px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;user-select: none;">标题</h4>'
                + '</div>'
                + '<div class="modal-body"></div>'
                + '<div class="modal-footer">'
                + '<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>'
                + '<button type="button" class="btn btn-primary">确定</button>'
                + '</div>'
                + '</div><!-- /.modal-content -->'
                + '</div><!-- /.modal-dialog -->'
                + '</div><!-- /.modal -->'
            ;
        ;
        if (!$('div').is('#messageModal')) {
            $('body').append(messageModalHtml);
        };

        if (!$('div').is('#confirmModalHtml')) {
            $('body').append(confirmModalHtml);
        };

        if (!$('div').is('#dialogModal')) {
            $('body').append(dialogModalHtml);
        };
    };

    /* 兼容modal创建
     ============================================================================ */
    function createLowModal(){
        var messageModalHtml = '<div class="modal hide fade" id="messageModal"  role="dialog" aria-hidden="true" style="width: 265px; margin-left: -132.5px;">'
                + '<div class="modal-header" style="padding: 10px; height: 20px;">'
                + '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'
                + '<h3 style="text-align: center; font-size: 14px;-ms-user-select: none;user-select: none;">提示信息</h3>'
                + '</div>'
                + '<div class="modal-body" style="text-align: center; line-height: 20px;"></div>'
                + '</div>'
            ,

            confirmModalHtml = '<div id="confirmModal" class="modal hide fade" role="dialog" aria-hidden="true" style="width: 265px; margin-left: -132.5px;">'
                + '<div class="modal-header" style="padding: 10px; height: 20px;">'
                + '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'
                + '<h3 style="text-align: center; font-size: 14px;-ms-user-select: none;user-select: none;">提示信息</h3>'
                + '</div>'
                + '<div class="modal-body" class="modal-body" style="text-align: center; line-height: 20px;">'
                + '</div>'
                + '<div class="modal-footer" style="height: 20px;">'
                + '<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>'
                + '<button class="btn btn-primary true-btn">确定</button>'
                + '</div>'
                + '</div>'
        ;


        if (!$('div').is('#messageModal')) {
            $('body').append(messageModalHtml);
        };

        if (!$('div').is('#confirmModalHtml')) {
            $('body').append(confirmModalHtml);
        };
    };


    /* 分页兼容IE78JS
     ============================================================================ */
    (function(){
        if($('.lteIE8-pagination') && $('.gteIE9-pagination')){
            if(lteIE8){
                $('.lteIE8-pagination').addClass('pagination');
            }else {
                $('.gteIE9-pagination').addClass('pagination');
            }
        }
    }())


});

/* 标准modal调用
 ============================================================================ */
/**
 * 消息提示框调用
 * @param _content
 */
function messageModal(_content){
    var id = $('#messageModal'),
        body = _content || '';
    id.find('.modal-body').text(body);
    id.modal();
    id.on('hidden.bs.modal', function(){id.find('.modal-body').empty()});
};

/**
 * 带确认取消按钮的提示框调用
 * @param _content  //  需要提示的内容
 * @param _fn       //  点击确定后的回调
 */
function confirmModal(_content ,_fn){
    var id = $('#confirmModal'),
        body = _content || '';
    id.find('.modal-body').text(body);
    id.modal();
    id.find('.true-btn').unbind('click').click(function(){
        if(_fn){
            id.modal('toggle');
            _fn();
        };
    });
    id.on('hidden.bs.modal', function(){id.find('.modal-body').empty()});
}

function dialogModal(_content){
    var id = $('#dialogModal'),
        body = _content || '';
    id.find('.modal-body').html(body);
    id.modal();
    id.on('hidden.bs.modal', function(){id.find('.modal-body').empty()});
}

/**
 * 一个可以将任何元素以dialoModal形式弹出的接口
 * 使用方法： $('元素节点').dialogModal();
 */
$.fn.dialogModal = function(options){
    var _this = this,
        html = _this.html(),
        id = $('#dialogModal');
    /*var width = options.width,
        height = options.height,
        title = options.title || '';
    if(width){
        id.find('.modal-dialog').css('width', width);
    };
    if(height){
        id.find('.modal-dialog').css('height', height);
    };
    if(title){
        id.find('.modal-title').html(title);
    };*/
    id.find('.modal-body')
        .empty().html(html);
    id.modal();
    id.on('hidden.bs.modal', function(){
        id.find('.body').empty();
    });
}
/* 兼容modal调用
 ============================================================================ */
/*
function messageModal(_content){
    var id = $('#messageModal'),
        body = _content || '';
    id.find('.modal-body').html(body);
    id.modal();
    id.on('hidden.bs.modal', function(){id.find('.modal-body').empty()});
};*/


/* flat选择框主题加载
 ============================================================================ */
if(!Sys.ie || Sys.ie > 7){
    document.write('<script type="text/javascript" src="'+ctxPath+'/e_tools/js/icheck.min.js"></script>');
    document.write('<link rel="stylesheet" href="'+ctxPath+'/e_tools/flat/green.css">');

    //  初始化icheck
    $(document).ready(function () {
        $('input').iCheck({
            checkboxClass: 'icheckbox_flat-green',
            radioClass: 'iradio_flat-green'
        });
    });
}

/* 定义alert全局变量
 ============================================================================ */
lteIE8 ? (alert = alert) : (alert = messageModal)

