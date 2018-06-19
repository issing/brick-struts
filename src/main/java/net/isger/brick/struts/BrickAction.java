package net.isger.brick.struts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Inject;

import net.isger.brick.auth.AuthCommand;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.ui.Screen;
import net.isger.brick.web.BrickListener;
import net.isger.util.Helpers;
import net.isger.util.Strings;

public class BrickAction {

    @Inject(StrutsConstants.BRICK_RESULT_NAME)
    private String name;

    private Screen screen;

    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        BaseCommand cmd = BrickListener.makeCommand(request,
                ServletActionContext.getResponse(),
                ActionContext.getContext().getParameters());
        /* 执行命令 */
        BrickListener.getConsole(request.getSession().getServletContext())
                .execute(cmd);
        Object result = cmd.getResult();
        /* 授权访问 */
        if (cmd instanceof AuthCommand) {
            /* TODO 访问拒绝 */
            if (!Helpers.toBoolean(result)) {
                System.out.println("访问拒绝");
            }
            result = ((BaseCommand) ((AuthCommand) cmd).getToken()).getResult();
        }
        /* 屏显处理 */
        String name = null;
        if (result instanceof Screen) {
            this.screen = (Screen) result;
            if (this.screen.see("@stream") != null) {
                name = "stream";
            } else {
                name = (String) this.screen.see("@name");
                if (name != null) {
                    // 空字符串替换为默认值
                    name = Strings.empty(name, this.name);
                }
            }
        }
        return name;
    }

    public InputStream getInputStream() {
        try {
            Object pending = screen.see("@stream");
            if (pending instanceof String) {
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

    public Screen getScreen() {
        return screen;
    }

}
