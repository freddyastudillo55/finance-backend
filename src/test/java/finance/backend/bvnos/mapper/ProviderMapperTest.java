package finance.backend.bvnos.mapper;

import finance.backend.bvnos.model.Provider;
import finance.backend.bvnos.model.ProviderRequestDTO;
import finance.backend.bvnos.model.ProviderResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderMapperTest {

    @Test
    @DisplayName("Should map ProviderRequestDTO to Provider entity")
    void shouldMapToEntity() {
        ProviderRequestDTO dto = new ProviderRequestDTO();
        dto.setName("Test Provider");
        dto.setActive(true);

        Provider entity = ProviderMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Test Provider");
        assertThat(entity.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should map Provider entity to ProviderResponseDTO")
    void shouldMapToResponse() {
        Provider entity = new Provider();
        entity.setId("prov-123");
        entity.setName("Test Provider");
        entity.setActive(true);

        ProviderResponseDTO dto = ProviderMapper.toResponse(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("prov-123");
        assertThat(dto.getName()).isEqualTo("Test Provider");
        assertThat(dto.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should map null active to null in entity")
    void shouldMapNullActive() {
        ProviderRequestDTO dto = new ProviderRequestDTO();
        dto.setName("Test");
        dto.setActive(null);

        Provider entity = ProviderMapper.toEntity(dto);

        assertThat(entity.getActive()).isNull();
    }
}
