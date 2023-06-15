package com.tjoeun.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tjoeun.dao.MyBatisDAO;
import com.tjoeun.service.GetCalendar;
import com.tjoeun.service.GetList;
import com.tjoeun.vo.BoardVO;
import com.tjoeun.vo.DateData;
import com.tjoeun.vo.EmpVO;
import com.tjoeun.vo.MeetRoomVO;
import com.tjoeun.vo.Param;
import com.tjoeun.vo.TodoVO;

@Controller
@RequestMapping("/community")
public class community {

	private static final Logger logger = LoggerFactory.getLogger(community.class);

	AbstractApplicationContext CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
	BoardVO vo = CTX.getBean("boardvo", BoardVO.class);
	Param param = CTX.getBean("param", Param.class);

	private int PageSize = 10;
	private int currentPage = 1;

	@Autowired
	private SqlSession sqlSession;

	@RequestMapping("/move_board_list")
	public String move_board_list(HttpServletRequest request, Model model) {

		HttpSession session = request.getSession();

		session.setAttribute("searchcategory", null);
		session.setAttribute("searchobj", null);

		model.addAttribute("category", request.getParameter("category"));
		model.addAttribute("currentPage", 1);

		return "redirect:board_list";
	}

	// 글 목록
	@RequestMapping("/board_list")
	public String board_list(HttpServletRequest request, Model model) {

		HttpSession session = request.getSession();
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		
		String category = request.getParameter("category");

		String searchcategory = request.getParameter("searchcategory");
		String searchobj = request.getParameter("searchobj");

		try {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
		} catch (Exception e) {

		}

		if (searchobj != null) {
			session.setAttribute("searchcategory", searchcategory);
			searchobj = searchobj.trim().length() == 0 ? "" : searchobj;
			session.setAttribute("searchobj", searchobj);
		} else { // 페이지가 이동되어도 검색 내용 유지
			searchcategory = (String) session.getAttribute("searchcategory");
			searchobj = (String) session.getAttribute("searchobj");
		}

		GetList boardlist = null;

		// 검색 내용 없을 경우에 전체 글 목록 얻어오기
		if (searchobj == null || searchobj.trim().length() == 0) {

			int totalCount = mapper.selectCount(category);

			boardlist = new GetList(PageSize, totalCount, currentPage);

			param.setStartNo(boardlist.getStartNo());
			param.setEndNo(boardlist.getEndNo());
			param.setCategory(category);

			boardlist.setBoardList(mapper.selectList(param));

		} else { // 검색 내용 찾기

			param.setSearchobj(searchobj);
			param.setSearchcategory(searchcategory);
			param.setCategory(category);

			int totalCount = mapper.selecCountMulti(param);

			boardlist = new GetList(PageSize, totalCount, currentPage);

			param.setStartNo(boardlist.getStartNo());
			param.setEndNo(boardlist.getEndNo());

			boardlist.setBoardList(mapper.selectListMulti(param));
		}

		// QNA는 따로 처리
		if (category.equals("QNA")) {
			int totalCount = mapper.selectQNACount(category);

			boardlist = new GetList(PageSize, totalCount, currentPage);

			param.setStartNo(boardlist.getStartNo());
			param.setEndNo(boardlist.getEndNo());
			param.setCategory(category);

			boardlist.setBoardList(mapper.selectQNAList(param));
		}

		model.addAttribute("currentPage", currentPage);
		model.addAttribute("BoardList", boardlist);
		
		if (category.equals("자유 게시판")) {
			return "community/board/freeboard_view";
		} else if (category.equals("공지사항")) {
			return "community/board/noticeboard_view";
		} else if (category.equals("자료실")) {
			return "community/board/databoard_view";
		} else if (category.equals("QNA")) {
			return "community/board/QNAboard_view";
		}

		return "./error";
	}

	// 글 입력 폼 이동
	@RequestMapping("/freeboard_insert")
	public String freeboard_insert(HttpServletRequest request, Model model) {
		return "community/board/freeboard_insert";
	}

	@RequestMapping("/databoard_insert")
	public String databoard_insert(HttpServletRequest request, Model model) {
		return "community/board/databoard_insert";
	}

	@RequestMapping("/noticeboard_insert")
	public String noticeboard_insert(HttpServletRequest request, Model model) {
		return "community/board/noticeboard_insert";
	}

	@RequestMapping("/QNAboard_insert")
	public String QNAboard_insert(HttpServletRequest request, Model model) {
		return "community/board/QNAboard_insert";
	}

	@RequestMapping("/teamboard_insert")
	public String teamboard_insert(HttpServletRequest request, Model model) {
		return "team/teamboard_insert";
	}

	// 글 입력
	@RequestMapping("/board_insert")
	public void board_insert(HttpServletRequest request, HttpServletResponse response, Model model, BoardVO vo) throws IOException {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		if (vo.getTitle() == null || vo.getTitle().trim().length() == 0) {
			out.println("<script>alert('제목을 입력하세요.')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else if (vo.getContent() == null || vo.getContent().trim().length() == 0) {
			out.println("<script>alert('내용을 입력하세요.')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else {
			mapper.boardinsert(vo);
			out.println("<script>alert('글이 작성됐습니다.')</script>");
			out.println("<script>location.href = './move_board_list?category=" + vo.getCategory() + "';</script>");
			out.flush();
		}

	}

	// 첨부파일 올라가는 게시판 글 입력
	@RequestMapping("/data_upload")
	public void data_upload(MultipartHttpServletRequest request, HttpServletResponse response, Model model, BoardVO vo) throws IOException {
		
		String rootUploarDir = "D:" + File.separator + "upload" + File.separator + "board"; // 업로드하는 파일이 저장될 디렉토리
		File dir = new File(rootUploarDir);
		String category = request.getParameter("category");
		UUID uuid = UUID.randomUUID();
		
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		// 업로드 디렉토리가 존재하지 않을 경우 mkdirs() 메소드로 업로드 디렉토리를 만든다.
		if (!dir.exists()) {
			dir.mkdir();
		}

		// 업로드 되는 파일 정보 수집
		Iterator<String> iterator = request.getFileNames();
		MultipartFile multipartFile = null;
		String realfilename = ""; // 실제 업로드 파일명
		String attachedfile = ""; // 원래 파일명

		while (iterator.hasNext()) {
			realfilename = iterator.next(); // 실제 업로드 파일명
			multipartFile = request.getFile(realfilename);
			attachedfile = multipartFile.getOriginalFilename();

			if (attachedfile != null && attachedfile.length() != 0) {
				try {
					// MultipartFile 인터페이스 객체에서 transferTo() 메소드로 파일을 File 객체로 만들어 업로드 한다.
					// C:/Upload/orgFileName, transferTo()로 원하는 위치에 저장
					multipartFile.transferTo(new File(dir + File.separator + uuid.toString() + "_" + attachedfile)); 
					vo.setAttachedfile(attachedfile);
					vo.setRealfilename(uuid.toString() + "_" + attachedfile);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if (vo.getTitle() != null && vo.getTitle().trim().length() != 0 && vo.getContent() != null && vo.getContent().trim().length() != 0) {

			MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
			mapper.insertattach(vo);
			
			model.addAttribute("currentPage", currentPage);
			model.addAttribute("category",category);

			out.println("<script>alert('업로드 완료')</script>");
			out.println("<script>location.href = './move_board_list?category=" + category + "';</script>");
			out.flush();
		
		} else if (vo.getTitle() == null || vo.getTitle().trim().length() == 0) {
			out.println("<script>alert('제목을 입력하세요.')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else if (vo.getContent() == null || vo.getContent().trim().length() == 0) {
			out.println("<script>alert('내용을 입력하세요.')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		}	
	}

	// Q&A 답변 입력
	@RequestMapping("/answer_insert")
	public void answer_insert(HttpServletRequest request, HttpServletResponse response, Model model, BoardVO vo) throws IOException {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		if (vo.getTitle() == null || vo.getTitle().trim().length() == 0) {
			out.println("<script>alert('제목을 입력하세요.')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else if (vo.getContent() == null || vo.getContent().trim().length() == 0) {
			out.println("<script>alert('내용을 입력하세요.')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else {
			mapper.answerinsert(vo);
			out.println("<script>alert('글이 작성됐습니다.')</script>");
			out.println("<script>location.href = './move_board_list?category=" + vo.getCategory() + "';</script>");
			out.flush();
		}
	}

	// 글 보기
	@RequestMapping("/content_list")
	public String content_list(HttpServletRequest request, HttpServletResponse response, Model model,
			@CookieValue(value = "coki") String coki) throws UnsupportedEncodingException {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		HttpSession session = request.getSession();
	      
		int empno = ((EmpVO) session.getAttribute("EmpVO")).getEmpno();

		int idx = Integer.parseInt(request.getParameter("idx"));
		try {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
		} catch (Exception e) {

		}
		String category = request.getParameter("category");

		// =============쿠키 ==============
		if (!coki.contains("_idx" + String.valueOf(idx) + "_empno" + String.valueOf(empno) + "_")) {
			coki += "_idx" + request.getParameter("idx") + "_empno" + empno + "_";
			mapper.increment_hit(idx);
			// 한글, 세미콜론, 콤마, 이콜 사인, 공백은 쿠키 값으로 이용될 수 없기 떄문에 쿠키 추가전에 인코딩
			coki = URLEncoder.encode(coki, "utf-8"); 
			response.addCookie(new Cookie("coki", coki));
		}
		// =============쿠키 ==============
		BoardVO vo = mapper.selectContentByIdx(idx);

		model.addAttribute("currentPage", currentPage);
		model.addAttribute("BoardVO", vo);
		model.addAttribute("enter", "\r\n");

		if (category.equals("자유 게시판")) {
			
			GetList commentlist = new GetList();
			commentlist.setBoardList(mapper.selectCommentListALL(idx));
			model.addAttribute("commentList", commentlist);
			
			return "community/board/free_content_view";
		} else if (category.equals("공지사항")) {
			return "community/board/notice_content_view";
		} else if (category.equals("자료실")) {
			return "community/board/data_content_view";
		} else if (category.equals("QNA")) {
			
			GetList commentlist = new GetList();
			commentlist.setBoardList(mapper.selectCommentListALL(idx));
			model.addAttribute("commentList", commentlist);
			
			return "community/board/QNA_content_view";
		} else if (category.equals("팀 게시판")) {
			
			int totalCount = mapper.selectCommentCount(idx);
			
			GetList commentlist = new GetList(7, totalCount, currentPage);
			HashMap<String, Integer> hmap = new HashMap<String, Integer>();
			hmap.put("startNo", commentlist.getStartNo());
			hmap.put("endNo", commentlist.getEndNo());
			hmap.put("idx", idx);

			commentlist.setBoardList(mapper.selectCommentList(hmap));
			model.addAttribute("commentList", commentlist);
			
			return "team/team_content_view";
		}

		return "./error";
	}

	// 글 삭제
	@RequestMapping("/board_delete")
	public String contentdelete(HttpServletRequest request, Model model) {
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);

		int idx = Integer.parseInt(request.getParameter("idx"));
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		String category = request.getParameter("category");

		BoardVO vo = mapper.selectContentByIdx(idx);
		mapper.contentdelete(vo);

		model.addAttribute("currentPage", currentPage);
		model.addAttribute("category", category);
		
		if (category.equals("내가쓴글")) {
			return "redirect:../mypage/mywrite_view";
		}

		return "redirect:board_list";
	}

	// 글 수정 폼 이동
	@RequestMapping("/board_update")
	public String board_update(HttpServletRequest request, Model model) {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);

		int idx = Integer.parseInt(request.getParameter("idx"));

		BoardVO boardvo = mapper.selectContentByIdx(idx);

		model.addAttribute("currentPage", request.getParameter("currentPage"));
		model.addAttribute("BoardVO", boardvo);
		model.addAttribute("enter", "\r\n");

		if (boardvo.getCategory().equals("자유 게시판")) {
			return "community/board/freeboard_update";
		} else if (boardvo.getCategory().equals("공지사항")) {
			return "community/board/noticeboard_update";
		} else if (boardvo.getCategory().equals("자료실")) {
			return "community/board/databoard_update";
		}

		return "./error";
	}

	// 글 수정
	@RequestMapping("/board_updateOK")
	public String board_updateOK(HttpServletRequest request, Model model, BoardVO vo) {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		mapper.boardupdate(vo);

		model.addAttribute("idx", vo.getIdx());
		model.addAttribute("currentPage", request.getParameter("currentPage"));
		model.addAttribute("category", request.getParameter("category"));

		return "redirect:content_list";
	}
	
	// 첨부파일 수정
	@RequestMapping("/data_updateOK")
	public void data_updateOK(MultipartHttpServletRequest request, Model model, HttpServletResponse response, BoardVO vo, EmpVO empVO) throws IOException {
		
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
	
		int idx = Integer.parseInt(request.getParameter("idx"));
		try {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
		} catch (NumberFormatException e) {	}
		String category = request.getParameter("category");
		
		String rootUploarDir = "D:" + File.separator + "upload";
		File dir = new File(rootUploarDir);
		UUID uuid = UUID.randomUUID();

		if (!dir.exists()) {
			dir.mkdir(); 
		}

		Iterator<String> iterator = request.getFileNames();
		MultipartFile multipartFile = null;
		String realfilename = "";
		String attachedfile = "";

		while (iterator.hasNext()) {
			realfilename = iterator.next();
			multipartFile = request.getFile(realfilename);
			attachedfile = multipartFile.getOriginalFilename();

			if (attachedfile != null && attachedfile.length() != 0) {
				try {
					multipartFile.transferTo(new File(dir + File.separator + uuid.toString() + "_" + attachedfile)); 
					vo.setAttachedfile(attachedfile);
					vo.setRealfilename(uuid.toString() + "_" + attachedfile);
					

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (vo.getTitle() != null && vo.getTitle().trim().length() != 0 && vo.getContent() != null && vo.getContent().trim().length() != 0) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			
			model.addAttribute("idx", idx);
			model.addAttribute("currentPage", currentPage);
			model.addAttribute("category", category);
			
			mapper.data_update(vo);
			out.println("<script>alert('수정 완료')</script>");
			out.println("<script>location.href='content_list?idx=" + idx + "&category=" + category + "&currentPage=" + currentPage + "'</script>");
			out.flush();
		
		} else if (vo.getTitle() == null || vo.getTitle().trim().length() == 0) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>alert('제목을 입력하세요')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else if (vo.getContent() == null || vo.getContent().trim().length() == 0) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>alert('내용을 입력하세요')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		}	
	}

	// 댓글 서비스
	@RequestMapping("/comment_service")
	public String comment_service(HttpServletRequest request, Model model, BoardVO vo, HttpServletResponse response) throws IOException {
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);

		String content = request.getParameter("content");

		if (content == null || content.trim().length() == 0) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>alert('내용을 입력하세요')</script>");
			out.println("<script>history.back(-1);</script>");
			out.flush();
		} else {
			
			if (Integer.parseInt(request.getParameter("mode")) == 1) {
				mapper.commentinsert(vo); // 입력
			} else if (Integer.parseInt(request.getParameter("mode")) == 2) {
				mapper.boardupdate(vo); // 수정
			} 
		}

		model.addAttribute("idx", vo.getGup());
		model.addAttribute("currentPage", request.getParameter("currentPage"));
		model.addAttribute("category", request.getParameter("category"));

		return "redirect:content_list";
	}
	
	// 댓글 삭제
	@ResponseBody
	@RequestMapping("/comment_delete")
	public void comment_delete(HttpServletRequest request, BoardVO vo) {
		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		mapper.commentdelete(vo.getIdx());
	}

	// 조직도 메인
	@RequestMapping("/groupchart_main")
	public String groupchart_main(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		session.setAttribute("searchdeptno", null);
		session.setAttribute("searchname", null);
		return "community/groupchart_main";
	}

	// 특정 부서 데이터 보기
	@RequestMapping("/groupchart_view")
	public String groupchart_view(HttpServletRequest request, Model model) {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);
		HttpSession session = request.getSession();
		
		int deptno = Integer.parseInt(request.getParameter("deptno"));
		String searchname = request.getParameter("searchname");
		
		if (searchname != null) {
			searchname = searchname.trim().length() == 0 ? null : searchname;
			session.setAttribute("searchname", searchname);
		} else { // 페이지가 이동되어도 검색 내용 유지
			searchname = (String) session.getAttribute("searchname");
		}
		
		try {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
		} catch (NumberFormatException e) {
		}
		
		GetList empList = null;

		if (searchname == null ) {
			int totalCount = mapper.countByDept(deptno);
	
			empList = new GetList(PageSize, totalCount, currentPage);
	
			HashMap<String, Integer> hmap = new HashMap<String, Integer>();
			hmap.put("startNo", empList.getStartNo());
			hmap.put("endNo", empList.getEndNo());
			hmap.put("deptno", deptno);
	
			empList.setEmpList((ArrayList<EmpVO>) mapper.selectByDept(hmap));
		} else {	
			Param param = new Param();
			param.setSearchdeptno(deptno+"");
			param.setSearchname(searchname);
			
			int totalCount = mapper.countByMultiEmp(param);
			
			empList = new GetList(PageSize, totalCount, currentPage);
			
			param.setStartNo(empList.getStartNo());
			param.setEndNo(empList.getEndNo());
			
			empList.setEmpList((ArrayList<EmpVO>) mapper.selectByMultiEmp(param));			
		}
		model.addAttribute("EmpList", empList);
		model.addAttribute("deptno", deptno);

		return "community/groupchart_view";
	}

	// 회의실 메인 페이지
	@RequestMapping("/meetroom_main")
	public String meetroom_main(HttpServletRequest request, Model model, DateData dateData) {

		Calendar cal = Calendar.getInstance();

		// 달력 초기화
		if (dateData.getDate() == 0 && dateData.getMonth() == 0) {
			dateData = new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
		}

		if (dateData.getMonth() == 13) {
			dateData.setMonth(1);
			dateData.setYear(dateData.getYear() + 1);
		}

		if (dateData.getMonth() == 0) {
			dateData.setMonth(12);
			dateData.setYear(dateData.getYear() - 1);
		}

		TodoVO vo = new TodoVO();
		
		List<DateData> dateList = GetCalendar.month_info(dateData, sqlSession, vo); // 이번달 필드

		model.addAttribute("dateList", dateList);
		model.addAttribute("datedata", dateData); // 오늘 날짜

		return "community/meetroom_main";
	}

	@RequestMapping("/meetroom_select")
	public String meetroom_select(HttpServletRequest request, Model model, DateData dateData, MeetRoomVO mvo)
			throws ParseException {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String writedate = dateData.getYear() + "-" + dateData.getMonth() + "-" + dateData.getDate();
		mvo.setSetdate(sdf.parse(writedate));

		AbstractApplicationContext CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		GetList list103 = CTX.getBean("getList", GetList.class);
		list103.setMeetRmList(mapper.list103(mvo));
		model.addAttribute("list103", list103);

		CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		GetList list222 = CTX.getBean("getList", GetList.class);
		list222.setMeetRmList(mapper.list222(mvo));
		model.addAttribute("list222", list222);

		CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		GetList list503 = CTX.getBean("getList", GetList.class);
		list503.setMeetRmList(mapper.list503(mvo));
		model.addAttribute("list503", list503);

		CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		GetList list710 = CTX.getBean("getList", GetList.class);
		list710.setMeetRmList(mapper.list710(mvo));
		model.addAttribute("list710", list710);

		CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		GetList list901 = CTX.getBean("getList", GetList.class);
		list901.setMeetRmList(mapper.list901(mvo));
		model.addAttribute("list901", list901);

		model.addAttribute("meetdata", dateData);
		return "community/meetroom_select";
	}

	@RequestMapping("/meetroom_reserve")
	public String meetroom_reserve(HttpServletRequest request, Model model, DateData dateData, MeetRoomVO mvo)
			throws ParseException {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String writedate = dateData.getYear() + "-" + dateData.getMonth() + "-" + dateData.getDate();
		mvo.setSetdate(sdf.parse(writedate));

		AbstractApplicationContext CTX = new GenericXmlApplicationContext("classpath:applicationCTX.xml");
		GetList list = CTX.getBean("getList", GetList.class);
		
		if (mvo.getRoomnum() == 103) {
			list.setMeetRmList(mapper.list103(mvo));
		} else if (mvo.getRoomnum() == 222) {
			list.setMeetRmList(mapper.list222(mvo));
		} else if (mvo.getRoomnum() == 503) {
			list.setMeetRmList(mapper.list503(mvo));
		} else if (mvo.getRoomnum() == 710) {
			list.setMeetRmList(mapper.list710(mvo));
		} else if (mvo.getRoomnum() == 901) {
			list.setMeetRmList(mapper.list901(mvo));
		}

		model.addAttribute("roomlist", list);
		model.addAttribute("mvo", mvo);
		model.addAttribute("meetdata", dateData);
		return "community/meetroom_reserve";
	}

	@RequestMapping("/meetroom_confirm")
	public String meetroom_confirm(HttpServletRequest request, Model model, DateData dateData, MeetRoomVO mvo)
			throws ParseException {

		MyBatisDAO mapper = sqlSession.getMapper(MyBatisDAO.class);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String writedate = dateData.getYear() + "-" + dateData.getMonth() + "-" + dateData.getDate();
		mvo.setSetdate(sdf.parse(writedate));

		mapper.meetroominsert(mvo);

		model.addAttribute("year", dateData.getYear());
		model.addAttribute("month", dateData.getMonth());
		model.addAttribute("date", dateData.getDate());

		model.addAttribute("meetdate", mvo.getSetdate());

		return "redirect:meetroom_select";
	}
	
	@RequestMapping("/chat.action")
	public String chat (HttpServletRequest req, HttpServletResponse resp, HttpSession session) {
		return "community/chat";
	}
	
}
