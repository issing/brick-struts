package net.isger.brick.struts;

import org.apache.struts2.result.ServletDispatcherResult;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

import net.isger.util.Strings;

public class BrickResult extends ServletDispatcherResult {

    private static final long serialVersionUID = 5841958140257071556L;

    @Inject(StrutsConstants.BRICK_RESULT_SUFFIX)
    private String suffix;

    public void execute(ActionInvocation invocation) throws Exception {
        String location = getLocation();
        if (!Strings.endWithIgnoreCase(location, "[.]" + suffix)) {
            super.setLocation(location + "." + suffix);
        }
        super.execute(invocation);
    }

}
