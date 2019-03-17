rest接口代理使用方法：
# 新建接口

使用注解表明必要参数。

## 接口注解@RestResource

`value`：对应*rest-proxy.json*中key值。在这里为*sendHttp*。

`codec`：目前只有一种*com.max.rest.proxy.codec.DefaultRestCodec*。

`contentType`：HTTP请求时的*Content-Type*。

## 方法注解

可选有@GET、@POST、@PUT、@DELETE。

`value`：请求路径。

`contentType`：HTTP请求时的*Content-Type*。

`serviceKey`：配置文件中的*key*。

`socketReadTimeoutSecond`：单位秒。*socketTimeOut*取本值和全局变量最大的一个。

```java
@RestResource(value = "sendHttp", codec = "com.max.rest.proxy.codec.DefaultRestCodec", contentType = "application/json")
public interface SendHttp {
    @GET(value = "/open/callcenter/param/{test}", desc = "测试方法")
    String getResult(@PathParam("test") String test);
}
```

# rest调用的配置文件

名称自定义。在xml文件*p:location*配置。**rest-proxy.json**

```json
{
  "sendHttp": {
    // http调用域名
    "address": "https://open.ceshi.com",
    // 名称
    "serviceName":"测试rest调用",
    // socketTimeOut。默认5000
    "socketTimeOut": 20000,
    // connectionTimeOut。默认2000
    "connectionTimeOut": 2000
 	}
}
```

# 配置文件

```xml
<bean id="restServiceProxyFactory"
          class="com.max.rest.proxy.RestServiceProxyFactory"
          p:location="classpath*:rest-proxy.json"
          init-method="init"/>

<bean id="sendHttp"
          class="com.max.rest.proxy.RestServiceProxyFactoryBean"
          p:type="com.facishare.open.rainbowhorse.SendHttp">
        <property name="factory" ref="restServiceProxyFactory"/>
</bean>
```

# 使用

```java
@Autowired
private SendHttp sendHttp;

pubic void test(){
  String result = sendHttp.getResult("test");
}
```

