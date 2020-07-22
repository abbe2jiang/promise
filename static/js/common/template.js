var HTemplate = function (functionObject) {
    return function (scope) {
        return functionObject.toString().match(/\/\*([\s\S]*?)\*\//)[1].replace(/\$\{.+?\}/g, function (variable) {
            var value = scope;
            if(value==null)return "";
            variable = variable.replace('${', '').replace('}', '');
            variable.split('.').forEach(function (section) {
                value = value[section];
            });
            return value;
        });
    }
};

var HTemplateString = function (str) {
    return function (scope) {
        return str.replace(/\$\{.+?\}/g, function (variable) {
            var value = scope;
            variable = variable.replace('${', '').replace('}', '');
            variable.split('.').forEach(function (section) {
                value = value[section];
            });
            return value;
        });
    }
};