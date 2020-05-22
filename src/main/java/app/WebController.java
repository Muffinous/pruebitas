package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jcraft.jroar.JRoar;

import java.io.IOException;

import javax.annotation.PostConstruct;

@Controller
public class WebController {
	
	@Autowired
	WebService webservice;
	
	Integer port = 9000;
	JRoar jroar;
	
	@PostConstruct
	private void init() {
		jroar=new JRoar();
	    String port = "9000";
        String testing = "/testing.ogg";
        String myplaylist = "C:/Users/user/Desktop/EAS/20E13/src/main/resources/static/songs/foo";
        String[] argumentos= {"-port", port, "-playlist", testing, myplaylist,"-passwd","hello" };
        jroar.main(argumentos);
	}

    @GetMapping("/")
    public String main() {
        return "index";     
    }

    @GetMapping("/index")
    public String auxMain() {
        return "index";
    }

    @GetMapping("/mountpage")
    public String mount() {
        return "mount";
    }

    @GetMapping("/droppage")
    public String drop() {
        return "drop";
    }

    @GetMapping("/shoutpage")
    public String shout() {
        return "shout";
    }

    @GetMapping("/udppage")
    public String udp() {
        return "UDP";
    }

    
    @PostMapping("/mount")
    public String Mount(Model model, @RequestParam String mountPoint , String source, String selectMount, String limit, String password) {
    	port++;
    	String[] values= {"-port", port.toString(), "-playlist", mountPoint, source,"-passwd",password};
    	jroar.main(values);
    	return "index";
    }
    
    @PostMapping("/drop")
    public String Drop(Model model, @RequestParam String selectDrop, String password) {
        return "index";
    }

    @PostMapping("/shout")
    public String Shout(Model model, @RequestParam String selectShout, String ice, String icepass, String password) {
    	return "index";
    }
    
    @PostMapping("/udp")
    public String profileChange(Model model, @RequestParam String port, String address_b, String mountpoint, String password) {
        return "index";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
