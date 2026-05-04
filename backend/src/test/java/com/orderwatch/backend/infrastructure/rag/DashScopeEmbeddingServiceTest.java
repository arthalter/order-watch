package com.orderwatch.backend.infrastructure.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DashScopeEmbeddingServiceTest {

    @AfterEach
    void tearDown() {
    }

    @Test
    void embedsTextWithDashScopeCompatibleEndpoint() throws Exception {
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> requestUrl = new AtomicReference<>();

        String baseUrl = "http://localhost/compatible-mode/v1";

        EmbeddingProperties properties = new EmbeddingProperties();
        properties.setProvider("dashscope");
        properties.setDimension(1024);
        properties.getDashscope().setApiKey("test-key");
        properties.getDashscope().setModel("text-embedding-v4");
        properties.getDashscope().setBaseUrl(baseUrl);
        properties.getDashscope().setTimeoutMs(2000);

        HttpClient fakeHttpClient = new FakeHttpClient(requestUrl, authorizationHeader, requestBody, fakeOkResponseJson());
        DashScopeEmbeddingService service = new DashScopeEmbeddingService(properties, new ObjectMapper(), fakeHttpClient);
        float[] vector = service.embed("hello");

        assertThat(vector).hasSize(1024);
        assertThat(vector[0]).isEqualTo(0.1f);
        assertThat(vector[1023]).isEqualTo(102.4f);

        assertThat(requestUrl.get()).endsWith("/compatible-mode/v1/embeddings");
        assertThat(authorizationHeader.get()).isEqualTo("Bearer test-key");
        assertThat(requestBody.get()).contains("\"model\":\"text-embedding-v4\"");
        assertThat(requestBody.get()).contains("\"input\":\"hello\"");
    }

    private static String fakeOkResponseJson() {
        StringBuilder embedding = new StringBuilder();
        embedding.append('[');
        for (int i = 1; i <= 1024; i++) {
            if (i > 1) embedding.append(',');
            if (i == 1) {
                embedding.append("0.1");
            } else if (i == 1024) {
                embedding.append("102.4");
            } else {
                embedding.append("0.0");
            }
        }
        embedding.append(']');

        return """
                {
                  "data": [
                    { "embedding": %s }
                  ]
                }
                """.formatted(embedding);
    }

    private static final class FakeHttpClient extends HttpClient {
        private final AtomicReference<String> requestUrl;
        private final AtomicReference<String> authorizationHeader;
        private final AtomicReference<String> requestBody;
        private final String responseJson;

        private FakeHttpClient(
                AtomicReference<String> requestUrl,
                AtomicReference<String> authorizationHeader,
                AtomicReference<String> requestBody,
                String responseJson
        ) {
            this.requestUrl = requestUrl;
            this.authorizationHeader = authorizationHeader;
            this.requestBody = requestBody;
            this.responseJson = responseJson;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.of(Duration.ofSeconds(1));
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public javax.net.ssl.SSLContext sslContext() {
            try {
                javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
                ctx.init(null, null, new SecureRandom());
                return ctx;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public javax.net.ssl.SSLParameters sslParameters() {
            return new javax.net.ssl.SSLParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            requestUrl.set(request.uri().toString());
            authorizationHeader.set(request.headers().firstValue("Authorization").orElse(null));

            try {
                if (request.bodyPublisher().isPresent()) {
                    requestBody.set(readBodyPublisher(request.bodyPublisher().get()));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            @SuppressWarnings("unchecked")
            T realBody = (T) responseJson;
            return new FakeHttpResponse<>(200, request.uri(), realBody);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException("not needed");
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException("not needed");
        }

        private static String readBodyPublisher(HttpRequest.BodyPublisher publisher) throws IOException {
            var subscriber = new BodyStringSubscriber();
            publisher.subscribe(subscriber);
            return subscriber.future.join();
        }
    }

    private static final class FakeHttpResponse<T> implements HttpResponse<T> {
        private final int statusCode;
        private final URI uri;
        private final T body;

        private FakeHttpResponse(int statusCode, URI uri, T body) {
            this.statusCode = statusCode;
            this.uri = uri;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (a, b) -> true);
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public Optional<javax.net.ssl.SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    private static final class BodyStringSubscriber implements Flow.Subscriber<ByteBuffer> {
        private final CompletableFuture<String> future = new CompletableFuture<>();
        private final StringBuilder sb = new StringBuilder();
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            sb.append(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public void onError(Throwable throwable) {
            future.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            future.complete(sb.toString());
        }
    }
}
