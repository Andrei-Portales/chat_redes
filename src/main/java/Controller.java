import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import models.MessageModel;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.*;
import org.jivesoftware.smackx.muc.MultiUserChat;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {
    private Client client;

    public void start() {
        client = new Client();
        this.loginMenu();
    }

    private void cleanScreen() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    private void loginMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {
            cleanScreen();
            System.out.println("Elija la opción deseada:\n");
            System.out.println("1. Iniciar sesion");
            System.out.println("2. Registrarse");
            System.out.println("3. Salir\n");
            System.out.print("Opción: ");

            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    loginForm();
                    break;
                case 2:
                    registerForm();
                    break;
                case 3:
                    this.cleanScreen();
                    System.out.println("--- Chat finalizado ---");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Opción no valida");
                    break;
            }
        }
    }

    private void registerForm() {
        cleanScreen();
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Registro ---");
        System.out.print("Usuario: ");
        String username = scanner.nextLine();
        System.out.print("Contraseña: ");
        String password = scanner.nextLine();
        boolean success = this.client.signin(username, password);
        if (success) {
            System.out.println("Registro exitoso");
        } else {
            System.out.println("Registro fallido");
        }
        System.out.println("Presione enter para continuar");
        scanner.nextLine();
    }

    private void loginForm() {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {
            cleanScreen();
            System.out.print("Ingrese su nombre de usuario ('exit' para salir): ");
            String username = scanner.nextLine();
            if (username.equals("exit")) {
                isRunning = false;
            } else {
                System.out.print("Ingrese su contraseña: ");
                String password = scanner.nextLine();
                System.out.println("\n\nIniciando sesión...\n");
                if (client.login(username, password)) {
                    isRunning = false;
                    this.mainMenu();
                } else {
                    this.cleanScreen();
                    System.out.println("Error al iniciar sesión, presione enter para volver a intentar...");
                    scanner.nextLine();
                }
            }

        }
    }

    private void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {
            this.cleanScreen();
            System.out.println(client.getCurrentUser() + "\n");
            System.out.println("---------------------------------------------------------\n");

            System.out.println("Elija la opción deseada:\n");

            System.out.println("1. Agregar contacto");
            System.out.println("2. Ver lista de contactos");
            System.out.println("3. Entrar a chat privado");
            System.out.println("4. Entrar a chat grupal");
            System.out.println("5. Definir mensaje de presencia");
            System.out.println("6. Cerrar sesion");
            System.out.println("7. Eliminar cuenta\n");

            System.out.print("Opción: ");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    this.addContact();
                    break;
                case 2:
                    this.cleanScreen();
                    this.getContacts();
                    System.out.println("Presione enter para volver al menu principal...");
                    scanner.nextLine();
                    scanner.nextLine();
                    break;
                case 3:
                    this.cleanScreen();
                    System.out.print("Ingrese el usuario del contacto con quien desea chatear: \n\n");

                    RosterEntry[] contacts = client.getContacts();
                    for (int i = 0; i < contacts.length; i++) {
                        System.out.println(i + 1 + ". " + contacts[i].getUser());
                    }

                    System.out.print("\nUsuario: ");
                    String username = scanner.nextLine();
                    username = scanner.nextLine();
                    System.out.println("\n\nIniciando chat privado con " + username + "...");

                    String finalUsername = username;
                    Thread thread = new Thread(() -> chatSingleRoom(finalUsername));
                    thread.start();

                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    this.cleanScreen();
                    System.out.print("Ingrese el nombre del grupo con el que desea chatear: ");

                    String groupName = scanner.nextLine();
                    groupName = scanner.nextLine();
                    System.out.println("\n\nIniciando chat grupal con " + groupName + "...");

                    String finalGroupName = groupName;
                    Thread thread2 = new Thread(() -> chatGroupRoom(finalGroupName));
                    thread2.start();

                    try {
                        thread2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    this.cleanScreen();
                    System.out.print("Elija el mensaje de presencia que desea: \n\n");
                    System.out.println("1. Disponible");
                    System.out.println("2. No disponible");

                    Presence.Type type = null;

                    System.out.print("\nOpción: ");
                    int typeInt = scanner.nextInt();

                    switch (typeInt) {
                        case 1:
                            type = Presence.Type.available;
                            break;
                        case 2:
                            type = Presence.Type.unavailable;
                            break;
                        default:
                            System.out.println("Opción no valida");
                            break;
                    }

                    if (type != null) {
                        boolean result = client.setPresence(type);
                        if (result) {
                            System.out.println("\nMensaje de presencia definido");
                        } else {
                            System.out.println("\nError al definir mensaje de presencia");
                        }
                    }

                    System.out.println("\n\nPresione enter para volver al menu principal...");
                    scanner.nextLine();
                    scanner.nextLine();

                    break;
                case 6:
                    client.logout();
                    System.exit(0);
                    isRunning = false;
                    break;
                case 7:
                    this.cleanScreen();
                    System.out.println("--- Eliminando cuenta ---");
                    client.deleteAccount();
                    isRunning = false;
                    break;
            }
        }
    }

    private void addContact() {
        Scanner scan = new Scanner(System.in);
        cleanScreen();
        System.out.print("Ingrese el nombre de usuario del contacto: ");
        String username = scan.nextLine();
        System.out.print("Ingrese el nombre del contacto: ");
        String name = scan.nextLine();

        if (client.addContact(username, name)) {
            System.out.println("Contacto agregado");
        } else {
            System.out.println("Error al agregar contacto");
        }

        System.out.println("\nPresione enter para volver al menu principal...");
        scan.nextLine();
    }

    private void getContacts() {
        RosterEntry[] contacts = client.getContacts();
        System.out.println("Lista de contactos:\n");

        for (RosterEntry contact : contacts) {
            System.out.println("Usuario: " + contact.getUser());
            System.out.println("Nombre: " + contact.getName());
            System.out.println("Status: " + client.getPresence(contact.getUser()));
            System.out.println("");
        }
    }

    private void chatSingleRoom(String username) {
        try {
            // Screen configuration
            Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
            AtomicReference<MessageModel[]> chatMessages = new AtomicReference<MessageModel[]>(new MessageModel[]{});

            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            screen.doResizeIfNecessary();
            StringBuilder message = new StringBuilder();

            AtomicInteger screenWidth = new AtomicInteger(screen.getTerminalSize().getColumns());
            AtomicInteger screenHeight = new AtomicInteger(screen.getTerminalSize().getRows());

            this.drawChatScreen(screen, username, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.get());
            terminal.addResizeListener((x, y) -> {
                screenWidth.set(y.getColumns());
                screenHeight.set(y.getRows());
                this.drawChatScreen(screen, username, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.get());
            });

            // Chat configuration
            ChatManager chatManager = client.getChatManager();
            Chat chat = chatManager.createChat(username, null);


            // messages change listener
            client.setMessageChangeList((allMessages) -> {
                // filteredMessages
                ArrayList<MessageModel> filteredMessages = new ArrayList();
                for (MessageModel m : allMessages) {
                    if (m.getType() == Message.Type.chat && (m.getFrom().split("/")[0].equals(username) || (m.getTo().equals(username) && m.getFrom().equals("Yo")))) {
                        filteredMessages.add(m);
                    }
                }
                chatMessages.set(filteredMessages.toArray(new MessageModel[]{}));
                this.drawChatScreen(screen, username, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.get());
            });

            while (true) {
                Character input = screen.readInput().getCharacter();

                if (input == '\n') {
                    if (message.toString().equals("/exit")) {
                        System.out.println("Saliendo del chat...");
                        screen.close();
                        break;
                    } else if (message.toString().startsWith("/file")) {
                        try {
                            final JFileChooser fc = new JFileChooser();
                            int returnVal = fc.showOpenDialog(null);

                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                File file = fc.getSelectedFile();
                                System.out.println("Enviando archivo...");

                                if (file.exists()) {
                                    FileTransferManager transferManager = new FileTransferManager(client.getConnection());
                                    OutgoingFileTransfer transfer = transferManager.createOutgoingFileTransfer(username);
                                    transfer.sendFile(file, "Archivo de " + file.getName());

                                    System.out.println("Archivo enviado");

                                } else {
                                    System.out.println("Archivo no encontrado...");
                                }

                            }

                            message.setLength(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        chat.sendMessage(message.toString());
                        client.addMessage(
                                new MessageModel(
                                        "Yo",
                                        username,
                                        message.toString(),
                                        Message.Type.chat
                                )
                        );
                        message.setLength(0);
                    }
                } else if (input == '\b') {
                    if (message.length() > 0) {
                        message.deleteCharAt(message.length() - 1);
                    }
                } else {
                    if (input != null) message.append(input);
                }
                this.drawChatScreen(screen, username, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al iniciar chat");

        }
        client.setMessageChangeList(null);
    }

    private void chatGroupRoom(String room) {
        try {
            // Screen configuration
            Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
            ArrayList<MessageModel> chatMessages = new ArrayList<>();

            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            screen.doResizeIfNecessary();
            StringBuilder message = new StringBuilder();

            AtomicInteger screenWidth = new AtomicInteger(screen.getTerminalSize().getColumns());
            AtomicInteger screenHeight = new AtomicInteger(screen.getTerminalSize().getRows());

            this.drawChatScreen(screen, room, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.toArray(new MessageModel[]{}));
            terminal.addResizeListener((x, y) -> {
                screenWidth.set(y.getColumns());
                screenHeight.set(y.getRows());
                this.drawChatScreen(screen, room, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.toArray(new MessageModel[]{}));
            });

            // Chat configuration
            MultiUserChat muc = new MultiUserChat(client.getConnection(), room + "@conference.alumchat.fun");
            muc.join(client.getConnection().getUser());

            muc.addMessageListener((packet -> {
                Message messagePacket = (Message) packet;
                String[] from = messagePacket.getFrom().split("/");
                chatMessages.add(
                        new MessageModel(
                                from[from.length - 1],
                                messagePacket.getTo(),
                                messagePacket.getBody(),
                                messagePacket.getType()
                        )
                );
                this.drawChatScreen(screen, room, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.toArray(new MessageModel[]{}));

            }));

            while (true) {
                Character input = screen.readInput().getCharacter();

                if (input == '\n') {
                    if (message.toString().equals("/exit")) {
                        System.out.println("Saliendo del chat...");
                        screen.close();
                        break;
                    } else if (message.toString().equals("/file")) {

                    } else {
                        muc.sendMessage(message.toString());
                        message.setLength(0);
                    }
                } else if (input == '\b') {
                    if (message.length() > 0) {
                        message.deleteCharAt(message.length() - 1);
                    }
                } else {
                    if (input != null) message.append(input);
                }
                this.drawChatScreen(screen, room, message.toString(), screenHeight.get(), screenWidth.get(), chatMessages.toArray(new MessageModel[]{}));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al iniciar chat");

        }
    }

    private void drawChatScreen(Screen screen, String username, String message, int height, int width, MessageModel[] messages) {
        try {
            screen.clear();
            screen.doResizeIfNecessary();
            TextGraphics tg = screen.newTextGraphics();

            // draw messages box
            int boxHeight = 20;
            tg.drawRectangle(
                    new TerminalPosition(0, 2),
                    new TerminalSize(width - 1, height - 5),
                    '-'
            );

            // draw user
            tg.putString(1, 1, "Usuario: " + username);

            int maxLengthInputText = width - 7;

            String inputText = message.length() > maxLengthInputText ?
                    message.substring(message.length() - maxLengthInputText) :
                    message;

            // draw input box
            tg.putString(2, height - 2, "= " + inputText);

            screen.setCursorPosition(
                    new TerminalPosition(
                            inputText.length() + 4,
                            height - 2
                    )
            );

            tg.drawRectangle(
                    new TerminalPosition(0, height - 3),
                    new TerminalSize(width - 1, 3),
                    '-'
            );

            // draw messages
            int messageCount = height - 7;
            // initialHeight: 3, initialWidth: 1
            // last messageCount messages from messages
            ArrayList<MessageModel> messagesToDraw = new ArrayList<>();

            int initialIndex = messages.length > messageCount ? messages.length - messageCount : 0;
            for (int i = initialIndex; i < messages.length; i++) {
                messagesToDraw.add(messages[i]);
            }

            for (int i = 0; i < messagesToDraw.size(); i++) {
                MessageModel messageModel = messagesToDraw.get(i);
                tg.putString(1, i + 3, "-> " + messageModel.getFrom() + ": " + messageModel.getMessage());
            }

            screen.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al refrescar chat");
        }

    }
}
