package org.example.iprody.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.iprody.entity.IdempotencyKey;
import org.example.iprody.enums.IdempotencyKeyStatus;
import org.example.iprody.exception.IdempotencyKeyExistsException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static org.example.iprody.constant.WebConstants.WRAPPED_RESPONSE_ATTRIBUTE_NAME;


@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String IDEMPOTENT_KEY_HEADER_NAME = "X-Idempotency-Key";

    private final IdempotencyService idempotencyService;

    //Создаем интерсептор, который будет отлавливать все POST и PATCH запросы,
    //проверять их на наличие заголовка с ключем идемпотентности и обрабатывать сам ключ
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        var method = HttpMethod.valueOf(request.getMethod());

        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PATCH)) {
            var idempotencyKey = request.getHeader(IDEMPOTENT_KEY_HEADER_NAME);
            //Если заголовок с ключем отсутствует - вернем 400 Bad Request с сообщением.
            if (StringUtils.isBlank(idempotencyKey)) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter().println("X-Idempotency-Key is not present");
                return false;
            }

            return processIdempotency(idempotencyKey, response);
        }

        return true;
    }

    private boolean processIdempotency(String idempotencyKey,
                                       HttpServletResponse response) throws IOException {

        var existingKey = idempotencyService.getByKey(idempotencyKey);

        if (existingKey.isPresent()) {
            //Если ключ идемпотентности уже существует в БД и от помечен как
            //COMPLETED - мы вернем уже сохраненный результат для этого ключа
            //Если же ключ находится в процессе обработки (PENDING), то в нашей реализации мы просто
            //выбросим ошибку 409 Conflict с сообщением, что запрос уже обрабатывается.
            return processExistingKey(existingKey.get(), response);
        } else {
            //Если же ключ еще не сохранен в БД, то сохраняем его со статусом PENDING и
            //отдаем наш запрос дальше на наш контроллер. Если возникнет ситуация, когда
            //параллельный запрос уже сохранил такой ключ, то считаем, что был выполнен retry и
            //вернем 409 Conflict с сообщением, что такой запрос уже обрабатывается
            return createNewKey(idempotencyKey, response);
        }
    }

    private boolean processExistingKey(IdempotencyKey idempotencyKey,
                                       HttpServletResponse response) throws IOException {
        var status = idempotencyKey.getStatus();

        if (status == IdempotencyKeyStatus.PENDING) {
            response.setStatus(HttpStatus.CONFLICT.value());
            response.getWriter().println("Same request is already in progress...");
        } else if (status == IdempotencyKeyStatus.COMPLETED) {
            response.setStatus(idempotencyKey.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().println(idempotencyKey.getResponse());
        } else {
            throw new IllegalArgumentException("Invalid status of idempotency key");
        }

        return false;
    }

    private boolean createNewKey(String idempotencyKey,
                                 HttpServletResponse response) throws IOException {
        try {
            idempotencyService.createPendingKey(idempotencyKey);
            return true;
        } catch (IdempotencyKeyExistsException e) {
            response.setStatus(HttpStatus.CONFLICT.value());
            response.getWriter().println("Same request is already in progress...");
            return false;
        }
    }

    //После того, как наш запрос был обработан контроллером и т.д., нам
    //необходимо для нашего ключа идемпотентности сохранить статус ответа и тело
    //ответа, дабы в случае повторных запросов мы вернули уже полученный ранее ответ.
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        var method = HttpMethod.valueOf(request.getMethod());

        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PATCH)) {
            // Обертка необходима для возможности повторных чтений response-а
            var wrappedResponse = (ContentCachingResponseWrapper) request.getAttribute(WRAPPED_RESPONSE_ATTRIBUTE_NAME);
            var responseBody = new String(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding());

            var idempotencyKey = request.getHeader(IDEMPOTENT_KEY_HEADER_NAME);
            idempotencyService.markAsCompleted(idempotencyKey, responseBody, response.getStatus());
        }
    }
}

