package finance.backend.bvnos.service;

import finance.backend.bvnos.mapper.PaymentProviderMapper;
import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.ProviderPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderPaymentServiceImplTest {

    @Mock
    private ProviderPaymentRepository providerPaymentRepository;

    @InjectMocks
    private ProviderPaymentServiceImpl providerPaymentService;

    @Captor
    private ArgumentCaptor<ProviderPayment> paymentCaptor;

    private ProviderPaymentRequestDTO requestDTO;
    private ProviderPayment payment1;
    private ProviderPayment payment2;

    @BeforeEach
    void setUp() {
        requestDTO = new ProviderPaymentRequestDTO();
        requestDTO.setProviderId("prov-1");
        requestDTO.setProviderName("Provider One");
        requestDTO.setAmount(new BigDecimal("1500.00"));
        requestDTO.setPaymentDate(LocalDate.of(2025, 3, 15));
        requestDTO.setStatus("PAID");
        requestDTO.setDescription("Payment for March");

        payment1 = new ProviderPayment();
        payment1.setId("pay-1");
        payment1.setProviderId("prov-1");
        payment1.setProviderName("Provider One");
        payment1.setAmount(new BigDecimal("1000.00"));
        payment1.setPaymentDate(LocalDate.of(2025, 1, 15));
        payment1.setStatus("PAID");
        payment1.setDescription("January payment");
        payment1.setActive(true);
        payment1.setCreatedAt(LocalDateTime.now().minusMonths(2));
        payment1.setUpdatedAt(LocalDateTime.now().minusMonths(2));

        payment2 = new ProviderPayment();
        payment2.setId("pay-2");
        payment2.setProviderId("prov-2");
        payment2.setProviderName("Provider Two");
        payment2.setAmount(new BigDecimal("2000.00"));
        payment2.setPaymentDate(LocalDate.of(2025, 2, 15));
        payment2.setStatus("PENDING");
        payment2.setDescription("February payment");
        payment2.setActive(true);
        payment2.setCreatedAt(LocalDateTime.now().minusMonths(1));
        payment2.setUpdatedAt(LocalDateTime.now().minusMonths(1));
    }

    @Nested
    @DisplayName("savePayment tests")
    class SavePaymentTests {

        @Test
        @DisplayName("Should save payment successfully")
        void shouldSavePaymentSuccessfully() {
            ProviderPayment savedPayment = new ProviderPayment();
            savedPayment.setId("new-pay-id");
            savedPayment.setProviderId("prov-1");
            savedPayment.setProviderName("Provider One");
            savedPayment.setAmount(new BigDecimal("1500.00"));
            savedPayment.setPaymentDate(LocalDate.of(2025, 3, 15));
            savedPayment.setStatus("PAID");
            savedPayment.setDescription("Payment for March");
            savedPayment.setActive(true);
            savedPayment.setCreatedAt(LocalDateTime.now());
            savedPayment.setUpdatedAt(LocalDateTime.now());

            when(providerPaymentRepository.save(any(ProviderPayment.class))).thenReturn(savedPayment);

            ProviderPaymentResponseDTO result = providerPaymentService.savePayment(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getProviderId()).isEqualTo("prov-1");
            assertThat(result.getProviderName()).isEqualTo("Provider One");
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
            assertThat(result.getStatus()).isEqualTo("PAID");
            assertThat(result.getDescription()).isEqualTo("Payment for March");
            assertThat(result.getActive()).isTrue();

            verify(providerPaymentRepository).save(any(ProviderPayment.class));
        }
    }

    @Nested
    @DisplayName("updatePayment tests")
    class UpdatePaymentTests {

        @Test
        @DisplayName("Should update payment successfully")
        void shouldUpdatePaymentSuccessfully() {
            ProviderPaymentRequestDTO updateDTO = new ProviderPaymentRequestDTO();
            updateDTO.setProviderId("prov-1");
            updateDTO.setProviderName("Provider One");
            updateDTO.setAmount(new BigDecimal("2500.00"));
            updateDTO.setPaymentDate(LocalDate.of(2025, 4, 1));
            updateDTO.setStatus("PAID");
            updateDTO.setDescription("Updated payment");

            when(providerPaymentRepository.findById("pay-1")).thenReturn(Optional.of(payment1));
            when(providerPaymentRepository.save(any(ProviderPayment.class))).thenAnswer(inv -> inv.getArgument(0));

            ProviderPaymentResponseDTO result = providerPaymentService.updatePayment("pay-1", updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(result.getPaymentDate()).isEqualTo(LocalDate.of(2025, 4, 1));
            assertThat(result.getDescription()).isEqualTo("Updated payment");
            assertThat(result.getStatus()).isEqualTo("PAID");

            verify(providerPaymentRepository).findById("pay-1");
            verify(providerPaymentRepository).save(paymentCaptor.capture());
            ProviderPayment saved = paymentCaptor.getValue();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw 404 when payment not found")
        void shouldThrow404WhenNotFound() {
            when(providerPaymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> providerPaymentService.updatePayment("invalid-id", requestDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Payment registry not found");

            verify(providerPaymentRepository).findById("invalid-id");
            verify(providerPaymentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllPayments tests")
    class GetAllPaymentsTests {

        @Test
        @DisplayName("Should return all payments")
        void shouldReturnAllPayments() {
            when(providerPaymentRepository.findAll()).thenReturn(List.of(payment1, payment2));

            List<ProviderPaymentResponseDTO> result = providerPaymentService.getAllPayments();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getProviderName()).isEqualTo("Provider One");
            assertThat(result.get(1).getProviderName()).isEqualTo("Provider Two");
        }

        @Test
        @DisplayName("Should return empty list when no payments")
        void shouldReturnEmptyList() {
            when(providerPaymentRepository.findAll()).thenReturn(List.of());

            List<ProviderPaymentResponseDTO> result = providerPaymentService.getAllPayments();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllActivePayments tests")
    class GetAllActivePaymentsTests {

        @Test
        @DisplayName("Should return only active payments")
        void shouldReturnOnlyActivePayments() {
            ProviderPayment inactivePayment = new ProviderPayment();
            inactivePayment.setId("pay-3");
            inactivePayment.setActive(false);

            when(providerPaymentRepository.findByActiveTrue()).thenReturn(List.of(payment1, payment2));

            List<ProviderPaymentResponseDTO> result = providerPaymentService.getAllActivePayments();

            assertThat(result).hasSize(2);
            verify(providerPaymentRepository).findByActiveTrue();
        }
    }

    @Nested
    @DisplayName("getPaymentsByProvider tests")
    class GetPaymentsByProviderTests {

        @Test
        @DisplayName("Should return payments for a specific provider")
        void shouldReturnPaymentsByProvider() {
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-1")).thenReturn(List.of(payment1));

            List<ProviderPaymentResponseDTO> result = providerPaymentService.getPaymentsByProvider("prov-1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProviderId()).isEqualTo("prov-1");
            assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should return empty list when provider has no payments")
        void shouldReturnEmptyForNoPayments() {
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-999")).thenReturn(List.of());

            List<ProviderPaymentResponseDTO> result = providerPaymentService.getPaymentsByProvider("prov-999");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deletePayment tests")
    class DeletePaymentTests {

        @Test
        @DisplayName("Should deactivate payment successfully")
        void shouldDeactivatePayment() {
            when(providerPaymentRepository.findById("pay-1")).thenReturn(Optional.of(payment1));
            when(providerPaymentRepository.save(any(ProviderPayment.class))).thenReturn(payment1);

            Boolean result = providerPaymentService.deletePayment("pay-1");

            assertThat(result).isTrue();
            assertThat(payment1.getActive()).isFalse();
            assertThat(payment1.getUpdatedAt()).isNotNull();
            verify(providerPaymentRepository).findById("pay-1");
            verify(providerPaymentRepository).save(payment1);
        }

        @Test
        @DisplayName("Should return false when payment not found")
        void shouldReturnFalseWhenNotFound() {
            when(providerPaymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

            Boolean result = providerPaymentService.deletePayment("invalid-id");

            assertThat(result).isFalse();
            verify(providerPaymentRepository).findById("invalid-id");
            verify(providerPaymentRepository, never()).save(any());
        }
    }
}
