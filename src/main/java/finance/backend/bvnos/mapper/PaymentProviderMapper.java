package finance.backend.bvnos.mapper;

import finance.backend.bvnos.model.ProviderPayment;
import finance.backend.bvnos.model.ProviderPaymentRequestDTO;
import finance.backend.bvnos.model.ProviderPaymentResponseDTO;

import java.time.LocalDateTime;

public class PaymentProviderMapper {

    public static ProviderPayment toEntity(ProviderPaymentRequestDTO dto) {

        ProviderPayment payment = new ProviderPayment();

        payment.setProviderId(dto.getProviderId());
        payment.setProviderName(dto.getProviderName());

        payment.setAmount(dto.getAmount());

        payment.setPaymentDate(dto.getPaymentDate());
        payment.setStatus(dto.getStatus());

        payment.setDescription(dto.getDescription());

        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        payment.setActive(true);

        return payment;
    }

    public static ProviderPaymentResponseDTO toResponse(ProviderPayment payment) {

        ProviderPaymentResponseDTO dto = new ProviderPaymentResponseDTO();

        dto.setId(payment.getId());

        dto.setProviderId(payment.getProviderId());
        dto.setProviderName(payment.getProviderName());

        dto.setAmount(payment.getAmount());

        dto.setPaymentDate(payment.getPaymentDate());
        dto.setStatus(payment.getStatus());

        dto.setDescription(payment.getDescription());

        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        dto.setActive(payment.getActive());

        return dto;
    }
}
