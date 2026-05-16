package io.lpsoft.features.pushnotif;

import io.lpsoft.features.pushnotif.dto.PushNotifDtos.PushResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/push-notif")
@RequiredArgsConstructor
public class PushNotifController {

    private final PushRegistry registry;

    @GetMapping("enviados")
    public List<PushResponse> enviados() {
        return registry.enviados().stream().map(PushResponse::de).toList();
    }
}
