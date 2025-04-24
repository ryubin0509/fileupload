package com.example.fileupload.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardfileRepository;

@Controller
public class BoardfileController {
	@Autowired
	BoardfileRepository boardfileRepository; 
	
	
	@GetMapping("/removeBoardfile")
	public String removeBoardfile(@RequestParam(value= "fno") int fno,
								  @RequestParam(value= "bno") int bno) {
		
		// 파일 삭제후 
		Boardfile boardfile = boardfileRepository.findById(fno).orElse(null);
		File f =  new File("c:/project/upload/"+boardfile.getFname()+"."+boardfile.getFext());  // boardFile 데이터 베이스에서 이름을 가져와 삭제를 한다.
		if(f.exists()) {
			f.delete();
		}
	    boardfileRepository.deleteById(fno);
		return "redirect:/boardOne?bno="+bno; 
	}
}
