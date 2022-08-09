import org.jivesoftware.smack.*;

import java.util.Collection;

public class Client {
    private final String SERVER_HOST = "alumchat.fun";
    private final int SERVER_PORT = 5222;
    private XMPPConnection connection;

    public Client(){
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER_HOST, SERVER_PORT);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setDebuggerEnabled(false);
        config.setSendPresence(true);

        connection = new XMPPConnection(config);

        try {
            connection.connect();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean login(String username, String password){
        try {
            if (!connection.isConnected()) connection.connect();
            this.connection.login(username, password);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean signup(String username, String password){
        try {
            AccountManager accountManager = this.connection.getAccountManager();
            accountManager.createAccount(username, password);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void logout(){
        connection.disconnect();
    }

    public boolean deleteAccount(){
        try {
            AccountManager accountManager = this.connection.getAccountManager();
            accountManager.deleteAccount();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public RosterEntry[] getContacts(){
        Roster roster = connection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();
        return entries.toArray(new RosterEntry[0]);
    }

    public void addUser(String username, String name){
        try {
            Roster roster = connection.getRoster();
            roster.createEntry(username, name, null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
