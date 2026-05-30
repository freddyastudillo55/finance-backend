package finance.backend.bvnos.service;

import finance.backend.bvnos.mapper.ProviderMapper;
import finance.backend.bvnos.model.*;
import finance.backend.bvnos.repository.ProviderPaymentRepository;
import finance.backend.bvnos.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService{

    private final ProviderRepository providerRepository;
    private final ProviderPaymentRepository providerPaymentRepository;

    @Override
    public ProviderResponseDTO saveProvider(ProviderRequestDTO providerRequestDTO){
        log.info("Saving provider with name {}", providerRequestDTO.getName());
        providerRequestDTO.setActive(true);
        Provider provider = ProviderMapper.toEntity(providerRequestDTO);
        Provider saved = providerRepository.save(provider);
        return ProviderMapper.toResponse(saved);
    }

    @Override
    public List<ProviderResponseDTO> getAllProviders() {
        return providerRepository.findAll()
                .stream()
                .map(ProviderMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProviderResponseDTO> getAllActiveProviders() {
        return providerRepository.findByActiveTrue()
                .stream()
                .map(ProviderMapper::toResponse)
                .toList();
    }

    @Override
    public Boolean deleteProvider(String id) {

        log.info("Deleting provider with id: {}", id);

        return providerRepository.findById(id)
                .map(provider -> {
                    provider.setActive(false);
                    providerRepository.save(provider);
                    return true;
                })
                .orElse(false);
    }


    @Override
    public List<ProviderDashboardResponseDTO> getProvidersForDashboard() {
        return providerRepository.findAll()
                .stream()
                .map(this::convertToDashboardDTO)
                .toList();
    }

    @Override
    public List<ProviderDashboardResponseDTO> getActiveProvidersForDashboard() {
        return providerRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDashboardDTO)
                .toList();
    }

    private ProviderDashboardResponseDTO convertToDashboardDTO(Provider provider) {
        ProviderDashboardResponseDTO dto = new ProviderDashboardResponseDTO();
        dto.setId(provider.getId());
        dto.setName(provider.getName());
        dto.setActive(provider.getActive());

        List<ProviderPayment> payments = providerPaymentRepository.findByProviderIdAndActiveTrue(provider.getId());
        BigDecimal total = payments.stream()
                .map(ProviderPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalInPayments(total);
        return dto;
    }

    @Override
    public ProviderResponseDTO updateProvider(String id, ProviderRequestDTO providerRequestDTO) {
        log.info("Updating provider name for id: {}", id);

        Provider existingProvider = providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        List<ProviderPayment> associatedPayments = providerPaymentRepository.findByProviderIdAndActiveTrue(id);
        if (!associatedPayments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update provider name because it has associated payments");
        }

        existingProvider.setName(providerRequestDTO.getName());
        Provider saved = providerRepository.save(existingProvider);
        return ProviderMapper.toResponse(saved);
    }

}
