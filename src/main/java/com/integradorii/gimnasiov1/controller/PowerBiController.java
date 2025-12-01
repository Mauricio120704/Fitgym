package com.integradorii.gimnasiov1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PowerBiController {

    @GetMapping("/admin/powerbi")
    public String verPowerBi(Model model) {
        model.addAttribute("activeMenu", "powerbi");
        return "admin/powerbi";
    }
}
