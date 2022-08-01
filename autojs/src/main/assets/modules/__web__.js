

module.exports = function(__runtime__, scope){
    scope.newInjectableWebClient = function(){
        return new com.pp.autojs.core.web.InjectableWebClient(org.mozilla.javascript.Context.getCurrentContext(), scope);
    }

    scope.newInjectableWebView = function(activity){
        return new com.pp.autojs.core.web.InjectableWebView(scope.activity, org.mozilla.javascript.Context.getCurrentContext(), scope);
    }
}


