package com.sravan.moneymanager.Controllers;


import com.sravan.moneymanager.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    public final DashboardService dashBoardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(){
        Map<String, Object> dashboardData = dashBoardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

}
