package com.max.rest.proxy.codec;

import com.max.rest.proxy.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultRestCodec extends AbstractRestCodeC {
    @Override
    public <T> byte[] encodeArg(T obj) {
        return JsonUtil.toJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T decodeResult(int statusCode, Map<String, List<String>> headers, byte[] bytes, Class<T> clazz) {
        T ret = JsonUtil.fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
        return ret;
    }

}
