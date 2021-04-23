public class UserActivity {
    private String userId;
    private int commentCnt, issueCnt;

    public UserActivity(String newId, int newCmntCnt, int newIssCnt)
    {
        userId = newId;
        commentCnt = newCmntCnt;
        issueCnt = newIssCnt;
    }

    public UserActivity(String newId){
        this(newId, 0, 0);
    }

    public String getUserId() { return userId; }
    public int getCommentCnt() { return commentCnt; }
    public int getIssueCnt() { return issueCnt; }

    public void setCommentCnt(int newCnt) { commentCnt = newCnt; }
    public void setIssueCnt(int newCnt) { issueCnt = newCnt; }
}
