package io.github.future0923.ai.agent.example.web.search.controller;

import io.github.future0923.ai.agent.example.web.search.search.WebSearchService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author future0923
 */
@RestController
@RequestMapping("/web/search")
public class WebSearchController {

    private final WebSearchService webSearchService;

    public WebSearchController(WebSearchService webSearchService) {
        this.webSearchService = webSearchService;
    }

    @GetMapping
    public Flux<String> search(@RequestParam("query") String query,
                               @RequestParam(value = "chatId", defaultValue = "ai") String chatId,
                               HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        if (!StringUtils.hasText(query)) {
            return Flux.just("输入非法");
        }
        return webSearchService.chat(query, chatId);
    }
}
