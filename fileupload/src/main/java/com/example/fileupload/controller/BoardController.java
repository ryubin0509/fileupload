package com.example.fileupload.controller;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {

	@Autowired BoardRepository boardRepository;
	@Autowired BoardfileRepository boardfileRepository;
	
	@GetMapping("/boardOne")
	public String boardOne(Model model ,@RequestParam(value = "bno") int bno) {
		BoardMapping boardMapping = boardRepository.findByBno(bno);
		
		List<Boardfile> fileList = boardfileRepository.findByBno(bno);
		log.debug("size:"+fileList.size());
		
		
		model.addAttribute("boardMapping", boardMapping);
		model.addAttribute("fileList", fileList);

		return "boardOne";
	}
	
	
	@GetMapping("/boardDelete")
	public String deleteBoard(@RequestParam(value ="bno")int bno ,Model model, RedirectAttributes rda) {
		rda.addAttribute("bno", bno);
		
		if(boardfileRepository.existsByBno(bno)) {
			rda.addFlashAttribute("msg", "파일이 있습니다.");	
			
			return "redirect:/boardOne?bno=" + bno;
		} else { // 파일이 없을 경우
			model.addAttribute("bno", bno);
		    return "delete";
		}	
	}
	
	@PostMapping("/boardDelete")
	public String deleteBoardAction(BoardForm boardForm,RedirectAttributes rda) {
		Board board = boardRepository.findById(boardForm.getBno()).orElse(null);
		if (!board.getPw().equals(boardForm.getPw())) {
		    rda.addFlashAttribute("msg", "비밀번호가 틀립니다");
		    rda.addAttribute("bno", boardForm.getBno());
		    return "redirect:boardOne";
		 } else {
			 boardRepository.deleteById(boardForm.getBno());
			 return"redirect:/boardList";
		 }
	} 
	
	
	@GetMapping("/boardModify")
	public String modifyOne(Model model , @RequestParam(value = "bno")int bno) {
		Board board = boardRepository.findById(bno).orElse(null);
		model.addAttribute("board", board);
		
		
		return "boardModify";
	}

	@PostMapping("boardModify")
	public String updateboard(BoardForm boardForm, RedirectAttributes rds ) {
		Board board = boardRepository.findById(boardForm.getBno()).orElse(null);
		if (!board.getPw().equals(boardForm.getPw())) {
		    rds.addFlashAttribute("msg", "비밀번호가 틀립니다");
		    rds.addAttribute("bno", boardForm.getBno());
		    return "redirect:/boardModify";
		} else {
		    board.setTitle(boardForm.getTitle());
		    boardRepository.save(board);  
		    rds.addAttribute("bno", board.getBno());
		    return "redirect:/boardOne";
		}	
		
	} 
	
	
	
	@GetMapping("/boardList")
	public String boardList(Model model ,
							@RequestParam(value="currentPage",defaultValue = "0")int currentPage
						   ,@RequestParam(value="rowPerPage",defaultValue = "5") int rowPerPage
						   ,@RequestParam(value="word",defaultValue = "")String word){
		
		
		Sort sort = Sort.by(Sort.Direction.ASC,"bno")
				.and(Sort.by(Sort.Direction.DESC,"title"));
		
		PageRequest pageable = PageRequest.of(currentPage, rowPerPage , sort);
		Page<BoardMapping> list = boardRepository.findByTitleContaining(word, pageable);
		
		model.addAttribute("list", list);
		model.addAttribute("word", word);
		model.addAttribute("nextPage",currentPage+1);
		model.addAttribute("prevPage",currentPage-1);
	    model.addAttribute("currentPage", currentPage);
		
		return "boardList"; 
	}
	
	
	// 입력폼 
	@GetMapping("/addBoard")
	public String addBoard() {
		return "/addBoard";
	}
	
	// 입력액션
	@PostMapping("/addBoard")
	public String addBoard(BoardForm boardForm) {
		log.debug(boardForm.toString());
		// 파일을 첨부하지 않아도 fileSize는 1이다.
		log.debug("MultipartFile Size: " + boardForm.getFileList().size());
		
		Board board = new Board();
		board.setTitle(boardForm.getTitle());
		board.setPw(boardForm.getPw());
		boardRepository.save(board);   // board 저장
		
		
		// 파일 분리
		List<MultipartFile> fileList = boardForm.getFileList();
		long firstFileSize = fileList.get(0).getSize();
		log.debug("firstFileSize:"+ firstFileSize); 
		
		// 이슈: 파일을 첨부하지 않아도 fileSize 는 1이다.
		if(firstFileSize > 0) { // 첫번째 파일 사이즈가 0이상이다 -> 첨부된 파일이 있다.
			for(MultipartFile f :fileList) {
				log.debug("파일타입:" + f.getContentType());
				log.debug("파일이름:" + f.getName());
				log.debug("원본이름:" + f.getOriginalFilename());
				log.debug("파일용량:" + f.getSize());
				// 확장자 추출
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".")+1);
				log.debug("확장자: "+ext);
				String saveName = UUID.randomUUID().toString().replace("-", "")+'.'+ext;
				log.debug("저장파일이름: "+saveName);
				
				File emptyFile = new File("c:/project/upload/"+saveName);
				//  f의 byte -> emptyFile 복사
				
				try {
					f.transferTo(emptyFile);
				} catch (Exception e) {
					log.error("파일저장실페!");
					e.printStackTrace();
				}
				
				// boardfile 테이블에도 파일정보 저장
				Boardfile boardfile = new Boardfile();
				boardfile.setBno(board.getBno());
				boardfile.setFext(ext);
				boardfile.setFname(saveName);
				boardfile.setForiginname(f.getOriginalFilename());
				boardfile.setFsize(f.getSize());
				boardfile.setFtype(f.getContentType());
				boardfileRepository.save(boardfile);
			}
			
		}
		
		return "redirect:/boardList";
	}
}
