package kr.co.api.flobankapi.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SurveyAdminController {

    @GetMapping("/ai-product")
    public String aiProductSurvey(Model model) {
        model.addAttribute("menu", "ai-product");
        return "admin/ai_product";
    }
}
