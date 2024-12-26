package cloud.loify.packages.home;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/home")
public class HomeController {

    @GetMapping
    public String home() {
        return "Welcome to the Loify API! Visit the docs for more information :)";
    }
}
