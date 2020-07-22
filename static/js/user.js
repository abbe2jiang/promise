(function ($) {
    myLatest(1);
    if ($("#_user_post").length > 0) {
        document.execCommand('defaultParagraphSeparator', false, 'p');

        document.getElementById("_user_first_name").contentEditable = "true";
        document.getElementById("_user_last_name").contentEditable = "true";
        document.getElementById("_user_brief").contentEditable = "true";

        initUser();

        $("#_user_portrait").on("click", function () {
            $("#_file_user_portrait").click();
            $("#_file_user_portrait").change(function () {
                var file = this.files[0];
                if(file.size>window.IMAGE_MAX_SIZE){
                    showMessage("image is too large.")
                    return;
                }
                var url = window.URL.createObjectURL(file);
                $("#_user_portrait").attr("src", url);
                uploadPortrait({id: '_user_portrait', file: file});
            });
        });

        $("#_user_post").on("click", function () {
            userPost();
        });


        window.fileNo = 1;
        window.uploadImageNo = 0;
        window.uploadedImageNo = 0;

        document.getElementById("_bio_title").contentEditable = "true";
        window.contentDiv = document.getElementById("_bio_content");
        window.contentDiv.contentEditable = "true";


        $("#_insert_image").on("click", function () {

            $('#_file_' + window.fileNo).click();
            $('#_file_' + window.fileNo).change(function () {
                var items = showImage(this, window.contentDiv);
                uploadImages(items);
            });

            window.fileNo = window.fileNo + 1;
            var template = HTemplate(function () {/*
              <input type="file" id="_file_${fileNo}"  multiple >
            */
            })
            var html = template({fileNo: window.fileNo});
            $('#_files').append(html);
        });

        $("#_bio_post").on("click", function () {
            bioPost();
        });

        $("#_category_add").click(function () {
            $("#_modal_category_add").modal('show');
            $("#_category_save_add").on("click", function () {
                addCategory();
            });
        });

        $("#_category_delete").click(function () {
            $("#_modal_category_delete").modal('show');
            $("#_category_save_delete").on("click", function () {
                deleteCategory();
            });
        });

        $("#_category_move").click(function () {
            $("#_modal_category_move").modal('show');
            $("#_category_save_move").on("click", function () {
                moveCategory();
            });
        });

    }

    function myLatest(page) {
        var template = HTemplate(function () {/*
                    <div class="post-entry-horzontal">
                        <a href="/blog/${id}">
                          <div class="image" style="background-image: url(${profile.compressUrl});"></div>
                          <span class="text">
                              <div class="post-meta">
                                <span class="author mr-2"><img src="${author.portrait.compressUrl}" alt="image"> ${author.firstName}</span>&bullet;
                                <span class="mr-2">${date} </span> &bullet;
                                <span class="ml-2"><span class="fa fa-comments"></span> ${comments}</span>
                              </div>
                              <h2>${title}</h2>
                            </span>
                        </a>
                      </div>
                    */
        })
        var success = function (data) {
            if(data.success){
                var pagination = data.data;
                var pages = pagination.data;
                var html = "";
                for (var i = 0; i < pages.length; i++) {
                    html += template(pages[i]);
                }
                $("#divSign-my-latest").html(html);
                HPage.init("_blog_page", myLatest, pagination);
            }
        }
        var userId = $("#_user_id").val();
        HAjax.jsonGet("/blogs/user/"+userId+"/"+page,success);
    }

    function moveCategory() {
        var success = function (data) {
            if (data.success) {
                window.location.reload();
            } else {
                $("#_modal_category_delete").modal('hide');
                showMessage(data.error);
            }
        }
        var sourceId = $("#_category_move_source_id").val();
        var targetId = $("#_category_move_target_id").val();
        if (sourceId == "" || targetId == "") return;
        var data = {sourceId: sourceId, targetId: targetId};
        HAjax.put("/category", data, success);

    }

    function deleteCategory() {
        var success = function (data) {
            if (data.success) {
                window.location.reload();
            } else {
                $("#_modal_category_delete").modal('hide');
                showMessage(data.error);
            }
        }
        var id = $("#_category_delete_id").val();
        if (id == "") return;
        var data = {id: id,};
        HAjax.delete("/category", data, success);

    }

    function addCategory() {
        var success = function (data) {
            if (data.success) {
                window.location.reload();
            } else {
                $("#_modal_category_add").modal('hide');
                showMessage(data.error);
            }
        }
        var name = $("#_category_name").val();
        if (name == "") return;
        var data = {name: name,};
        HAjax.post("/category", data, success);

    }

    function initUser() {
        if ($("#_user_first_name").html().length < 3) {
            $("#_user_first_name").html('FName')
        }
        if ($("#_user_last_name").html().length < 3) {
            $("#_user_last_name").html('LName')
        }
        if ($("#_user_brief").html().length < 3) {
            $("#_user_brief").html('brief')
        }
    }


    function userPost() {
        var success = function (data) {
            if (data.success) {
                showMessage('success')
            } else {
                showMessage('fail');
            }
        }
        var portrait = $("#_user_portrait").attr("src");
        if (portrait.indexOf("blob:") != -1) {
            showMessage('Portrait is uploading.');
            return;
        }
        var firstName = $("#_user_first_name").html();
        var lastName = $("#_user_last_name").html();
        var brief = $("#_user_brief").html();
        var data = {portrait: portrait, firstName: firstName, lastName: lastName, brief: brief};
        HAjax.jsonPost("/author", data, success);
    }

    function bioPost() {
        if (window.uploadImageNo != window.uploadedImageNo) {
            var no = window.uploadImageNo - window.uploadedImageNo;
            showMessage(no + "images are uploading")
            return
        }

        var success = function (data) {
            if (data.success) {
                showMessage('success');
            } else {
                showMessage('fail');
            }
        }

        var title = $("#_bio_title").html();
        var content = $("#_bio_content").html();
        var data = {title: title, content: content};
        HAjax.jsonPost("/author/bio", data, success);
    }

    function uploadPortrait(item) {
        var formData = new FormData();
        formData.append('upload', item.file);
        $.ajax({
            url: "/image/upload",
            data: formData,
            type: "POST",
            dataType: "json",
            cache: false,//上传文件无需缓存
            processData: false,//用于对data参数进行序列化处理 这里必须false
            contentType: false, //必须
            mimeType: "multipart/form-data",
            // async: false,
            success: function (obj) {
                if (obj.success) {
                    $("#" + item.id).attr("src", obj.url);
                } else {
                    alert("upload error")
                }
            },
        });
    }

    function showImage(obj, dev, fileNo) {
        var items = [];
        for (var i = 0; i < obj.files.length; i++) {
            var file = obj.files[i];
            if(file.size>window.IMAGE_MAX_SIZE){
                showMessage("image is too large.")
                return items;
            }
            var url = window.URL.createObjectURL(file);
            var id = url.substring(url.lastIndexOf("/") + 1);
            var item = {id: id, url: url, file: file, no: fileNo};
            items.push(item);
        }
        insertImages(dev, items, fileNo);
        return items;
    }

    function insertImages(obj, items, fileNo) {
        /*
          <p class="mb-5"><img src="images/img_6.jpg" alt="Image" class="img-fluid"></p>
        */
        var html = '';

        for (var i = 0; i < items.length; i++) {
            html += '<p class="mb-5"><img src="' + items[i].url + '"  alt="Image" class="img-fluid" id="' + items[i].id + '"></p>\n<p><br\></p>\n';
        }


        execCommandOnElement(obj, 'insertHTML', html);
    }

    function uploadImages(files) {
        window.uploadImageNo += files.length;
        for (var i = 0; i < files.length; i++) {
            var item = files[i];
            var formData = new FormData();
            formData.append('upload', item.file);
            $.ajax({
                url: "/image/upload",
                data: formData,
                type: "POST",
                dataType: "json",
                cache: false,//上传文件无需缓存
                processData: false,//用于对data参数进行序列化处理 这里必须false
                contentType: false, //必须
                mimeType: "multipart/form-data",
                // async: false,
                success: function (obj) {
                    if (obj.success) {
                        $("#" + item.id).attr("src", obj.url);
                        window.uploadedImageNo += 1;
                    } else {
                        alert("upload error")
                    }
                },
            })
        }
    }


    function execCommandOnElement(el, commandName, value) {
        if (typeof value == "undefined") {
            value = null;
        }

        if (typeof window.getSelection != "undefined") {
            // Non-IE case
            var sel = window.getSelection();

            // Save the current selection
            var savedRanges = [];
            for (var i = 0, len = sel.rangeCount; i < len; ++i) {
                savedRanges[i] = sel.getRangeAt(i).cloneRange();
            }

            // Temporarily enable designMode so that
            // document.execCommand() will work
            document.designMode = "on";

            // Select the element's content
            sel = window.getSelection();
            var range = document.createRange();
            range.selectNodeContents(el);
            // sel.removeAllRanges();
            // sel.addRange(range);

            // Execute the command
            document.execCommand(commandName, false, value);

            // Disable designMode
            document.designMode = "off";

            // Restore the previous selection
            sel = window.getSelection();
            sel.removeAllRanges();
            for (var i = 0, len = savedRanges.length; i < len; ++i) {
                sel.addRange(savedRanges[i]);
            }
        } else if (typeof document.body.createTextRange != "undefined") {
            // IE case
            var textRange = document.body.createTextRange();
            textRange.moveToElementText(el);
            textRange.execCommand(commandName, false, value);
        }
    }

})(jQuery);