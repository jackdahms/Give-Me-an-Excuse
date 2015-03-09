package com.jackdahms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class GiveMeAnExcuse {
	
	public static final int INFO = 0,
							COMMAND = 1,
							SEVERE = 2;
	
	private static String USER_NAME = "givemeanexcuse@gmail.com"; 
	private static JTextArea display;
		
	private static int WIDTH = 850, HEIGHT = 550;
		
    public static void main(String[] args) throws Exception{
    	
    	Arrays.sort(args);
    	if (Arrays.binarySearch(args, "nogui") != -1)
    		;
    	else
    		createAndShowGUI();
    	
    	start();
    }
    
    public static void createAndShowGUI() throws Exception{
    	//set look and feel
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	//set up jframe
    	JFrame frame = new JFrame();
    	
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(WIDTH, HEIGHT);
    	frame.setLocationRelativeTo(null);
    	frame.setResizable(false);
    	
    	Container pane = frame.getContentPane();
    	pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
    	
    	int statWidth = WIDTH / 4;
    	
    	JPanel stats = new JPanel();
    	stats.setBorder(BorderFactory.createTitledBorder("Connected this session")); //TODO  rename title to "stats" once you've thought of some stats
    	stats.setMaximumSize(new Dimension(statWidth, HEIGHT));
    	stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
    	
    	JPanel numbers = new JPanel();
    	
    	JPanel session = new JPanel();
    	session.setBorder(BorderFactory.createTitledBorder("Session"));
    	session.setLayout(new GridLayout(1, 1));
    	
    	JTextArea players = new JTextArea();
    	players.setFont(new Font("courier", Font.PLAIN, 12));
    	players.setLineWrap(true);
    	players.setWrapStyleWord(true);
    	players.setEditable(false);
    	JScrollPane playScroll = new JScrollPane(players);
    	playScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    	
//    	session.add(playScroll); //TODO uncomment these lines once you've thought of some stats
    	
//    	stats.add(numbers);
//    	stats.add(session);
    	
    	stats.add(playScroll);
    	
    	JPanel log = new JPanel();
    	log.setBorder(BorderFactory.createTitledBorder("Log"));
    	log.setMaximumSize(new Dimension(WIDTH - statWidth, HEIGHT));
    	log.setLayout(new BorderLayout());
    	
    	display = new JTextArea();
    	display.setFont(new Font("courier", Font.PLAIN, 12));
    	display.setLineWrap(true);
    	display.setWrapStyleWord(true);
    	display.setEditable(false);
    	JScrollPane dispScroll = new JScrollPane(display);
    	dispScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    	
    	
    	JTextField comm = new JTextField();
    	comm.setFont(new Font("courier", Font.PLAIN, 12));
    	comm.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
    			command(comm.getText());
    			comm.setText("");
    		}
    	});
    	
    	log.add(dispScroll, BorderLayout.CENTER);
    	log.add(comm, BorderLayout.SOUTH);
    	
    	pane.add(stats);
    	pane.add(log);
    	
    	frame.setVisible(true);
    }
    
    public static void command(String msg) {
    	
    	append(1, msg);
    }
    
    public static void append(String msg) {
    	append(0, msg);
    }
    
    public static void append(int code, String msg) {
    	switch(code) {
    		case COMMAND: display.append("[COMMAND] "); break;
    	}
    	display.append(msg + "\n");
    }
    
    public static void start() throws Exception{
    	//things that only need to be set once
        String from = USER_NAME;
        List<String> excuses = new ArrayList<String>();
        String pass;
        boolean alive = true;
        int oldCount;
        int newCount;
        int diff = 0;
        int size;
        Random gen = new Random();
        
        //read password
        Scanner	key = new Scanner(GiveMeAnExcuse.class.getResourceAsStream("key.lock"));
        pass = key.nextLine();
        key.close();
        
        //read excuses
        Scanner excuse = new Scanner(GiveMeAnExcuse.class.getResourceAsStream("excuses.txt"));
        while (excuse.hasNextLine())
        	excuses.add(excuse.nextLine());
        excuse.close();
        size = excuses.size();
        
    	Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true"); 
        
        Session session = Session.getDefaultInstance(props);
        
        //for reading mail
        Store store = session.getStore();
        store.connect("imap.gmail.com", from, pass);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        oldCount = inbox.getMessageCount();
           
        while (alive) {
        	//detects new messages
        	newCount = inbox.getMessageCount();
        	diff = newCount - oldCount;
        	oldCount = newCount;
        	if (diff != 0) {
        		//loops in case incoming messages ever outpaces program
        		for (int i = 0; i < diff; i++) {
	        		//read message
	                Message msg = inbox.getMessage(inbox.getMessageCount() - i);
	                Address[] in = msg.getFrom();
                
                	//send message
                	MimeMessage message = new MimeMessage(session);  
	                
	                for (Address address : in) {
	                    System.out.println("FROM:" + address.toString());
	                    
	                    message.setFrom(new InternetAddress(from));
	                    InternetAddress toAddress = new InternetAddress(address.toString());
	                    message.setRecipient(Message.RecipientType.TO, toAddress);

	                    message.setText(excuses.get(gen.nextInt(size)));
	                    
	                    Transport transport = session.getTransport("smtp");
	                    transport.connect("smtp.gmail.com", from, pass);
	                    transport.sendMessage(message, message.getAllRecipients());
	                    transport.close();	                    
	                }
        		}
        	}
        }
    }
}//beautiful cascading brackets