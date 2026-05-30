package finance.backend.bvnos.service;

import finance.backend.bvnos.mapper.PaymentProviderMapper;
import finance.backend.bvnos.model.ProviderPayment;
import finance.backend.bvnos.model.ProviderPaymentRequestDTO;
import finance.backend.bvnos.model.ProviderPaymentResponseDTO;
import finance.backend.bvnos.repository.ProviderPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProviderPaymentServiceImpl implements ProviderPaymentService{

    private final ProviderPaymentRepository providerPaymentRepository;

    @Override
    public ProviderPaymentResponseDTO savePayment(ProviderPaymentRequestDTO requestDTO) {

        log.info("Saving payment for provider {}", requestDTO.getProviderName());

        ProviderPayment payment = PaymentProviderMapper.toEntity(requestDTO);

        ProviderPayment saved = providerPaymentRepository.save(payment);

        return PaymentProviderMapper.toResponse(saved);
    }

    @Override
    public ProviderPaymentResponseDTO updatePayment(String id, ProviderPaymentRequestDTO requestDTO) {
        log.info("Updating payment data for id: {}", id);
        ProviderPayment existingPayment = providerPaymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment registry not found"));

        existingPayment.setAmount(requestDTO.getAmount());
        existingPayment.setPaymentDate(requestDTO.getPaymentDate());
        existingPayment.setDescription(requestDTO.getDescription());
        existingPayment.setStatus(requestDTO.getStatus());
        existingPayment.setUpdatedAt(LocalDateTime.now());

        ProviderPayment saved = providerPaymentRepository.save(existingPayment);
        return PaymentProviderMapper.toResponse(saved);
    }

    @Override
    public List<ProviderPaymentResponseDTO> getAllPayments() {
        return providerPaymentRepository.findAll()
                .stream()
                .map(PaymentProviderMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProviderPaymentResponseDTO> getAllActivePayments() {
        return providerPaymentRepository.findByActiveTrue()
                .stream()
                .map(PaymentProviderMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProviderPaymentResponseDTO> getPaymentsByProvider(String providerId) {
        return providerPaymentRepository
                .findByProviderIdAndActiveTrue(providerId)
                .stream()
                .map(PaymentProviderMapper::toResponse)
                .toList();
    }

    @Override
    public Boolean deletePayment(String id) {

        log.info("Deleting payment with id {}", id);

        return providerPaymentRepository.findById(id)
                .map(payment -> {

                    payment.setActive(false);
                    payment.setUpdatedAt(LocalDateTime.now());

                    providerPaymentRepository.save(payment);

                    return true;
                })
                .orElse(false);
    }
}
