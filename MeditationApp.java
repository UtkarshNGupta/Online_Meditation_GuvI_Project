import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;

public class MeditationApp {
    private static ArrayList<MeditationSession> sessions = new ArrayList<>();
    
    public static void main(String[] args) {
        // Try database connection, if fails use demo data
        loadSessions();
        
        // Create GUI
        createGUI();
    }
    
    private static void loadSessions() {
        // Try database connection first
        boolean dbConnected = tryLoadFromDatabase();
        
        if (!dbConnected) {
            // Use demo data if database fails
            System.out.println("Database connection failed! Using demo data...");
            loadDemoData();
        }
    }
    
    // Database connection method
    private static boolean tryLoadFromDatabase() {
        try {
            // Database connection details
            String url = "jdbc:mysql://localhost:3306/meditation_app";
            String user = "root";
            String password = "password";  // Change to your MySQL password
            
            // Load MySQL driver and connect
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            
            // Load sessions from database
            String sql = "SELECT * FROM sessions";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration");
                String type = rs.getString("type");
                String description = rs.getString("description");
                
                if ("GUIDED".equals(type)) {
                    sessions.add(new GuidedSession(title, duration, description));
                } else {
                    sessions.add(new BreathingSession(title, duration, description));
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("✅ Successfully loaded " + sessions.size() + " sessions from database!");
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ Database error: " + e.getMessage());
            return false;
        }
    }
    
    // Demo data method
    private static void loadDemoData() {
        sessions.add(new GuidedSession("Morning Calm", 10, "Relax and focus your mind"));
        sessions.add(new BreathingSession("Deep Breathing", 5, "4-7-8 technique"));
        sessions.add(new GuidedSession("Sleep Meditation", 15, "Perfect for bedtime"));
        sessions.add(new BreathingSession("Stress Relief", 8, "Calm your nervous system"));
        sessions.add(new GuidedSession("Anxiety Relief", 12, "Release worry and fear"));
    }
    
    private static void createGUI() {
        // Main Frame
        JFrame frame = new JFrame("🧘 Mindfulness Meditation Platform");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(173, 216, 230));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel header = new JLabel("🧘 Meditation Sessions", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(new Color(0, 100, 0));
        
        // Status Label - Shows database connection status
        boolean usingDemoData = sessions.isEmpty() || sessions.get(0).getTitle().equals("Morning Calm");
        JLabel statusLabel = new JLabel(usingDemoData ? 
            "🔴 Using Demo Data (Database Connection Failed)" : 
            "🟢 Connected to Database", JLabel.CENTER);
        statusLabel.setForeground(usingDemoData ? Color.RED : Color.GREEN);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Sessions Panel
        JPanel sessionsPanel = new JPanel();
        sessionsPanel.setLayout(new GridLayout(0, 1, 10, 10));
        sessionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add session buttons
        for (int i = 0; i < sessions.size(); i++) {
            MeditationSession session = sessions.get(i);
            
            JButton sessionBtn = new JButton("<html><b>" + session.getTitle() + "</b><br>" +
                                           session.getDuration() + " minutes • " + session.getType() + 
                                           "<br><small>" + session.getDescription() + "</small></html>");
            
            sessionBtn.setBackground(new Color(240, 248, 255));
            sessionBtn.setFont(new Font("Arial", Font.PLAIN, 14));
            sessionBtn.setHorizontalAlignment(SwingConstants.LEFT);
            sessionBtn.setPreferredSize(new Dimension(500, 80));
            
            final int sessionIndex = i;
            sessionBtn.addActionListener(e -> startSession(sessionIndex));
            
            sessionsPanel.add(sessionBtn);
        }
        
        // Scroll Panel
        JScrollPane scrollPane = new JScrollPane(sessionsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Progress Label
        JLabel progressLabel = new JLabel("Total Sessions: " + sessions.size() + " | Select a session to begin");
        progressLabel.setHorizontalAlignment(JLabel.CENTER);
        progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add components to frame
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(progressLabel, BorderLayout.SOUTH);
        
        frame.setVisible(true);
    }
    
    private static void startSession(int sessionIndex) {
        MeditationSession session = sessions.get(sessionIndex);
        
        // Create session dialog
        JDialog sessionDialog = new JDialog();
        sessionDialog.setTitle("Meditation Session: " + session.getTitle());
        sessionDialog.setSize(450, 350);
        sessionDialog.setLayout(new BorderLayout());
        sessionDialog.setLocationRelativeTo(null);
        sessionDialog.setModal(true);
        
        // Session content
        JLabel sessionLabel = new JLabel("<html><center><h1>" + session.getTitle() + "</h1>" +
                                        "Duration: " + session.getDuration() + " minutes<br>" +
                                        "Type: " + session.getType() + "<br><br>" +
                                        "<i>" + session.getDescription() + "</i></center></html>", JLabel.CENTER);
        sessionLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("0% Complete");
        progressBar.setBackground(Color.WHITE);
        progressBar.setForeground(new Color(50, 150, 50));
        
        // Timer label
        JLabel timerLabel = new JLabel("Time remaining: " + session.getDuration() + " minutes", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Control buttons
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton startBtn = new JButton("▶ Start Session");
        JButton pauseBtn = new JButton("⏸ Pause");
        JButton stopBtn = new JButton("⏹ Stop");
        
        startBtn.setBackground(new Color(50, 150, 50));
        pauseBtn.setBackground(new Color(255, 200, 50));
        stopBtn.setBackground(new Color(220, 80, 60));
        
        startBtn.setForeground(Color.WHITE);
        pauseBtn.setForeground(Color.BLACK);
        stopBtn.setForeground(Color.WHITE);
        
        startBtn.setFont(new Font("Arial", Font.BOLD, 12));
        pauseBtn.setFont(new Font("Arial", Font.BOLD, 12));
        stopBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Timer variables
        javax.swing.Timer[] timer = new javax.swing.Timer[1];
        int[] progress = new int[1];
        int totalTime = session.getDuration() * 60; // Convert to seconds
        int[] remainingTime = new int[1];
        remainingTime[0] = totalTime;
        
        startBtn.addActionListener(e -> {
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            
            timer[0] = new javax.swing.Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    progress[0] += (100 / totalTime);
                    remainingTime[0]--;
                    
                    if (progress[0] >= 100) {
                        progress[0] = 100;
                        timer[0].stop();
                        JOptionPane.showMessageDialog(sessionDialog, 
                            "🎉 Session Completed!\nYou meditated for " + session.getDuration() + " minutes.\n\nGreat job! 🧘");
                        sessionDialog.dispose();
                    }
                    
                    progressBar.setValue(progress[0]);
                    progressBar.setString(progress[0] + "% Complete");
                    
                    int minutes = remainingTime[0] / 60;
                    int seconds = remainingTime[0] % 60;
                    timerLabel.setText(String.format("Time remaining: %02d:%02d", minutes, seconds));
                }
            });
            timer[0].start();
        });
        
        pauseBtn.addActionListener(e -> {
            if (timer[0] != null) {
                if (pauseBtn.getText().equals("⏸ Pause")) {
                    timer[0].stop();
                    pauseBtn.setText("▶ Resume");
                    pauseBtn.setBackground(new Color(50, 150, 50));
                } else {
                    timer[0].start();
                    pauseBtn.setText("⏸ Pause");
                    pauseBtn.setBackground(new Color(255, 200, 50));
                }
            }
        });
        
        stopBtn.addActionListener(e -> {
            if (timer[0] != null) {
                timer[0].stop();
            }
            int result = JOptionPane.showConfirmDialog(sessionDialog, 
                "Are you sure you want to stop the session?", 
                "Stop Session", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                sessionDialog.dispose();
                JOptionPane.showMessageDialog(null, "Session stopped. Remember to come back later! 🙏");
            } else if (timer[0] != null) {
                timer[0].start();
            }
        });
        
        controlPanel.add(startBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);
        
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(progressBar, BorderLayout.CENTER);
        infoPanel.add(timerLabel, BorderLayout.SOUTH);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add to dialog
        sessionDialog.add(sessionLabel, BorderLayout.CENTER);
        sessionDialog.add(infoPanel, BorderLayout.NORTH);
        sessionDialog.add(controlPanel, BorderLayout.SOUTH);
        
        sessionDialog.setVisible(true);
    }
}

// Base Session Class
abstract class MeditationSession {
    protected String title;
    protected int duration;
    protected String description;
    
    public MeditationSession(String title, int duration, String description) {
        this.title = title;
        this.duration = duration;
        this.description = description;
    }
    
    public abstract String getType();
    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getDescription() { return description; }
    
    // Session operations methods
    public void play() {
        System.out.println("Playing session: " + title);
    }
    
    public void pause() {
        System.out.println("Session paused: " + title);
    }
    
    public void stop() {
        System.out.println("Session stopped: " + title);
    }
}

// Guided Session
class GuidedSession extends MeditationSession {
    public GuidedSession(String title, int duration, String description) {
        super(title, duration, description);
    }
    
    @Override
    public String getType() {
        return "GUIDED MEDITATION";
    }
    
    @Override
    public void play() {
        System.out.println("🎧 Playing guided meditation: " + getTitle());
        System.out.println("Follow the instructor's voice...");
    }
}

// Breathing Session  
class BreathingSession extends MeditationSession {
    public BreathingSession(String title, int duration, String description) {
        super(title, duration, description);
    }
    
    @Override
    public String getType() {
        return "BREATHING EXERCISE";
    }
    
    @Override
    public void play() {
        System.out.println("🌬️ Starting breathing exercise: " + getTitle());
        System.out.println("Inhale... Exhale... Repeat...");
    }
}