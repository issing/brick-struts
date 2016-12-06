package net.isger.brick.struts;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.isger.brick.auth.AuthCommand;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.ui.Screen;
import net.isger.brick.web.BrickListener;
import net.isger.util.Strings;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.inject.Inject;

public class BrickAction {

    @Inject(StrutsConstants.BRICK_RESULT_NAME)
    private String name;

    private Screen screen;

    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        ServletContext context = request.getSession().getServletContext();
        BaseCommand cmd = BrickListener.makeCommand(request,
                ServletActionContext.getResponse());
        /* 执行命令 */
        BrickListener.getConsole(context).execute(cmd);
        Object result = cmd.getResult();
        /* 授权访问 */
        if (cmd instanceof AuthCommand) {
            /* TODO 访问拒绝 */
            if (!(boolean) result) {
                System.out.println("访问拒绝");
            }
            result = ((BaseCommand) ((AuthCommand) cmd).getToken()).getResult();
        }
        /* 屏显处理 */
        String name = null;
        if (result instanceof Screen) {
            this.screen = (Screen) result;
            name = (String) this.screen.see("@name");
            if (name != null) {
                // 空字符串替换为默认值
                name = Strings.empty(name, this.name);
            }
        }
        return name;
    }

    public Screen getScreen() {
        return screen;
    }

}
