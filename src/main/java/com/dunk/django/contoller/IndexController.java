package com.dunk.django.contoller;

import com.dunk.django.domain.Recipe;
import com.dunk.django.dto.GenericListDTO;
import com.dunk.django.dto.PageDTO;
import com.dunk.django.dto.RecipeDTO;
import com.dunk.django.service.MemberService;
import com.dunk.django.service.PreferencesService;
import com.dunk.django.service.RecipeService;
import com.dunk.django.service.UserFridgeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequiredArgsConstructor
@RequestMapping("/django")
@Log4j2
public class IndexController {
    
    private final RecipeService service;  
    private final PreferencesService preferencesService;
    private final MemberService memberService;
    //추가
    private final UserFridgeService fridgeService;

    @GetMapping("/index")
    public void index(@ModelAttribute("pageDTO")PageDTO pageDTO, Model model) {
        log.info("===============================INDEX==================================");
        GenericListDTO<RecipeDTO, Recipe> result =
        service.getList(pageDTO);

        result.getDtoList().forEach(dto -> log.info(dto));

        model.addAttribute("list", result);
    }

    @GetMapping("/scan")
    public void scan() {
        log.info("===============================SCAN==================================");
    }

    @GetMapping("/recipe")
    public void recipeGet(@RequestParam("itemId") Long itemId, Authentication auth,Model model) {
        log.info("=======================get==================");
        log.info("itemId : " + itemId);
        // log.info("id : " + auth.getName());
 
        model.addAttribute("get", service.get(itemId));

        //로그인 중일 때만
        if(auth!=null){
            Long userId = memberService.getMno(auth.getName()).get().getUserId();
    
            log.info(userId);
            //userId와 itemId로 select한다.
    
            preferencesService.getAndRegisterOrModify(userId, itemId);
            
        }
    }
    //------------------추가-------------------
    @GetMapping("/myFridge")
    public void getMyFridge(Authentication auth, Model model) {
        log.info("================get My Fridge==============");
        log.info("auth : "+auth);
        // 로그인 안했을떄 null
        if(auth==null){
            log.info("auth null");
            return;
        }
        // 사용자 한명의 냉장고 내용출력
        log.info("getList : "+fridgeService.get(auth.getName()));
        model.addAttribute("fridgeList", fridgeService.get(auth.getName()));
        model.addAttribute("username", auth.getName());
    }
    
    //냉장고 전체삭제
	@PostMapping("/removeFridge")
	public String removeFridge(Authentication auth, RedirectAttributes rttr) {
		log.info("************************FRIDGE REMOVE*****************************");
		log.info("id : "+auth.getName());
		// auth.getName()이 유저의 아이디.
		fridgeService.removeUsername(auth.getName());
		rttr.addAttribute("username" , auth.getName());
		
		return "redirect:/django/myFridge";
    }

    	// 냉장고 선택삭제하기.
	@DeleteMapping(value="/{fno}",
                  produces = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public ResponseEntity<String> selectRemoveFridge(@PathVariable("fno") Long fno) {
            fridgeService.remove(fno);
        return  new ResponseEntity<String>("success", HttpStatus.OK);
    }

}