/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.shamrock.as.google.gate.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.haulmont.monaco.AbstractCommand;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.failsafe.CircuitBreakerRegistry;
import com.haulmont.monaco.jackson.ObjectReaderWriterFactory;
import com.haulmont.monaco.metrics.MetricRegistry;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.as.google.gate.GateConfiguration;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.nike.fastbreak.CircuitBreaker;
import com.nike.fastbreak.exception.CircuitBreakerOpenException;
import com.nike.fastbreak.exception.CircuitBreakerTimeoutException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class UnirestCommand<T> extends AbstractCommand<T> {
    public static final int DEFAULT_TIMEOUT = 60000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String service;
    protected Class<T> responseClass;

    public UnirestCommand(String service, Class<T> responseClass) {
        this.service = service;
        this.responseClass = responseClass;
    }

    public T execute() throws ServiceException {
        final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.getInstance();

        final Path path = getPath();
        final String url = getUrl();
        final BaseRequest request = createRequest(url, path);

        final CircuitBreaker<T> circuitBreaker = circuitBreakerRegistry.get(service, path.getPath(), String.valueOf(request.getHttpRequest().getHttpMethod()));

        final T res;
        try {
            res = circuitBreaker.executeBlockingCall(() -> __execute(url, path, request));
        } catch (CircuitBreakerOpenException | CircuitBreakerTimeoutException e) {
            throw new ServiceException(ErrorCode.GATEWAY_TIMEOUT, e.getMessage(), e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, e.getMessage(), e);
        }

        return res;
    }

    private T __execute(String url, Path path, BaseRequest request) {
        final MetricRegistry metricRegistry = AppContext.getBean(MetricRegistry.class);
        long timestamp = System.currentTimeMillis();

        HttpMethod httpMethod = request.getHttpRequest().getHttpMethod();

        HttpResponse<T> response;

        Future<HttpResponse<T>> future = request.asObjectAsync(responseClass);
        try {
            response = future.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ServiceException(ErrorCode.GATEWAY_TIMEOUT, e.getMessage(), e);
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, e.getMessage(), e);
        }

        int status = response.getStatus();

        long duration = getDuration(timestamp);

        String msg = "Call " + service + path.getPath() + " service (status: " + status + ", X-Forwarded-Host: " + response.getHeaders().getFirst("X-Forwarded-Host") + ") (" + duration + " ms)";
        if (logger.isDebugEnabled()) {
            try {
                logger.debug(msg + "\nUrl: {}\n{}\n", request.getHttpRequest().getUrl(), IOUtils.toString(response.getRawBody()));
            } catch (Throwable t) {}
        } else {
            logger.debug(msg);
        }

        String metricKey = AppContext.getServiceName() + ".services." + service + path.getPath() + "|" + httpMethod + "|status:" + status;
        metricRegistry.timer(metricKey).update(duration, TimeUnit.MILLISECONDS);

        if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED || status == HttpStatus.SC_ACCEPTED) {
            return response.getBody();
        } else if (status == HttpStatus.SC_BAD_REQUEST) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, response.getStatusText());
        } else {
            throw new ServiceException(ErrorCode.FAILED_DEPENDENCY, "Fail to call " + service + path.getPath() + "service method (code: " + status + ")", response.getStatusText());
        }
    }

    private long getTimeout() {
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);
        try {
            return conf.getTimeout() > 0 ? conf.getTimeout() : DEFAULT_TIMEOUT;
        } catch (Throwable t) {
            return DEFAULT_TIMEOUT;
        }
    }

    protected abstract BaseRequest createRequest(String url, Path path);

    //

    private long getDuration(long timestamp) {
        return System.currentTimeMillis() - timestamp;
    }

    //

    public static class Path {
        private String path;
        private Map<String, Object> routeParams;

        public Path(String path) {
            this(path, Collections.emptyMap());
        }

        public Path(String path, Map<String, Object> routeParams) {
            this.path = path;
            this.routeParams = routeParams;
        }

        public String getPath() {
            return path;
        }

        public Map<String, Object> getRouteParams() {
            return routeParams;
        }
    }

    protected abstract String getUrl();
    protected abstract Path getPath();

    private HttpRequest createRequest(HttpMethod method, String url, Path path) {
        HttpRequest request;

        switch (method) {
            case GET:
            case HEAD: {
                request = new GetRequest(method, url + path.getPath());
                break;
            }
            case OPTIONS:
            case POST:
            case DELETE:
            case PATCH:
            case PUT: {
                request = new HttpRequestWithBody(method, url + path.getPath());
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown method: " + method);
            }
        }

        assignPathParams(path, request);

        return assignDefaultHeaders(request);
    }

    private void assignPathParams(Path path, HttpRequest request) {
        for (Map.Entry<String, Object> entry : path.getRouteParams().entrySet()) {
            request.routeParam(entry.getKey(), ConvertUtils.convert(entry.getValue()));
        }
    }

    private <T extends HttpRequest>T assignDefaultHeaders(T request) {
        request
                .header("X-Request-ID", AppContext.getRequestId())
                .header("X-Forwarded-Host", AppContext.getHost().getHostName())
                .header("X-Service-Name", AppContext.getServiceName());

        return request;
    }

    //

    protected GetRequest get(String url, Path path) {
        return (GetRequest) createRequest(HttpMethod.GET, url, path);
    }

    protected HttpRequestWithBody post(String url, Path path) {
        return (HttpRequestWithBody) createRequest(HttpMethod.POST, url, path);
    }

    protected GetRequest head(String url, Path path) {
        return (GetRequest) createRequest(HttpMethod.HEAD, url, path);
    }

    protected HttpRequestWithBody options(String url, Path path) {
        return (HttpRequestWithBody) createRequest(HttpMethod.OPTIONS, url, path);
    }

    protected HttpRequestWithBody delete(String url, Path path) {
        return (HttpRequestWithBody) createRequest(HttpMethod.DELETE, url, path);
    }

    protected HttpRequestWithBody patch(String url, Path path) {
        return (HttpRequestWithBody) createRequest(HttpMethod.PATCH, url, path);
    }

    protected HttpRequestWithBody put(String url, Path path) {
        return (HttpRequestWithBody) createRequest(HttpMethod.PUT, url, path);
    }

    //

    static {
        Unirest.setObjectMapper(new JacksonObjectMapper());
    }

    private static class JacksonObjectMapper implements ObjectMapper {
        private final ObjectReaderWriterFactory rw = new ObjectReaderWriterFactory();

        @Override
        public <T> T readValue(String value, Class<T> valueType) {
            try {
                return rw.reader(valueType).readValue(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String writeValue(Object value) {
            try {
                return rw.writer(value).writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
