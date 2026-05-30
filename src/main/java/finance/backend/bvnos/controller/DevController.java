package finance.backend.bvnos.controller;

import finance.backend.bvnos.service.DevSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final DevSeedService devSeedService;

    @PostMapping("/seed-sales")
    public String seedSales() {
        devSeedService.seedSalesData();
        return "Seed completed";
    }
}
