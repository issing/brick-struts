package net.isger.brick.struts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Inject;

import net.isger.brick.Constants;
import net.isger.brick.auth.AuthCommand;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.ui.Screen;
import net.isger.brick.web.BrickListener;
import net.isger.util.Helpers;
import net.isger.util.Strings;

/**
 * 行为活动
 * 
 * @author issing
 */
public class BrickAction {

    @Inject(StrutsConstants.BRICK_RESULT_NAME)
    private String name;

    @Inject("struts.i18n.encoding")
    private String encoding;

    private Screen screen;

    /**
     * 活动入口
     *
     * @return
     */
    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        BaseCommand cmd = BrickListener.makeCommand(request, response, ActionContext.getContext().getParameters());
        /* 执行命令 */
        BrickListener.getConsole(request.getSession().getServletContext()).execute(cmd);
        Object result = cmd.getResult();
        /* 授权访问 */
        if (cmd instanceof AuthCommand) {
            /* TODO 访问拒绝 */
            if (!Helpers.toBoolean(result)) {
                System.out.println("访问拒绝");
            }
            result = ((BaseCommand) ((AuthCommand) cmd).getToken()).getResult();
        }
        /* 界面导向 */
        String name = null;
        if (result instanceof Screen) {
            screen = (Screen) result;
            if (screen.see("@stream") != null) {
                name = "stream";
            } else {
                name = (String) screen.see("@name");
                if (name == null) {
                    if ((result = screen.see("result")) != null) {
                        response.setContentType("text/plain; charset=" + Strings.empty(encoding, Constants.DEFAULT_ENCODING));
                        try {
                            response.getWriter().print(result);
                        } catch (IOException e) {
                        }
                    }
                } else {
                    // 空字符串替换为默认值
                    name = Strings.empty(name, this.name);
                }
            }
        }
        return name;
    }

    /**
     * 输入流
     *
     * @return
     */
    public InputStream getInputStream() {
        try {
            Object pending = screen.see("@stream");
            if (pending instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) pending);
            } else if (pending instanceof String) {
                return new FileInputStream((String) pending);
            } else if (pending instanceof File) {
                return new FileInputStream((File) pending);
            } else if (pending instanceof InputStream) {
                return (InputStream) pending;
            }
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * 屏幕
     *
     * @return
     */
    public Screen getScreen() {
        return screen;
    }

}
