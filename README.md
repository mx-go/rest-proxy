rest接口代理使用方法：
# 新建接口

使用注解表明必要参数。

```java
@RestResource(value = "sendHttp", codec = "com.max.rest.proxy.codec.DefaultRestCodec", contentType = "application/json")
public interface SendHttp {
    @GET(value = "/open/callcenter/param/{test}", desc = "测试方法")
    String getResult(@PathParam("test") String test);
}
```

## 接口注解@RestResource

`value`：对应*rest-proxy.json*中key值。在这里为*sendHttp*。

`desc`：描述接口的备注，仅作为展示。

`codec`：目前只有一种*com.max.rest.proxy.codec.DefaultRestCodec*。

`contentType`：HTTP请求时的*Content-Type*。

## 方法注解

可选有@GET、@POST、@PUT、@DELETE四种请求类型。

`value`：请求路径。

`desc`：描述方法的备注，仅作为展示。

`contentType`：HTTP请求时的*Content-Type*。

`serviceKey`：配置文件中的*key*。

`socketReadTimeoutSecond`：单位秒。*socketTimeOut*取本值和全局变量最大的一个。

# rest调用的配置文件

这里配置是在本地文件，项目中有配置中心可从配置中心拉取。

名称自定义。在xml文件*p:location*配置。

**rest-proxy.json**如下所示：

```json
{
  "sendHttp": {
    // http调用域名
    "address": "https://open.ceshi.com",
    // 名称，相当与注释作用
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
          p:type="com.max.open.SendHttp">
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

