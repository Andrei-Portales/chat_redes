import models.MessageModel;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;


import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class Client {
    private final String SERVER_HOST = "alumchat.fun";
    private final int SERVER_PORT = 5222;
    private XMPPConnection connection;
    private ArrayList<MessageModel> allMessages = new ArrayList<>();
    private MessageChangeList messageChangeList;

    SystemTray tray = SystemTray.getSystemTray();
    private TrayIcon trayIconPrev;

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

    public XMPPConnection getConnection() {
        return connection;
    }

    public boolean login(String username, String password) {
        try {
            if (!connection.isConnected()) connection.connect();
            this.connection.login(username, password);
            connection.sendPacket(new Presence(Presence.Type.available));
            setChatListeners();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean signin(String username, String password) {
        try {
            AccountManager accountManager = this.connection.getAccountManager();
            accountManager.createAccount(username, password);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showNotification(String title, String message) {
        try {
            if (trayIconPrev != null) {
                tray.remove(trayIconPrev);
            }

            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Andrei Portales Chat");
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

            trayIconPrev = trayIcon;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setMessageChangeList(MessageChangeList messageChangeList) {
        this.messageChangeList = messageChangeList;
        if (this.messageChangeList != null) {
            this.messageChangeList.messageChangeList(allMessages.toArray(new MessageModel[allMessages.size()]));
        }
    }

    public void addMessage(MessageModel message) {
        allMessages.add(message);
        if (this.messageChangeList != null) {
            this.messageChangeList.messageChangeList(allMessages.toArray(new MessageModel[allMessages.size()]));
        }
    }

    public void setChatListeners() {

        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                if (packet instanceof Message) {
                    Message message = (Message) packet;
                    if (message.getBody() != null) {
                        allMessages.add(new MessageModel(
                                message.getFrom(),
                                message.getTo(),
                                message.getBody(),
                                message.getType()
                        ));
                        showNotification(message.getFrom(), message.getBody());
                        if (messageChangeList != null) {
                            messageChangeList.messageChangeList(allMessages.toArray(new MessageModel[allMessages.size()]));
                        }
                    }
                }
            }
        }, new PacketTypeFilter(Packet.class));

        // send notification to every new user
        Roster roster = connection.getRoster();
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {
                for (String address : addresses) {
                    showNotification(address, "has joined the chat");
                }
            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {
                for (String address : addresses) {
                    showNotification(address, "has updated his profile");
                }
            }
            @Override
            public void entriesDeleted(Collection<String> addresses) {
                for (String address : addresses) {
                    showNotification(address, "has left the chat");
                }
            }
            @Override
            public void presenceChanged(Presence presence) {
                    String statusMessage = presence.getStatus() != null ? presence.getStatus() : "";
                    Presence availableStatus = getPresence(presence.getFrom());
                    showNotification(presence.getFrom() + " cambio de estado", "\nEstado: " + availableStatus + "\nMensaje: " + statusMessage);
            }
        });
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
