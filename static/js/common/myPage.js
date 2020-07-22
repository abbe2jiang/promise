var HPage = {
    divId: '',
    preUrl: '',
    sufUrl: '',
    fun:'',
    page: 1,
    total: 1,
    size: 10,
    len: 5,
    pages: 1,
    range: [],
    init: function (divId, fun, param) {
        this.divId = divId;
        this.fun = fun;
        // this.preUrl = preUrl;
        // this.sufUrl = sufUrl;
        this.page = param.page || this.page;
        this.total = param.total || this.total;
        this.size = param.size || this.size;
        this.len = param.len || this.len;
        this.pages = this.total % this.size == 0 ? parseInt(this.total / this.size) : parseInt(this.total / this.size) + 1;
        this.range = this.createRange(this.page, this.pages, this.len);
        this.show();
    },
    show: function () {
        var range = this.range;
        var preHtml = '';
        if (1 == range[0]) {
            preHtml = '<li class="page-item"><a href="javascript:void(0)" class="page-link">&lt;</a></li>';
        } else {
            preHtml = '<li class="page-item"><a href="javascript:HPage.showPre();" class="page-link">&lt;</a></li>';
        }
        var nextHtml = '';
        if (this.pages == range[range.length - 1]) {
            nextHtml = '<li class="page-item"><a href="javascript:void(0)" class="page-link">&gt;</a></li>';
        } else {
            nextHtml =  '<li class="page-item"><a href="javascript:HPage.showNext();" class="page-link">&gt;</a></li>';
        }

        var html = '';
        for (var i = 0; i < range.length; i++) {
            if (range[i] == this.page) {
                html += '<li class="page-item active"><a href="javascript:void(0)" class="page-link">' + range[i] + '</a></li>';
            } else {
                html += '<li class="page-item" ><a href="javascript:HPage.fun(' + range[i]  + ')" class="page-link">' + range[i] + '</a></li>';
            }
        }

        var pageHtml = '<nav aria-label="Page navigation" class="text-center">'
            + '<ul class="pagination">'
            + preHtml + html + nextHtml + '</ul>'
            + '</nav>';
        $("#" + this.divId).html(pageHtml);
    },
    showPre: function () {
        for (var i = 0; i < this.range.length; i++) {
            var min = this.range[0]
            if (min > 1) {
                this.range.unshift(min - 1);
                this.range.pop();
            }
        }
        this.show();
    },
    showNext: function () {
        for (var i = 0; i < this.range.length; i++) {
            var max = this.range[this.range.length - 1];
            if (this.pages > max) {
                this.range.shift();
                this.range.push(max + 1);
            }
        }
        this.show();
    },
    createRange: function (page, pages, len) {
        var range = [];
        if (len >= pages) {
            for (var i = 1; i <= pages; i++) {
                range.push(i);
            }
        } else {
            var mid = len % 2 == 0 ? parseInt(len / 2) : parseInt(len / 2) + 1;
            if (page - mid <= 0) {
                for (var i = 1; i <= len; i++) {
                    range.push(i);
                }
            } else if (page + mid >= pages) {
                for (var i = len; i > 0; i--) {
                    range.push(pages - i + 1);
                }
            } else if (page - mid > 0 && page + mid < pages) {
                for (var i = mid; i > 0; i--) {
                    range.push(page - i + 1);
                }
                for (var i = 1; i <= (len - mid); i++) {
                    range.push(page + i);
                }
            }
        }
        return range;
    }

}

