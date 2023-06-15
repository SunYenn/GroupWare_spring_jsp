package com.tjoeun.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tjoeun.vo.*;

// mapper 연결에 사용하는 인터페이스
public interface MyBatisDAO {

	// emp
	int confirmEmpno(int empno);
	EmpVO selectlogin(int empno);
	EmpVO registerCheck(String empno);	
	void register(EmpVO vo);
	void registerprofile(EmpVO vo);
	void registerUpdate(EmpVO vo);
	String findID(EmpVO vo); // 아이디 찾기
	String findPW(EmpVO vo); // 비밀번호 찾기
	ArrayList<EmpVO> searchemp(Param pr); // 쪽지 전송 받는 사람 자동완성

	// emp_profile
	void updateProfile(EmpVO vo); // 프로필 이미지 등록
	
	// board
	int selectCount(String category);
	ArrayList<BoardVO> selectList(Param param);
	int selecCountMulti(Param param);
	ArrayList<BoardVO> selectListMulti(Param param);
	int selectQNACount(String category);
	ArrayList<BoardVO> selectQNAList(Param param);
	int countByDept(int deptno);
	ArrayList<EmpVO> selectByDept(HashMap<String, Integer> hmap);
	int mywrite_selectCount(int empno);
	ArrayList<BoardVO> mywrite_selectList(HashMap<String, Integer> myhmap);
	void boardinsert(BoardVO vo);
	void answerinsert(BoardVO vo);
	void increment_hit(int idx);
	BoardVO selectContentByIdx(int idx);
	int selectCommentCount(int idx);
	ArrayList<BoardVO> selectCommentList(HashMap<String, Integer> hmap);
	ArrayList<BoardVO> selectCommentListALL(int idx);
	void contentdelete(BoardVO vo);
	void boardupdate(BoardVO vo);
	void commentinsert(BoardVO vo);
	void commentdelete(int idx);
	ArrayList<BoardVO> selectBoardList(String string); // 메인 화면 board 리스트
	void insertattach(BoardVO vo);
	void data_update(BoardVO vo); // 첨부파일 수정
	
	// message
	void sendmessage(MessageVO vo);
	int noreadCount(int empno);
	
	int receiveMessageCount(int empno);
	ArrayList<MessageVO> receiveMessageList(Param param);
	int receiveMessageCountMulti(Param param);
	ArrayList<MessageVO> receiveMessageListMulti(Param param);
	
	int sendMessageCount(int empno);
	ArrayList<MessageVO> sendMessageList(Param param);
	int sendMessageCountMulti(Param param);
	ArrayList<MessageVO> sendMessageListMulti(Param param);
	
	int trashMessageCount(int empno);
	ArrayList<MessageVO> trashMessageList(Param param);
	int trashMessageCountMulti(Param param);
	ArrayList<MessageVO> trashMessageListMulti(Param param);
	
	MessageVO messageSelectByIdx(int idx);
	void updateMessageStatus(int idx);
	void messageRemove(MessageVO meo);
	void messageRestore(MessageVO meo);
	void messageDelete(MessageVO meo);

	// todo
	int maxidx(TodoVO vo);
	void todoinsert(TodoVO vo);
	ArrayList<TodoVO> todolist(TodoVO todovo);
	void todoupdateS(TodoVO vo);
	void tododelete(TodoVO vo);
	void todoupdate(TodoVO vo);
	ArrayList<TodoVO> caltodolist(TodoVO todo);
	ArrayList<TodoVO> caltodolistShareset(TodoVO todo);
	
	// meetroom
	void meetroominsert(MeetRoomVO mvo);
	ArrayList<MeetRoomVO> list103(MeetRoomVO mvo);
	ArrayList<MeetRoomVO> list222(MeetRoomVO mvo);
	ArrayList<MeetRoomVO> list503(MeetRoomVO mvo);
	ArrayList<MeetRoomVO> list710(MeetRoomVO mvo);
	ArrayList<MeetRoomVO> list901(MeetRoomVO mvo);
	ArrayList<MeetRoomVO> list907(MeetRoomVO mvo);
	
	// manager
	int countwaiting(); // emp.xml
	ArrayList<EmpVO> selectwaiting(HashMap<String, Integer> hmap); // emp.xml
	void account_approval(EmpVO vo); // emp.xml
	int countemp(); // emp.xml
	ArrayList<EmpVO> selectemp(HashMap<String, Integer> hmap); // emp.xml
	int countByName(String searchname); // emp.xml
	ArrayList<EmpVO> selectByName(Param param); // emp.xml
	int countByMultiEmp(Param param); // emp.xml
	ArrayList<EmpVO> selectByMultiEmp(Param param); // eml.xml
	void updatePMS(EmpVO vo); // emp.xml
	int AllBoardCount(); // board.xml
	ArrayList<BoardVO> AllBoardSelect(Param param); // board.xml
	int NoCatCountMulti(Param param); // board.xml
	ArrayList<BoardVO> NoCatSelectMulti(Param param); // board.xml
	void categoryupdate(BoardVO vo); // board.xml
	
	// attend
	int countAttnedAll(); 
	ArrayList<AttendVO> selectAttendAll(HashMap<String, Integer> hmap); 
	int countAttendByDept(int deptno); 
	ArrayList<AttendVO> selectAttendByDept(HashMap<String, Integer> hmap);
	int countAttendByDate(String searchdate);
	ArrayList<AttendVO> selectAttendByDate(Param param);
	int countAttendByName(String searchname);
	ArrayList<AttendVO> selectAttendByName(Param param);
	int countAttendByDeptDate(Param param);
	ArrayList<AttendVO> selectAttendByDeptDate(Param param);
	int countAttendByDateName(Param param);
	ArrayList<AttendVO> selectAttendByDateName(Param param);
	int countDayoffByEmpno(int empno);
	ArrayList<AttendVO> selectDayoffByEmpno(Param param);
	PaySlipVO showPaySlip(Param param); // 급여명세서

	// approval
	int approvalCount_YET(int empno);
	int approvalCount_UNDER(int empno);
	int approvalCount_DONE(int empno);
	int approvalCount_CANCEL(int empno);
	List<ApprovalVO> selectRecentList(Param param);
	List<ApprovalVO> selectRecentList1(Param param);

	List<EmpVO> selectMemberAllForApproval(EmpVO empvo);
	List<EmpVO> selectApprover(ApprovalVO vo);

	int listCount(Param param);
	ArrayList<ApprovalVO> selectApprovalList(Param param);

	int approved1(int appNo);
	int approved2(int appNo);
	int approved3(int appNo);
	
	int canceled1(ApprovalVO vo);
	int canceled2(ApprovalVO vo);
	int canceled3(ApprovalVO vo);
	
	int insertApproval(ApprovalVO vo);
	int insertAppLoa(ApprovalVO vo);
	int insertAppLeave(ApprovalVO vo);
	int insertAppER(ApprovalVO vo);
	int insertReceiveRef(ApprovalVO vo);
	
	ApprovalVO selectLOAListDetail(int appNo); // 품의서 한 건
	ApprovalVO selectExpenseReportListDetail(int appNo); // 지출결의서 한 건
	ApprovalVO selectLeaveListDetail(int appNo); // 휴가신청서 한 건

	// 검색
	int AselecCountMulti(Param param);
	ArrayList<ApprovalVO> AselectListMulti(Param param);
	List<EmpVO> selectMemberByName(String searchName);
	int updateApprovalFromCancel(Map<String, Object> bodyMap);


}
