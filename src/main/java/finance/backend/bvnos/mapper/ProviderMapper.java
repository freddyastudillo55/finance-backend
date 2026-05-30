package finance.backend.bvnos.mapper;

import finance.backend.bvnos.model.Provider;
import finance.backend.bvnos.model.ProviderRequestDTO;
import finance.backend.bvnos.model.ProviderResponseDTO;

public class ProviderMapper {

    public static Provider toEntity(ProviderRequestDTO dto) {
        Provider provider = new Provider();
        provider.setName(dto.getName());
        provider.setActive(dto.getActive());
        return provider;
    }

    public static ProviderResponseDTO toResponse(Provider provider) {
        ProviderResponseDTO dto = new ProviderResponseDTO();
        dto.setId(provider.getId());
        dto.setName(provider.getName());
        dto.setActive(provider.getActive());
        return dto;
    }
}
