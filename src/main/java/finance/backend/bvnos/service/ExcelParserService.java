package finance.backend.bvnos.service;

import finance.backend.bvnos.model.PaymentRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ExcelParserService {

    List<PaymentRecord> parseVesta(MultipartFile file) throws IOException;

    List<PaymentRecord> parseAax(MultipartFile file) throws IOException;
}
