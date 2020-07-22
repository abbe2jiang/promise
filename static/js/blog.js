(function($) {

    // document.getElementById("_blog_comment").contentEditable = "true";
    window.commentMap = {};

    loadComments();

    function loadComments(){
        var success = function (data) {
            if(data.success){
                var comments = data.data;
                var template = HTemplate(function () {/*
                      <li class="comment">
                        <div class="vcard">
                          <img src="${author.portrait.compressUrl}" alt="Image">
                        </div>
                        <div class="comment-body">
                          <h3>${author.firstName} ${author.lastName}</h3>
                          <div class="meta">${date}</div>
                          <div class="replyDiv rounded" style="display: ${replyDiv}">
                            <div class="meta2">${reply.authorName} published ${reply.date}</div>
                            <p>${reply.content}</p>
                          </div>
                          <p>${content}</p>
                          <p><a href="javascript:void(0)" class="reply rounded _class_reply" _comment_id="${id}">Reply</a></p>
                        </div>
                      </li>
                    */
                })
                for(var i=0;i<comments.length;i++){
                    var item = comments[i];
                    if(window.commentMap[item.id] == undefined) {
                        item['replyDiv'] = "true";
                        if(item['reply'] == null){
                            item['reply'] = {authorName:'',content:'',date:''};
                            item['replyDiv'] = "none";
                        }
                        window.commentMap[item.id] = item;
                        var html = template(item);
                        $("#_sign_comment").before(html);
                    }
                }

                $("._class_reply").on("click", function () {
                    var commentId = $(this).attr("_comment_id")
                    replayComment(commentId);
                })
                cancelComment();
            }
        }
        var blogId = $("#_blog_id").val();
        HAjax.jsonGet("/comment/"+blogId,success);
    }


    function replayComment(commentId){
        var template = HTemplate(function () {/*
                <!--<div class="replyDiv rounded" id="_blog_reply_div">-->
                <div class="meta2">${author.firstName} ${author.lastName} published ${date}</div>
                <p>${content}</p>
                <!--</div>-->
            */
        })
        var comment = window.commentMap[commentId];
        var html = template(comment);
        $("#_blog_reply_id").val(comment.id);
        $("#_blog_reply_div").html(html);
        $("#_blog_reply_div").show();
        $("#_blog_comment").focus();

    }

    $("#_post_comment").on("click", function () {
        postComment();
    });
    $("#_post_comment_cancel").on("click", function () {
        cancelComment();
    });

    function cancelComment() {
        $("#_blog_reply_id").val("");
        $("#_blog_reply_div").hide();
        $("#_blog_comment").val("");
    }

    function postComment() {
        var success = function (data) {
            if(data.success){
                window.location.reload();
                // showMessage('success');
                //
                // loadComments();
                // cancelComment();
            } else {
                showMessage('fail');
            }
        }
        var blogId = $("#_blog_id").val();
        var replyId = $("#_blog_reply_id").val();
        var content = $("#_blog_comment").val();

        var comment = {blogId:blogId,replyId:replyId,content:content};
        HAjax.jsonPost("/comment",comment,success);
    }

    function showMessage(msg) {
        $('#_show_message_body').html(msg)
        $('#_show_message').modal('show')
    }

})(jQuery);