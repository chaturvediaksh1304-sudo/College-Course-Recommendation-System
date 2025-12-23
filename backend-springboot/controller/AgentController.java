package com.example.coursereco.controller;

import com.example.coursereco.dto.AgentRecommendRequest;
import com.example.coursereco.dto.AgentRecommendation;
import com.example.coursereco.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/recommend")
    public List<AgentRecommendation> recommend(@RequestBody AgentRecommendRequest req) {
        return agentService.agentRecommend(req);
    }
}
