package edu.study.controller;

import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.study.service.MemberService;
import edu.study.service.sha256;
import edu.study.vo.MemberVo;


@RequestMapping(value="/find")
@Controller
public class FindController {
	
	@Autowired
	private MemberService memberService;
	
	@Autowired 
	private JavaMailSender mailSender;
	
	
	
	@RequestMapping(value="/find_id.do", method=RequestMethod.GET) 
	public String find_id() {

		return "findPwd/find_id";
	}
	
	
	@ResponseBody
	@RequestMapping(value="/findingID.do") 
	public String findingID( String name, String email) {
		System.out.println(name);
		System.out.println(email);
		int result = memberService.nameCheck(name);
		int em = memberService.emailCheck(email);
		String value = "{ \"name\": \""+result+"\"  , \"email\":\""+em+"\"}";
		
		return value;
	
	}

	
	@RequestMapping(value="/find_id.do", method=RequestMethod.POST)
	public String find_id(MemberVo vo,  Model model) {
		
		MemberVo result = memberService.find_id(vo);
		System.out.println("vo:"+vo);
		
		if(result != null) {
			model.addAttribute("id", result.getId());
			model.addAttribute("name", vo.getName());
			System.out.println("name:"+vo.getName());
		} else {
			model.addAttribute("msg", false);
		}
	
		return "findPwd/find_id_result";
	}	
		
	
	
	@RequestMapping(value="/findPwd-fir", method=RequestMethod.GET)
	public String findPwd1(){
		
		return "findPwd/findPwd-fir";
		
	}
	@RequestMapping(value = "/findPwd-fir.do", method=RequestMethod.POST)
	public ModelAndView pw_auth(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model){
		String email = (String)request.getParameter("email");
		String name = (String)request.getParameter("name");
		String id = (String)request.getParameter("id");
				
		MemberVo vo = memberService.selectMember(email);
				
		if(vo != null) {
		Random r = new Random();
		int num = r.nextInt(999999); 
		
		if (vo.getName().equals(name)&vo.getId().equals(id)) {
			session.setAttribute("email", vo.getEmail());
			session.setAttribute("id", vo.getId());
			session.setAttribute("name", vo.getName());
			String setfrom = "ksj56762@gmail.com";  
			String tomail = vo.getEmail(); 
			String title = "(혼밥의고수)비밀번호변경 인증 이메일 입니다"; 
			String content = System.getProperty("line.separator") + "안녕하세요 회원님 혼밥의 고수입니다." + System.getProperty("line.separator")
					+ "회원님의 비밀번호찾기(변경) 인증번호는 " + num + " 입니다" + System.getProperty("line.separator"); // 

			try {
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "utf-8");

				messageHelper.setFrom(setfrom); 
				messageHelper.setTo(tomail); 
				messageHelper.setSubject(title);
				messageHelper.setText(content); 

				mailSender.send(message);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			ModelAndView mv = new ModelAndView();
			mv.setViewName("findPwd/findPwd-sec");
			mv.addObject("num", num);			
			return mv;
		}else {
			ModelAndView mv = new ModelAndView();
			mv.setViewName("findPwd/findPwd-fir");
			mv.addObject("name", name);
			mv.addObject("id", id);	
			return mv;
		}
		}else {
			ModelAndView mv = new ModelAndView();
				mv.setViewName("findPwd/findPwd-fir");
				mv.addObject("name", name);
				mv.addObject("id", id);	
				return mv;
		}
	}
	
	@RequestMapping(value = "/findPwd-sec.do", method = RequestMethod.POST)
	public String pw_set(@RequestParam(value="confirmNum") String confirmNum,
				@RequestParam(value = "num") String num) {
			
			if(confirmNum.equals(num)) {
				return "findPwd/findPwd-thd";
			}
			else {
				return "findPwd/findPwd-sec";
			}
	} 
	
	@RequestMapping(value = "/findPwd-thd.do", method = RequestMethod.POST)
	public String pw_new(MemberVo vo, HttpSession session){
		
		String inputPwd = vo.getPwd();
		String pwd = sha256.encrypt(inputPwd); //암호화
		
		vo.setPwd(pwd); //vo에 저장
		
		int result = memberService.updatePw(vo);
		
		if(result == 1) {
			
			return "redirect:/";
		}
		else {
			System.out.println("pw_update"+ result);
			return "findPwd/findPwd-thd";
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/check.do")
	public String Check(String name, String id, String email) {
		System.out.println(name);
		System.out.println(id);
		int result = memberService.nameCheck(name);
		int data = memberService.idCheck(id);
		int em = memberService.emailCheck(email);
		
		
		String value = "{ \"name\": \""+result+"\" , \"id\":\""+data+"\" , \"email\":\""+em+"\"}";
		
		return value;
	}
	

		
}
