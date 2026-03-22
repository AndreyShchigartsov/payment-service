package org.example.iprody.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.iprody.dto.PaymentRequest;
import org.example.iprody.dto.PaymentResponse;
import org.example.iprody.dto.PaymentStatusUpdateRequest;
import org.example.iprody.valueobject.PaymentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Tag(name = "Payment Controller", description = "API для управления платежами")
public interface PaymentControllerDoc {

    @Operation(
            summary = "Создать новый платеж",
            description = "Создает новый платеж для заказа. Статус платежа устанавливается в PENDING."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Платеж успешно создан",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "409", description = "Платеж для этого заказа уже существует")
    })
    ResponseEntity<PaymentResponse> createPayment(
            @Parameter(description = "Данные для создания платежа", required = true)
            @RequestBody PaymentRequest request
    );

    @Operation(
            summary = "Получить платеж по ID",
            description = "Возвращает информацию о платеже по его UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Платеж найден",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Платеж не найден")
    })
    ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "UUID платежа", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    );

    @Operation(
            summary = "Получить платеж по номеру",
            description = "Возвращает информацию о платеже по его уникальному номеру (формат: PAY-XXXXXXXXX-XXXXXXXX)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Платеж найден",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Платеж не найден")
    })
    ResponseEntity<PaymentResponse> getPaymentByNumber(
            @Parameter(description = "Уникальный номер платежа", required = true, example = "PAY-1734567890123-abc12345")
            @PathVariable String paymentNumber
    );

    @Operation(
            summary = "Получить все платежи по заказу",
            description = "Возвращает список всех платежей для указанного заказа"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список платежей получен",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            @Parameter(description = "ID заказа", required = true, example = "ORD-123456")
            @PathVariable String orderId
    );

    @Operation(
            summary = "Получить все платежи",
            description = "Возвращает список всех платежей в системе"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список платежей получен",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    ResponseEntity<List<PaymentResponse>> getAllPayments();

    @Operation(
            summary = "Получить платежи по статусу",
            description = "Возвращает список платежей с указанным статусом"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список платежей получен",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Статус платежа", required = true,
                    example = "PENDING", schema = @Schema(implementation = PaymentStatus.class))
            @PathVariable PaymentStatus status
    );

    @Operation(
            summary = "Получить количество платежей по статусу",
            description = "Возвращает количество платежей с указанным статусом"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Количество получено",
                    content = @Content(schema = @Schema(implementation = Long.class)))
    })
    ResponseEntity<Long> getPaymentCountByStatus(
            @Parameter(description = "Статус платежа", required = true, example = "COMPLETED")
            @PathVariable PaymentStatus status
    );

    @Operation(
            summary = "Получить общую выручку",
            description = "Возвращает сумму всех успешно завершенных платежей"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сумма получена",
                    content = @Content(schema = @Schema(implementation = Double.class)))
    })
    ResponseEntity<Double> getTotalRevenue();

    @Operation(
            summary = "Обновить платеж",
            description = "Полностью обновляет информацию о платеже"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Платеж успешно обновлен",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Платеж не найден")
    })
    ResponseEntity<PaymentResponse> updatePayment(
            @Parameter(description = "UUID платежа", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Обновленные данные платежа", required = true)
            @RequestBody PaymentRequest request
    );

    @Operation(
            summary = "Обновить статус платежа",
            description = "Изменяет статус платежа. Поддерживаемые статусы: COMPLETED, FAILED, REFUNDED, CANCELLED"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Недопустимый статус"),
            @ApiResponse(responseCode = "404", description = "Платеж не найден")
    })
    ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(description = "UUID платежа", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Новый статус платежа", required = true)
            @RequestBody PaymentStatusUpdateRequest request
    );

    @Operation(
            summary = "Удалить платеж",
            description = "Удаляет платеж по его UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Платеж успешно удален"),
            @ApiResponse(responseCode = "404", description = "Платеж не найден")
    })
    ResponseEntity<Void> deletePayment(
            @Parameter(description = "UUID платежа", required = true)
            @PathVariable UUID id
    );
}
