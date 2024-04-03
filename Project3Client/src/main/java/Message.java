/*
* WARNING: This class must be identical to the message class in the server. If it is not, no messages will be accepted
* WARNING: This class does not dictate how the data should be gathered. The data can be gathered from text input,
* drop down menus, etc.
* */
import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    String message; // Message to be sent
    String groupName; // either name of new group or name of group being sent to
    String userName; // either name of new username or name of user message is being sent to

    Boolean newUser; // False if user is existing, True if user is new
    Boolean isNewGroup; // False is user is not creating a group, True otherwise
    Boolean sendAll; // False if the message is private, True otherwise

    ArrayList<String> groupNames; // List of names for group to be created

    // todo: Add anymore attributes that can be helpful
}
