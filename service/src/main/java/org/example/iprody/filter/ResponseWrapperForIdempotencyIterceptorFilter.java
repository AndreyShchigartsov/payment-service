package org.example.iprody.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static org.example.iprody.constant.WebConstants.WRAPPED_RESPONSE_ATTRIBUTE_NAME;

//Создание фильтра для “обертки” ответа (необходимо для того, чтобы можно
//было вычитывать response несколько раз за весь цикл запрос-ответ
public class ResponseWrapperForIdempotencyIterceptorFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            wrapResponseForNonIdempotentMethods(chain, httpRequest, httpResponse);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static void wrapResponseForNonIdempotentMethods(FilterChain chain,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse httpResponse) throws IOException, ServletException {
        var method = HttpMethod.valueOf(httpRequest.getMethod());

        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PATCH)) {
            var wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
            httpRequest.setAttribute(WRAPPED_RESPONSE_ATTRIBUTE_NAME, wrappedResponse);

            try {
                chain.doFilter(httpRequest, wrappedResponse);
            } finally {
                wrappedResponse.copyBodyToResponse();
            }
        } else {
            chain.doFilter(httpRequest, httpResponse);
        }
    }
}
