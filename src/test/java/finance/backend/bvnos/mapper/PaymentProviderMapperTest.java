package finance.backend.bvnos.mapper;

import finance.backend.bvnos.model.ProviderPayment;
import finance.backend.bvnos.model.ProviderPaymentRequestDTO;
import finance.backend.bvnos.model.ProviderPaymentResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentProviderMapperTest {

    @Test
    @DisplayName("Should map ProviderPaymentRequestDTO to ProviderPayment entity")
    void shouldMapToEntity() {
        ProviderPaymentRequestDTO dto = new ProviderPaymentRequestDTO();
        dto.setProviderId("prov-1");
        dto.setProviderName("Provider One");
        dto.setAmount(new BigDecimal("1500.00"));
        dto.setPaymentDate(LocalDate.of(2025, 3, 15));
        dto.setStatus("PAID");
        dto.setDescription("Payment for March");

        ProviderPayment entity = PaymentProviderMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getProviderId()).isEqualTo("prov-1");
        assertThat(entity.getProviderName()).isEqualTo("Provider One");
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(entity.getPaymentDate()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(entity.getStatus()).isEqualTo("PAID");
        assertThat(entity.getDescription()).isEqualTo("Payment for March");
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should map ProviderPayment entity to ProviderPaymentResponseDTO")
    void shouldMapToResponse() {
        ProviderPayment entity = new ProviderPayment();
        entity.setId("pay-123");
        entity.setProviderId("prov-1");
        entity.setProviderName("Provider One");
        entity.setAmount(new BigDecimal("1500.00"));
        entity.setPaymentDate(LocalDate.of(2025, 3, 15));
        entity.setStatus("PAID");
        entity.setDescription("Payment for March");
        entity.setActive(true);

        ProviderPaymentResponseDTO dto = PaymentProviderMapper.toResponse(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("pay-123");
        assertThat(dto.getProviderId()).isEqualTo("prov-1");
        assertThat(dto.getProviderName()).isEqualTo("Provider One");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(dto.getPaymentDate()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(dto.getStatus()).isEqualTo("PAID");
        assertThat(dto.getDescription()).isEqualTo("Payment for March");
        assertThat(dto.getActive()).isTrue();
    }
}
