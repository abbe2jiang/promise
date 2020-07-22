(function ($) {

    document.getElementById("_blog_title").contentEditable = "true";
    window.contentDiv = document.getElementById("_blog_content");
    window.contentDiv.contentEditable = "true";

    window.fileNo = 1;
    window.uploadImageNo = 0;
    window.uploadedImageNo = 0;
    window.posted = false;

    window.initTitle = $("#_blog_title").html();
    window.initContent = $("#_blog_content").html();

    initText("_blog_title", window.initTitle, "");
    // initText("_blog_content", window.initContent, "");

    function initText(id, showText, initText) {
        $("#" + id).on("focus", function () {
            if ($("#" + id).html() == showText) {
                $("#" + id).html(initText);
            }
            $("#" + id).focus();
        })

        $("#" + id).on("blur", function () {
            if ($("#" + id).html() == initText || $("#" + id).html() == "<br>") {
                $("#" + id).html(showText);
            }
        })
    }


    initCategory();

    function initCategory() {
        var success = function (data) {
            if (data.success) {
                var categories = data.data;
                if (categories.length == 0) {
                    categories.push({id: 'Default', name: 'Default'});
                }
                var html = "";
                var template = HTemplate(function () {/*
                        '<option value="${id}">${name}</option>'
                    */
                })
                for (var i = 0; i < categories.length; i++) {
                    html += template(categories[i]);
                }

                $("#_blog_category").html(html);
            }
        }
        HAjax.jsonGet("/category", success);
    }

    $("#_blog_profile").on("click", function () {
        $("#_file_blog_profile").click();
        $("#_file_blog_profile").change(function () {
            var file = this.files[0];
            if (file.size > window.IMAGE_MAX_SIZE) {
                showMessage("image is too large.")
                return;
            }
            var url = window.URL.createObjectURL(file);
            $("#_blog_profile").attr("src", url);
            uploadImages([{id: '_blog_profile', file: file}]);
        })
    })

    $("#_insert_image").on("click", function () {
        $("#_blog_content").focus();
        $('#_file_' + window.fileNo).click();
        $('#_file_' + window.fileNo).change(function () {
            var items = showImage(this, window.contentDiv, window.fileNo);
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

    $("#_blog_post").on("click", function () {
        showLoader();
        setInterval(post, 1000);
    });


    function post() {
        if (window.uploadImageNo != window.uploadedImageNo) {
            // var no = window.uploadImageNo - window.uploadedImageNo;
            // showMessage(no + "images are uploading")
            return;
        }
        if (window.posted) {
            return;
        }
        window.posted = true;
        var success = function (data) {
            if (data.success) {
                location.href = "/";
            } else {
                showMessage('fail');
            }
        }
        var profile = $("#_blog_profile").attr("src");
        var category = $("#_blog_category").val();
        var title = $("#_blog_title").html();
        var content = $("#_blog_content").html();
        var data = {profile: profile, category: category, title: title, content: content};
        HAjax.jsonPost("/blog", data, success);
    }

    function uploadImages(files) {
        window.uploadImageNo += files.length;
        for (var i = 0; i < files.length; i++) {
            var item = files[i];
            var formData = new FormData();
            formData.append('upload', item.file);
            $.ajax({
                url: "image/upload",
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

    function showImage(obj, dev, fileNo) {
        var items = [];
        for (var i = 0; i < obj.files.length; i++) {
            var file = obj.files[i];
            if (file.size > window.IMAGE_MAX_SIZE) {
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
          <div class="row mb-5">
            <div class="col-md-12 mb-4">
              <img src="${url1}" alt="Image" class="img-fluid">
            </div>
            <div class="col-md-6 mb-4">
              <img src="${url2}" alt="Image" class="img-fluid">
            </div>
            <div class="col-md-6 mb-4">
              <img src="${url2}" alt="Image" class="img-fluid">
            </div>
          </div>
        */
        var html = '<div class="row mb-5"  id="_image_div_' + fileNo + '">\n';

        for (var i = 0; i < items.length; i++) {
            var col = i == 0 && items.length % 2 == 1 ? "col-md-12" : "col-md-6";
            html += '<div class="' + col + ' mb-4">\n' +
                '<img src="' + items[i].url + '"  alt="Image" class="img-fluid" id="' + items[i].id + '">\n' +
                '</div>\n';
        }
        html += '</div><p><br/></p>'


        execCommandOnElement(obj, 'insertHTML', html);
        obj.focus();
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

    function showMessage(msg) {
        $('#_show_message_body').html(msg)
        $('#_show_message').modal('show')
    }

})(jQuery);