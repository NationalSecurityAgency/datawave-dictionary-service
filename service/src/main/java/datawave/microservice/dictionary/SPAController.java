package datawave.microservice.dictionary;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.codahale.metrics.annotation.Timed;

@Controller
public class SPAController {
    @GetMapping("/data/v2")
    @Timed(name = "dw.dictionary.data.get", absolute = true)
    public String v2PathDictionary() {
        return "index.html";
    }
}
