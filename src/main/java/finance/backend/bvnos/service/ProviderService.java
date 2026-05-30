package finance.backend.bvnos.service;

import finance.backend.bvnos.model.ProviderDashboardResponseDTO;
import finance.backend.bvnos.model.ProviderRequestDTO;
import finance.backend.bvnos.model.ProviderResponseDTO;

import java.util.List;

public interface ProviderService {

    ProviderResponseDTO saveProvider(ProviderRequestDTO provider);

    ProviderResponseDTO updateProvider(String id, ProviderRequestDTO provider);

    List<ProviderResponseDTO> getAllProviders();

    List<ProviderResponseDTO> getAllActiveProviders();

    Boolean deleteProvider(String id);

    List<ProviderDashboardResponseDTO> getProvidersForDashboard();

    List<ProviderDashboardResponseDTO> getActiveProvidersForDashboard();
}
