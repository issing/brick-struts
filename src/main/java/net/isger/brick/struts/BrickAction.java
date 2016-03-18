package net.isger.brick.struts;

import javax.servlet.http.HttpServletRequest;

import net.isger.brick.ui.Screen;
import net.isger.brick.ui.UICommand;
import net.isger.brick.web.BrickListener;
import net.isger.util.Strings;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.inject.Inject;

public class BrickAction {

    @Inject(StrutsConstants.BRICK_PLUGIN_DOMAIN)
    private String domain;

    @Inject(StrutsConstants.BRICK_RESULT_NAME)
    private String name;

    private Screen screen;

    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        UICommand cmd = BrickListener.makeCommand(request,
                ServletActionContext.getResponse());
        /* 指定访问域（来自Struts配置） */
        if (Strings.isNotEmpty(this.domain)) {
            cmd.setDomain(domain);
        }
        BrickListener.getConsole(request.getSession().getServletContext())
                .execute(cmd);
        String name = null;
        Object result = cmd.getResult();
        if (result instanceof Screen) {
            this.screen = (Screen) result;
            name = (String) this.screen.see("@name");
            if (name != null) {
                name = Strings.empty(name, this.name);
            }
        }
        return name;
    }

    public Screen getScreen() {
        return screen;
    }

}
