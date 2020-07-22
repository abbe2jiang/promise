(function ($) {
    getByCategory(1);

    function getByCategory(page) {
        var template = HTemplate(function () {/*
                      <div class="post-entry-horzontal">
                        <a href="/blog/${id}">
                          <div class="image element-animate" data-animate-effect="fadeIn"
                               style="background-image: url(${profile.compressUrl});"></div>
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
                $("#divSign-categories").html(html);
                contentWayPoint();
                HPage.init("_blog_page", getByCategory, pagination);
            }
        }
        var categoryId = $("#_category_id").val();
        HAjax.jsonGet("/blogs/category/"+categoryId+"/"+page,success);
    }

    var contentWayPoint = function() {
        var i = 0;
        $('.element-animate').waypoint( function( direction ) {

            if( direction === 'down' && !$(this.element).hasClass('element-animated') ) {

                i++;

                $(this.element).addClass('item-animate');
                setTimeout(function(){

                    $('body .element-animate.item-animate').each(function(k){
                        var el = $(this);
                        setTimeout( function () {
                            var effect = el.data('animate-effect');
                            if ( effect === 'fadeIn') {
                                el.addClass('fadeIn element-animated');
                            } else if ( effect === 'fadeInLeft') {
                                el.addClass('fadeInLeft element-animated');
                            } else if ( effect === 'fadeInRight') {
                                el.addClass('fadeInRight element-animated');
                            } else {
                                el.addClass('fadeInUp element-animated');
                            }
                            el.removeClass('item-animate');
                        },  k * 100);
                    });

                }, 100);

            }

        } , { offset: '95%' } );
    };
})(jQuery);