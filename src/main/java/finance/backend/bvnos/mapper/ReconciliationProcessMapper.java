package finance.backend.bvnos.mapper;

import finance.backend.bvnos.model.FileInformation;
import finance.backend.bvnos.model.ReconciliationProcess;
import finance.backend.bvnos.model.ReconciliationProcessRequestDTO;
import finance.backend.bvnos.model.ReconciliationProcessResponseDTO;

import java.time.LocalDateTime;

public class ReconciliationProcessMapper {

//    public static ReconciliationProcess toEntity(ReconciliationProcessRequestDTO dto) {
//
//        ReconciliationProcess process = new ReconciliationProcess();
//
//        process.setName(dto.getName());
//
//        FileInformation fileA = new FileInformation();
//        fileA.setFileName(dto.getFileA().getOriginalFilename());
//        fileA.setUploadedAt(LocalDateTime.now());
//
//        FileInformation fileB = new FileInformation();
//        fileB.setFileName(dto.getFileB().getOriginalFilename());
//        fileB.setUploadedAt(LocalDateTime.now());
//
//        process.setFileA(fileA);
//        process.setFileB(fileB);
//
//        process.setStatus("PROCESSING");
//
//        process.setCreatedAt(LocalDateTime.now());
//
//        return process;
//    }
//
//    public static ReconciliationProcessResponseDTO toResponse(
//            ReconciliationProcess process
//    ) {
//
//        ReconciliationProcessResponseDTO dto =
//                new ReconciliationProcessResponseDTO();
//
//        dto.setId(process.getId());
//        dto.setName(process.getName());
//
//        dto.setFileA(process.getFileA());
//        dto.setFileB(process.getFileB());
//
//        dto.setStatus(process.getStatus());
//
//        dto.setTotalRecords(process.getTotalRecords());
//        dto.setMatchedRecords(process.getMatchedRecords());
//        dto.setMismatchedRecords(process.getMismatchedRecords());
//
//        dto.setCreatedAt(process.getCreatedAt());
//
//        return dto;
//    }
}
