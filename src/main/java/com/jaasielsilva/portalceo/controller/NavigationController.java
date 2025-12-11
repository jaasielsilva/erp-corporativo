package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.service.navigation.MenuIntentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/navigation")
public class NavigationController {
    @Autowired private MenuIntentService menuIntentService;

    @GetMapping("/route")
    public ResponseEntity<Map<String,Object>> routeGet(@RequestParam String query) {
        if (query == null || query.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(menuIntentService.route(query));
    }

    @PostMapping("/route")
    public ResponseEntity<Map<String,Object>> routePost(@RequestBody Query q) {
        if (q == null || q.query == null || q.query.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(menuIntentService.route(q.query));
    }

    public static class Query { public String query; }
}

