import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

public class Client {
    private final String SERVER_HOST = "alumchat.fun";
    private final int SERVER_PORT = 5222;
    private XMPPConnection connection;

    public Client() {
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER_HOST, SERVER_PORT);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        config.setDebuggerEnabled(false);
        config.setSendPresence(true);

        connection = new XMPPConnection(config);

        try {
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean login(String username, String password) {
        try {
            if (!connection.isConnected()) connection.connect();
            this.connection.login(username, password);
            connection.sendPacket(new Presence(Presence.Type.available));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean signup(String username, String password) {
        try {
            AccountManager accountManager = this.connection.getAccountManager();
            accountManager.createAccount(username, password);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        connection.sendPacket(new Presence(Presence.Type.unavailable));
        this.connection.disconnect();
    }

    public boolean deleteAccount() {
        try {
            AccountManager accountManager = this.connection.getAccountManager();
            accountManager.deleteAccount();
            this.connection.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public RosterEntry[] getContacts() {
        Roster roster = connection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();
        return entries.toArray(new RosterEntry[0]);
    }

    public boolean addContact(String username, String name) {
        try {
            Roster roster = connection.getRoster();
            roster.createEntry(username, name, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUser() {
        return "Id de conexion: " + this.connection.getConnectionID() + "\n" +
                "Usuario: " + this.connection.getUser() + "\n" +
                "Host: " + this.connection.getHost() + "\n" +
                "Estado: " + this.getUserPresence();

    }

    public ChatManager getChatManager() {
        ChatManager chatManager = this.connection.getChatManager();
        return chatManager;
    }

    public Presence getPresence(String username) {
        Roster roster = connection.getRoster();
        return roster.getPresence(username);
    }

    public Presence getUserPresence() {
        return connection.getRoster().getPresence(connection.getUser());
    }

    public boolean setPresence(Presence.Type type) {
        try {
            connection.sendPacket(new Presence(type));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
