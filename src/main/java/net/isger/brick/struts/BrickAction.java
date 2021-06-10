package net.isger.brick.struts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.Parameter;
import org.apache.struts2.dispatcher.multipart.StrutsUploadedFile;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Inject;

import net.isger.brick.Constants;
import net.isger.brick.auth.AuthCommand;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.ui.Screen;
import net.isger.brick.web.BrickListener;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.Strings;

/**
 * 访问活动
 * 
 * @author issing
 */
public class BrickAction {

    private static final Callable<Object> PURGER;

    @Inject(StrutsConstants.BRICK_RESULT_NAME)
    private String name;

    @Inject("struts.i18n.encoding")
    private String encoding;

    private Screen screen;

    static {
        PURGER = new Callable<Object>() {
            public Object call(Object... args) {
                Object value = args[1];
                if (value instanceof StrutsUploadedFile) {
                    value = ((StrutsUploadedFile) value).getContent();
                }
                return value;
            }
        };
    }

    /**
     * 活动入口
     *
     * @return
     */
    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (Map.Entry<String, Parameter> entry : ActionContext.getContext().getParameters().entrySet()) {
            parameters.put(entry.getKey(), purge(entry.getValue().getObject()));
        }
        BaseCommand cmd = BrickListener.makeCommand(request, response, parameters);
        /* 执行命令 */
        BrickListener.getConsole(request.getSession().getServletContext()).execute(cmd);
        Object result = cmd.getResult();
        /* 授权访问 */
        if (cmd instanceof AuthCommand) {
            if (!Helpers.toBoolean(result)) {
                name = "unauth";
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
                    name = Strings.empty(name, this.name); // 空字符串替换为默认值
                }
            }
        }
        return name;
    }

    /**
     * 净化值
     *
     * @param value
     * @return
     */
    private Object purge(Object value) {
        return Helpers.compact(Helpers.each(value, PURGER));
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
