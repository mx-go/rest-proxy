package com.max.rest.proxy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.max.rest.proxy.codec.AbstractRestCodeC;
import com.max.rest.proxy.config.ServiceConfig;
import com.max.rest.proxy.exception.RestProxyInvokeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RestClient {

    private CloseableHttpClientManager clientManager;

    public RestClient() {
        clientManager = new CloseableHttpClientManager();
    }

    public <R> R invoke(InvokeParams invokeParams) {
        DefaultRestResponseHandler<R> handler = new DefaultRestResponseHandler<>(invokeParams.getServiceUrl()
                , invokeParams.getResultClazz()
                , invokeParams.getCodec());

        CloseableHttpClient serviceClient = clientManager.getClient();
        HttpUriRequest request = HTTPRequestBuilderFactory.create(invokeParams, invokeParams.getCodec()).build();
        try (CloseableHttpResponse response = serviceClient.execute(request)) {
            R rst = handler.handleResponse(response);
            if (rst == null) {
                throw new RestProxyInvokeException("返回结果为空");
            }
            return rst;
        } catch (NoHttpResponseException | ConnectTimeoutException e) {
            if (invokeParams.getRetryTimes() < 2) {
                log.info("retry request invoke:url:{},times:{}", invokeParams.getServiceUrl(), invokeParams.getRetryTimes());
                invokeParams.incrRetryTimes();
                return invoke(invokeParams);
            }
            throw new RestProxyInvokeException("访问服务异常", e);
        } catch (IOException e) {
            throw new RestProxyInvokeException("访问服务异常", e);
        }
    }

    public static final class DefaultRestResponseHandler<T> implements ResponseHandler<T> {

        private Class<T> clazz;
        private AbstractRestCodeC codeC;
        private String uri;

        public DefaultRestResponseHandler(String uri, Class<T> clazz, AbstractRestCodeC codeC) {
            this.uri = uri;
            this.clazz = clazz;
            this.codeC = codeC;
        }

        @Override
        public T handleResponse(HttpResponse response) throws IOException {

            int statusCode = response.getStatusLine().getStatusCode();
            Map<String, List<String>> headers = Maps.newHashMap();
            Header[] tempHeaders = response.getAllHeaders();
            for (Header header : tempHeaders) {
                if (header.getName() != null && CollectionUtils.isEmpty(headers.get(header.getName().toLowerCase()))) {
                    headers.put(header.getName().toLowerCase(), Lists.newArrayList(header.getValue()));
                } else {
                    headers.get(header.getName().toLowerCase()).add(header.getValue());
                }
            }
            HttpEntity in = response.getEntity();
            return codeC.decodeResult(statusCode, headers, in != null ? EntityUtils.toByteArray(in) : null, clazz);
        }

        public String getUri() {
            return uri;
        }
    }

    static class HTTPRequestBuilderFactory {
        public static RequestBuilder create(InvokeParams invokeParams, AbstractRestCodeC codeC) {
            return RestProxyRequestBuilder.create(invokeParams.getMethodType()).build(invokeParams, codeC);
        }

        enum RestProxyRequestBuilder implements HTTPRequestBuilder {
            /**
             * GET方法
             */
            GET {
                @Override
                public RequestBuilder build(InvokeParams invokeParams, AbstractRestCodeC codeC) {
                    RequestBuilder requestBuilder = RequestBuilder.get();
                    setHttpHeaderAndUri(invokeParams, requestBuilder);
                    return requestBuilder;
                }
            },
            /**
             * DELETE方法
             */
            DELETE {
                @Override
                public RequestBuilder build(InvokeParams invokeParams, AbstractRestCodeC codeC) {
                    RequestBuilder requestBuilder = RequestBuilder.delete();
                    setHttpHeaderAndUri(invokeParams, requestBuilder);
                    return requestBuilder;
                }
            },
            /**
             * POST方法
             */
            POST {
                @Override
                public RequestBuilder build(InvokeParams invokeParams, AbstractRestCodeC codeC) {
                    RequestBuilder requestBuilder = RequestBuilder.post(invokeParams.getServiceUrl());
                    setEntity(requestBuilder, invokeParams, codeC);
                    setHttpHeaderAndUri(invokeParams, requestBuilder);
                    return requestBuilder;
                }
            },
            /**
             * PUT方法
             */
            PUT {
                @Override
                public RequestBuilder build(InvokeParams invokeParams, AbstractRestCodeC codeC) {
                    RequestBuilder requestBuilder = RequestBuilder.put(invokeParams.getServiceUrl());
                    setEntity(requestBuilder, invokeParams, codeC);
                    setHttpHeaderAndUri(invokeParams, requestBuilder);
                    return requestBuilder;
                }
            };

            void setHttpHeaderAndUri(InvokeParams invokeParams, RequestBuilder requestBuilder) {
                for (Map.Entry<String, String> e : invokeParams.getHeaders().entrySet()) {
                    requestBuilder.addHeader(e.getKey(), e.getValue());
                }
                requestBuilder.setUri(invokeParams.getServiceUrl());
                setRequestConfig(requestBuilder, invokeParams);
            }

            void setRequestConfig(RequestBuilder builder, InvokeParams invokeParams) {
                ServiceConfig serviceConfig = invokeParams.getServiceConfig();
                RequestConfig.Builder rb = RequestConfig.custom();
                rb.setSocketTimeout(invokeParams.getSocketTimeOut() <= 0 ?
                        CloseableHttpClientManager.DEFAULT_SOCKET_TIMEOUT : invokeParams.getSocketTimeOut());
                rb.setConnectTimeout(serviceConfig.getConnectionTimeOut() <= 0 ?
                        CloseableHttpClientManager.DEFAULT_CONNECTION_TIMEOUT : serviceConfig.getConnectionTimeOut());
                builder.setConfig(rb.build());
            }

            public void setEntity(RequestBuilder requestBuilder, InvokeParams invokeParams, AbstractRestCodeC codeC) {
                BasicHttpEntity entity = new BasicHttpEntity();
                entity.setContentEncoding("UTF-8");
                if (invokeParams.getBody() != null) {
                    byte[] bytes = codeC.encodeArg(invokeParams.getBody());
                    entity.setContentLength(bytes.length);
                    entity.setContent(new ByteArrayInputStream(bytes));
                    requestBuilder.setEntity(entity);
                }
            }

            public static HTTPRequestBuilder create(String methodType) {
                return valueOf(methodType);
            }

        }

        interface HTTPRequestBuilder {
            RequestBuilder build(InvokeParams invokeParams, AbstractRestCodeC codeC);
        }
    }

    private static class CloseableHttpClientManager {
        protected static final int DEFAULT_MAX_CONNECTION = 512;
        protected static final int DEFAULT_MAX_PER_ROUTE_CONNECTION = 50;
        static final int DEFAULT_SOCKET_TIMEOUT = 5000;
        protected static final int DEFAULT_CONNECTION_TIMEOUT = 2000;

        private PoolingHttpClientConnectionManager connectionManager;
        private CloseableHttpClient defaultHttpClient;
        private static ScheduledExecutorService closeExecutor = Executors.newScheduledThreadPool(10);

        {
            closeExecutor.scheduleAtFixedRate(() -> {
                if (connectionManager != null) {
                    log.info("client connection state:{}", connectionManager.getTotalStats());
                    Set<HttpRoute> routes = connectionManager.getRoutes();
                    routes.forEach(route -> {
                        HttpHost host = route.getTargetHost();
                        log.info("client connection state: address:{},port:{},hopCount", host.getAddress().getAddress(), host.getPort(), route.getHopCount());
                    });
                }
            }, 1, 10, TimeUnit.MINUTES);
        }

        CloseableHttpClientManager() {
            resetDefaultClient();
        }

        private CloseableHttpClient resetDefaultClient() {
            log.info("reset Default Client\n");
            connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(DEFAULT_MAX_CONNECTION);
            connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE_CONNECTION);
            SocketConfig.Builder sb = SocketConfig.custom();
            sb.setSoKeepAlive(true);
            sb.setTcpNoDelay(true);
            connectionManager.setDefaultSocketConfig(sb.build());
            RequestConfig.Builder rb = RequestConfig.custom();
            rb.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
            rb.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            RequestConfig defaultRequestConfig = rb.build();
            HttpClientBuilder hb = HttpClientBuilder.create();
            hb.setConnectionManager(connectionManager);
            hb.setDefaultRequestConfig(defaultRequestConfig);
            CloseableHttpClient oldClient = defaultHttpClient;
            defaultHttpClient = hb.build();
            closeSocketClient(oldClient);
            return defaultHttpClient;
        }

        public CloseableHttpClient getClient() {
            if (defaultHttpClient == null) {
                return resetDefaultClient();
            }
            return defaultHttpClient;
        }

        private void closeSocketClient(CloseableHttpClient client) {
            if (client == null) {
                return;
            }
            log.info("closeSocket submit close client {}", client);
            closeExecutor.schedule(() -> {
                try {
                    client.close();
                    log.info("closeSocket execute close client {}", client.toString());
                } catch (Exception e) {
                    log.error("Close httpclient error", e);
                }
            }, 20, TimeUnit.SECONDS);
        }
    }

}
