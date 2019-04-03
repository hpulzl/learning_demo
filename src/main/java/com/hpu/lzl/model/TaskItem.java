package com.hpu.lzl.model;

import java.io.Serializable;

/**
*   
* @author:awo  
* @time:2019/3/26  下午3:45 
* @Description: info
**/
public class TaskItem implements Serializable{

    private String taskId;

    private String content;

    private String extra;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
