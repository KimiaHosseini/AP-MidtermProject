package UserFeatures;

import java.io.Serial;
import java.io.Serializable;

public enum Status implements Serializable {
    ONLINE,
    IDLE,
    DND,
    INVISIBLE,
    OFFLINE;
}
