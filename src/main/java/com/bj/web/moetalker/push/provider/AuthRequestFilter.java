package com.bj.web.moetalker.push.provider;

import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.UserFactory;
import com.google.common.base.Strings;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * jersery框架 拦截器 拦截Token
 * 用于所有的请求接口的过滤和拦截
 */
@Provider
public class AuthRequestFilter implements ContainerRequestFilter{

    //实现接口的过滤方法
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        //检测是否是登录注册接口
        String relationPath = ((ContainerRequest)requestContext).getPath(false);
        if (relationPath.startsWith("account/login")
                || relationPath.startsWith("account/register")){
            //直接return返回，不走拦截流程
            return;
        }

        //从Header中取找到第一个Token节点
        String token = requestContext.getHeaders().getFirst("token");
        if (!Strings.isNullOrEmpty(token)){
            //查询自己的信息
            final User self = UserFactory.findByToken(token);
            if(self != null){

                //设置一个给当前请求添加一个上下文
                requestContext.setSecurityContext(new SecurityContext() {
                    //主体部分
                    @Override
                    public Principal getUserPrincipal() {
                        //User实现Principal接口
                        return self;
                    }

                    //可以在这里写入用户的权限，role是权限名
                    //可以管理--管理员权限等等
                    @Override
                    public boolean isUserInRole(String role) {
                        return true;
                    }

                    //这里一般检查是否为Https等安全
                    @Override
                    public boolean isSecure() {
                        //默认false
                        return false;
                    }

                    //其他配置，默认即可
                    @Override
                    public String getAuthenticationScheme() {
                        return null;
                    }
                });
                //写入上下文后就返回
                return;
            }
        }

        //直接返回一个"账户异常，需要登录"的model
        ResponseModel model = ResponseModel.buildAccountError();
        //拦截
        //停止一个请求的继续下发，调用该方法后直接返回请求
        //不会走到Service中去

        //构建一个返回
        Response response = Response
                .status(Response.Status.OK)
                .entity(model)
                .build();
        requestContext.abortWith(response);
    }
}
