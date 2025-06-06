package com.example.fileupload.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class BoardForm{

	private Integer bno; // 게시글 번호
	private String title;
	private String pw;
	private List<MultipartFile> fileList; // spring에서  input type = "file"
}
