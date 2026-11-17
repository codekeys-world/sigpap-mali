package ml.apes.rh.dashboard;

import lombok.RequiredArgsConstructor;
import ml.apes.rh.security.Acces;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    @PreAuthorize(Acces.TOUS)
    public Dtos.Dashboard get() {
        return service.construire();
    }
}
