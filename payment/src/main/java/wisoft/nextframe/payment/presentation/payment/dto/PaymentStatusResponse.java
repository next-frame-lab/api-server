// payment 모듈 - 공개 DTO
package wisoft.nextframe.payment.presentation.payment.dto;

public record PaymentStatusResponse(String status, String approvedAt) {}
