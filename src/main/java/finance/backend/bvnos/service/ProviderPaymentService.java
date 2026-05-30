package finance.backend.bvnos.service;

import finance.backend.bvnos.model.ProviderPaymentRequestDTO;
import finance.backend.bvnos.model.ProviderPaymentResponseDTO;

import java.util.List;

public interface ProviderPaymentService {

    ProviderPaymentResponseDTO savePayment(ProviderPaymentRequestDTO requestDTO);

    ProviderPaymentResponseDTO updatePayment(String id, ProviderPaymentRequestDTO requestDTO);

    List<ProviderPaymentResponseDTO> getAllPayments();

    List<ProviderPaymentResponseDTO> getAllActivePayments();

    List<ProviderPaymentResponseDTO> getPaymentsByProvider(String providerId);

    Boolean deletePayment(String id);
}
