package finance.backend.bvnos.service;

import finance.backend.bvnos.mapper.ProviderMapper;
import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.ProviderPaymentRepository;
import finance.backend.bvnos.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceImplTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderPaymentRepository providerPaymentRepository;

    @InjectMocks
    private ProviderServiceImpl providerService;

    private ProviderRequestDTO providerRequestDTO;
    private Provider provider1;
    private Provider provider2;
    private ProviderPayment payment1;
    private ProviderPayment payment2;

    @BeforeEach
    void setUp() {
        providerRequestDTO = new ProviderRequestDTO();
        providerRequestDTO.setName("Test Provider");
        providerRequestDTO.setActive(true);

        provider1 = new Provider();
        provider1.setId("prov-1");
        provider1.setName("Provider One");
        provider1.setActive(true);

        provider2 = new Provider();
        provider2.setId("prov-2");
        provider2.setName("Provider Two");
        provider2.setActive(false);

        payment1 = new ProviderPayment();
        payment1.setId("pay-1");
        payment1.setProviderId("prov-1");
        payment1.setProviderName("Provider One");
        payment1.setAmount(new BigDecimal("1000.00"));
        payment1.setPaymentDate(LocalDate.of(2025, 1, 15));
        payment1.setStatus("PAID");
        payment1.setActive(true);

        payment2 = new ProviderPayment();
        payment2.setId("pay-2");
        payment2.setProviderId("prov-1");
        payment2.setProviderName("Provider One");
        payment2.setAmount(new BigDecimal("2000.00"));
        payment2.setPaymentDate(LocalDate.of(2025, 2, 15));
        payment2.setStatus("PAID");
        payment2.setActive(true);
    }

    @Nested
    @DisplayName("saveProvider tests")
    class SaveProviderTests {

        @Test
        @DisplayName("Should save provider successfully")
        void shouldSaveProviderSuccessfully() {
            Provider savedProvider = new Provider();
            savedProvider.setId("new-prov-id");
            savedProvider.setName("Test Provider");
            savedProvider.setActive(true);

            when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);

            ProviderResponseDTO result = providerService.saveProvider(providerRequestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-prov-id");
            assertThat(result.getName()).isEqualTo("Test Provider");
            assertThat(result.getActive()).isTrue();

            verify(providerRepository).save(any(Provider.class));
        }

        @Test
        @DisplayName("Should set active to true even if DTO has active as null")
        void shouldSetActiveToTrueByDefault() {
            providerRequestDTO.setActive(null);

            Provider savedProvider = new Provider();
            savedProvider.setId("new-prov-id");
            savedProvider.setName("Test Provider");
            savedProvider.setActive(true);

            when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);

            ProviderResponseDTO result = providerService.saveProvider(providerRequestDTO);

            assertThat(result.getActive()).isTrue();
            verify(providerRepository).save(argThat(p -> p.getActive() == true || p.getActive() == null));
        }
    }

    @Nested
    @DisplayName("getAllProviders tests")
    class GetAllProvidersTests {

        @Test
        @DisplayName("Should return all providers")
        void shouldReturnAllProviders() {
            when(providerRepository.findAll()).thenReturn(List.of(provider1, provider2));

            List<ProviderResponseDTO> result = providerService.getAllProviders();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Provider One");
            assertThat(result.get(1).getName()).isEqualTo("Provider Two");
            verify(providerRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no providers")
        void shouldReturnEmptyList() {
            when(providerRepository.findAll()).thenReturn(List.of());

            List<ProviderResponseDTO> result = providerService.getAllProviders();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllActiveProviders tests")
    class GetAllActiveProvidersTests {

        @Test
        @DisplayName("Should return only active providers")
        void shouldReturnOnlyActiveProviders() {
            when(providerRepository.findByActiveTrue()).thenReturn(List.of(provider1));

            List<ProviderResponseDTO> result = providerService.getAllActiveProviders();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Provider One");
            assertThat(result.get(0).getActive()).isTrue();
            verify(providerRepository).findByActiveTrue();
        }
    }

    @Nested
    @DisplayName("deleteProvider tests")
    class DeleteProviderTests {

        @Test
        @DisplayName("Should deactivate provider successfully")
        void shouldDeactivateProviderSuccessfully() {
            when(providerRepository.findById("prov-1")).thenReturn(Optional.of(provider1));
            when(providerRepository.save(any(Provider.class))).thenReturn(provider1);

            Boolean result = providerService.deleteProvider("prov-1");

            assertThat(result).isTrue();
            assertThat(provider1.getActive()).isFalse();
            verify(providerRepository).findById("prov-1");
            verify(providerRepository).save(provider1);
        }

        @Test
        @DisplayName("Should return false when provider not found")
        void shouldReturnFalseWhenNotFound() {
            when(providerRepository.findById("invalid-id")).thenReturn(Optional.empty());

            Boolean result = providerService.deleteProvider("invalid-id");

            assertThat(result).isFalse();
            verify(providerRepository).findById("invalid-id");
            verify(providerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getProvidersForDashboard tests")
    class GetProvidersForDashboardTests {

        @Test
        @DisplayName("Should return all providers with payment totals")
        void shouldReturnProvidersWithPaymentTotals() {
            when(providerRepository.findAll()).thenReturn(List.of(provider1, provider2));
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-1"))
                    .thenReturn(List.of(payment1, payment2));
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-2"))
                    .thenReturn(List.of());

            List<ProviderDashboardResponseDTO> result = providerService.getProvidersForDashboard();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Provider One");
            assertThat(result.get(0).getTotalInPayments()).isEqualByComparingTo(new BigDecimal("3000.00"));
            assertThat(result.get(1).getName()).isEqualTo("Provider Two");
            assertThat(result.get(1).getTotalInPayments()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getActiveProvidersForDashboard tests")
    class GetActiveProvidersForDashboardTests {

        @Test
        @DisplayName("Should return only active providers with payment totals")
        void shouldReturnActiveProvidersWithPaymentTotals() {
            when(providerRepository.findByActiveTrue()).thenReturn(List.of(provider1));
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-1"))
                    .thenReturn(List.of(payment1));

            List<ProviderDashboardResponseDTO> result = providerService.getActiveProvidersForDashboard();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Provider One");
            assertThat(result.get(0).getTotalInPayments()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
    }

    @Nested
    @DisplayName("updateProvider tests")
    class UpdateProviderTests {

        @Test
        @DisplayName("Should update provider name successfully")
        void shouldUpdateProviderSuccessfully() {
            ProviderRequestDTO updateDTO = new ProviderRequestDTO();
            updateDTO.setName("Updated Name");
            updateDTO.setActive(true);

            when(providerRepository.findById("prov-1")).thenReturn(Optional.of(provider1));
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-1")).thenReturn(List.of());
            when(providerRepository.save(any(Provider.class))).thenAnswer(inv -> inv.getArgument(0));

            ProviderResponseDTO result = providerService.updateProvider("prov-1", updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(providerRepository).findById("prov-1");
            verify(providerPaymentRepository).findByProviderIdAndActiveTrue("prov-1");
            verify(providerRepository).save(provider1);
        }

        @Test
        @DisplayName("Should throw 404 when provider not found")
        void shouldThrow404WhenNotFound() {
            when(providerRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> providerService.updateProvider("invalid-id", providerRequestDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Provider not found");

            verify(providerRepository).findById("invalid-id");
            verify(providerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw 400 when provider has associated payments")
        void shouldThrow400WhenHasPayments() {
            when(providerRepository.findById("prov-1")).thenReturn(Optional.of(provider1));
            when(providerPaymentRepository.findByProviderIdAndActiveTrue("prov-1")).thenReturn(List.of(payment1));

            assertThatThrownBy(() -> providerService.updateProvider("prov-1", providerRequestDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("associated payments");

            verify(providerRepository).findById("prov-1");
            verify(providerPaymentRepository).findByProviderIdAndActiveTrue("prov-1");
            verify(providerRepository, never()).save(any());
        }
    }
}
