/**
 *字符串相关操作
 */

var HString = {
    /**字符串替换content:模板，dict字典*/
    replace : function(content,dict) {
        for (var key in dict) {
            var pattern = new RegExp(key,'g');
            content = content.replace(pattern,dict[key]);
        }
        return content;
    },
    trim : function(str) {
        return str.replace(/(^\s*)|(\s*$)/g, "");
    },
    strip : function(str) {
        return HString.trim(str);
    }

}