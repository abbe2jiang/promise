(function($) {
    latest(1);
    function latest(page) {
        var template = HTemplate(function () {/*
                    <div class="col-md-6">
                      <a href="/blog/${id}" class="blog-entry element-animate" data-animate-effect="fadeIn">
                        <img src="${profile.compressUrl}" alt="Image">
                        <div class="blog-content-body">
                          <div class="post-meta">
                            <span class="author mr-2"><img src="${author.portrait.compressUrl}" alt="image"> ${author.firstName}</span>&bullet;
                            <span class="mr-2">${date} </span> &bullet;
                            <span class="ml-2"><span class="fa fa-comments"></span> ${comments}</span>
                          </div>
                          <h2>${title}</h2>
                        </div>
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
                $("#divSign-latest").html(html);
                contentWayPoint();
                HPage.init("_blog_page", latest, pagination);
            }
        }
        HAjax.jsonGet("/blogs/"+page,success);
    }




})(jQuery);