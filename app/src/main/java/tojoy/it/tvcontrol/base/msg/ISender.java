package tojoy.it.tvcontrol.base.msg;

import java.io.Serializable;

public interface ISender extends Serializable {
    /**
     * 打包要发送的数据
     *
     * @return
     */
    byte[] parse();
}
